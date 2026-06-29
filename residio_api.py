"""
Residio API — FastAPI backend that "operates" the Residio app.

Single source of truth: residio-data.json (same data the prototype ships with).
Implements master / owner / staff auth + role-scoped read endpoints and a few
write actions (acknowledge ticket, mark task done, generate payout).

Run:
    pip install fastapi uvicorn pydantic
    uvicorn residio_api:app --reload --port 8000

Then point the prototype at it:  window.RESIDIO_API = "http://localhost:8000"

Demo logins (username / password):
    master  / residio@master      -> Master ID MASTER-0001 (sees everything)
    rajiv   / owner@1001           -> Owner OWN-1001
    anita   / owner@1002           -> Owner OWN-1002 (NRI)
    suresh  / staff@2001           -> Staff STF-2001
    priya   / staff@2002           -> Staff STF-2002
"""

from __future__ import annotations

import json
import secrets
from datetime import datetime
from pathlib import Path
from typing import Optional

from fastapi import Depends, FastAPI, HTTPException, Header
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

DATA_FILE = Path(__file__).with_name("residio-data.json")

app = FastAPI(title="Residio API", version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# ----------------------------------------------------------------------------- data
def load_db() -> dict:
    with DATA_FILE.open(encoding="utf-8") as fh:
        return json.load(fh)


def save_db(db: dict) -> None:
    with DATA_FILE.open("w", encoding="utf-8") as fh:
        json.dump(db, fh, indent=2, ensure_ascii=False)


DB = load_db()
SESSIONS: dict[str, str] = {}  # token -> user_id


# ----------------------------------------------------------------------------- auth
class LoginIn(BaseModel):
    username: str
    password: str


def find_user(username: str) -> Optional[dict]:
    uname = username.strip().lower()
    for u in DB["users"]:
        if u["username"].lower() == uname or u["id"].lower() == uname:
            return u
    return None


def current_user(authorization: str = Header(default="")) -> dict:
    token = authorization.replace("Bearer ", "").strip()
    uid = SESSIONS.get(token)
    if not uid:
        raise HTTPException(status_code=401, detail="Not authenticated")
    return next(u for u in DB["users"] if u["id"] == uid)


def public_user(u: dict) -> dict:
    return {k: v for k, v in u.items() if k != "password"}


@app.post("/auth/login")
def login(body: LoginIn):
    u = find_user(body.username)
    if not u or u["password"] != body.password:
        raise HTTPException(status_code=401, detail="Invalid credentials")
    token = secrets.token_urlsafe(24)
    SESSIONS[token] = u["id"]
    return {"token": token, "user": public_user(u)}


@app.post("/auth/logout")
def logout(authorization: str = Header(default="")):
    SESSIONS.pop(authorization.replace("Bearer ", "").strip(), None)
    return {"ok": True}


@app.get("/me")
def me(user: dict = Depends(current_user)):
    return public_user(user)


# ----------------------------------------------------------------------------- scoping
def visible_properties(user: dict) -> list[dict]:
    props = DB["properties"]
    if user["role"] == "master":
        return props
    if user["role"] == "owner":
        return [p for p in props if p["owner_id"] == user["id"]]
    # staff: properties where they are AM or have a task/ticket
    ids = {p["id"] for p in props if p.get("am_id") == user["id"]}
    ids |= {t["property_id"] for t in DB["tasks"] if t["staff_id"] == user["id"]}
    ids |= {t["property_id"] for t in DB["tickets"] if t["staff_id"] == user["id"]}
    return [p for p in props if p["id"] in ids]


def scope_ids(user: dict) -> set[str]:
    return {p["id"] for p in visible_properties(user)}


# ----------------------------------------------------------------------------- data endpoints
@app.get("/properties")
def properties(user: dict = Depends(current_user)):
    return visible_properties(user)


@app.get("/bookings")
def bookings(user: dict = Depends(current_user)):
    ids = scope_ids(user)
    return [b for b in DB["bookings"] if b["property_id"] in ids]


@app.get("/statements")
def statements(user: dict = Depends(current_user)):
    ids = scope_ids(user)
    return [s for s in DB["statements"] if s["property_id"] in ids]


@app.get("/tasks")
def tasks(user: dict = Depends(current_user)):
    if user["role"] == "owner":
        raise HTTPException(status_code=403, detail="Owners have no task queue")
    rows = DB["tasks"]
    if user["role"] == "staff":
        rows = [t for t in rows if t["staff_id"] == user["id"]]
    return rows


@app.get("/tickets")
def tickets(user: dict = Depends(current_user)):
    ids = scope_ids(user)
    rows = [t for t in DB["tickets"] if t["property_id"] in ids]
    if user["role"] == "staff":
        rows = [t for t in rows if t["staff_id"] == user["id"]]
    return rows


@app.get("/staff")
def staff(user: dict = Depends(current_user)):
    if user["role"] != "master":
        raise HTTPException(status_code=403, detail="Master only")
    return [public_user(u) for u in DB["users"] if u["role"] == "staff"]


@app.get("/owners")
def owners(user: dict = Depends(current_user)):
    if user["role"] != "master":
        raise HTTPException(status_code=403, detail="Master only")
    return [public_user(u) for u in DB["users"] if u["role"] == "owner"]


# ----------------------------------------------------------------------------- portfolio summary
@app.get("/summary")
def summary(user: dict = Depends(current_user)):
    props = visible_properties(user)
    ids = {p["id"] for p in props}
    stmts = [s for s in DB["statements"] if s["property_id"] in ids]
    gross = sum(s["gross"] for s in stmts)
    owner_payout = sum(s["owner_share"] for s in stmts)
    mgmt_fee = sum(s["mgmt_fee"] for s in stmts)
    live = [p for p in props if p["status"] == "live"]
    occ = round(sum(p["occupancy"] for p in live) / len(live), 3) if live else 0
    rated = [p for p in live if p["rating"]]
    rating = round(sum(p["rating"] for p in rated) / len(rated), 2) if rated else 0
    return {
        "role": user["role"],
        "properties": len(props),
        "live": len(live),
        "gross_mtd": gross,
        "owner_payout_mtd": owner_payout,
        "mgmt_fee_mtd": mgmt_fee,
        "avg_occupancy": occ,
        "avg_rating": rating,
        "open_tickets": len([t for t in DB["tickets"] if t["property_id"] in ids and t["status"] != "resolved"]),
    }


# ----------------------------------------------------------------------------- write actions
@app.post("/tasks/{task_id}/done")
def complete_task(task_id: str, user: dict = Depends(current_user)):
    for t in DB["tasks"]:
        if t["id"] == task_id:
            if user["role"] == "staff" and t["staff_id"] != user["id"]:
                raise HTTPException(status_code=403, detail="Not your task")
            t["status"] = "done"
            save_db(DB)
            return t
    raise HTTPException(status_code=404, detail="Task not found")


@app.post("/tickets/{ticket_id}/resolve")
def resolve_ticket(ticket_id: str, user: dict = Depends(current_user)):
    for t in DB["tickets"]:
        if t["id"] == ticket_id:
            t["status"] = "resolved"
            save_db(DB)
            return t
    raise HTTPException(status_code=404, detail="Ticket not found")


@app.post("/statements/{stmt_id}/payout")
def mark_payout(stmt_id: str, user: dict = Depends(current_user)):
    if user["role"] != "master":
        raise HTTPException(status_code=403, detail="Master only")
    for s in DB["statements"]:
        if s["id"] == stmt_id:
            s["paid"] = True
            s["payout_date"] = datetime.utcnow().date().isoformat()
            save_db(DB)
            return s
    raise HTTPException(status_code=404, detail="Statement not found")


@app.get("/")
def root():
    return {"service": "Residio API", "docs": "/docs", "master_id": DB["org"]["master_id"]}

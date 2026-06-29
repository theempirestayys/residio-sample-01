# Residio Owner — Native Android App

A real native **Kotlin + Jetpack Compose** Android app, built from your Residio Owner wireframe and the `residio-data.json` data model. Role-based login (Master / Owner / Staff) with property, booking, payout, task and ticket views.

- **Package / applicationId:** `com.theempirestays.residio`
- **targetSdk:** 36 (required for new Play apps)
- **minSdk:** 24 (covers ~98% of Android phones)
- **Data:** ships offline inside the app (`app/src/main/assets/residio-data.json`). The included `residio_api.py` FastAPI backend matches the same shapes, so you can switch the app to the live API later with no model changes.

## Run it on your computer
1. Install **Android Studio** (free): https://developer.android.com/studio
2. **File → Open** → select this `residio-owner-app` folder.
3. Wait for "Gradle sync" to finish (Android Studio downloads everything and creates the Gradle wrapper automatically).
4. Press the green **▶ Run** button with an emulator or a USB-connected phone.

## Demo logins
| Username | Password | Role |
|---|---|---|
| master | residio@master | Master Admin (sees everything) |
| rajiv | owner@1001 | Owner |
| anita | owner@1002 | Owner (NRI) |
| suresh | staff@2001 | Staff |
| priya | staff@2002 | Staff |

## Publish it
Follow **`GITHUB_AND_PLAYSTORE_GUIDE.md`** — the straight, step-by-step path from this folder → GitHub → Google Play. After one-time setup, every release is a single `git tag`.

package com.theempirestays.residio.data

import android.content.Context
import org.json.JSONObject

/**
 * Loads the bundled residio-data.json (same source of truth as the prototype + FastAPI backend)
 * and exposes role-scoped reads. To switch to the live API later, replace loadFromAssets()
 * with a network call to the FastAPI endpoints in residio_api.py — the model shapes match 1:1.
 */
object ResidioRepository {
    lateinit var org: Org
    var users: List<User> = emptyList()
    var properties: List<Property> = emptyList()
    var bookings: List<Booking> = emptyList()
    var statements: List<Statement> = emptyList()
    var tasks: List<Task> = emptyList()
    var tickets: List<Ticket> = emptyList()
    var activity: List<Activity> = emptyList()
    private var loaded = false

    fun load(context: Context) {
        if (loaded) return
        val raw = context.assets.open("residio-data.json")
            .bufferedReader().use { it.readText() }
        val root = JSONObject(raw)

        val o = root.getJSONObject("org")
        org = Org(o.getString("name"), o.getString("brand"), o.getString("master_id"),
            o.getString("city"), o.getString("currency"))

        users = root.getJSONArray("users").map {
            User(it.getString("id"), it.getString("role"), it.getString("name"),
                it.getString("title"), it.getString("email"), it.getString("username"),
                it.getString("password"), it.getString("phone"), it.getString("initials"),
                it.optBoolean("nri", false))
        }
        properties = root.getJSONArray("properties").map {
            Property(it.getString("id"), it.getString("name"), it.getString("owner_id"),
                it.getString("city"), it.getString("type"), it.getString("status"),
                it.getInt("adr"), it.getDouble("occupancy"), it.getDouble("rating"),
                it.getString("am_id"))
        }
        bookings = root.getJSONArray("bookings").map {
            Booking(it.getString("id"), it.getString("property_id"), it.getString("guest"),
                it.getString("platform"), it.getString("checkin"), it.getString("checkout"),
                it.getInt("nights"), it.getInt("gross"), it.getString("status"))
        }
        statements = root.getJSONArray("statements").map {
            Statement(it.getString("id"), it.getString("property_id"), it.getString("month"),
                it.getInt("gross"), it.getInt("net"), it.getInt("owner_share"),
                it.getInt("mgmt_fee"), it.getInt("gst"), it.getBoolean("paid"),
                it.getString("payout_date"))
        }
        tasks = root.getJSONArray("tasks").map {
            Task(it.getString("id"), it.getString("property_id"), it.getString("type"),
                it.getString("title"), it.getString("due"), it.getString("status"),
                it.getString("staff_id"), it.getString("priority"))
        }
        tickets = root.getJSONArray("tickets").map {
            Ticket(it.getString("id"), it.getString("property_id"), it.getString("category"),
                it.getString("title"), it.getString("priority"), it.getString("status"),
                it.getString("staff_id"), it.getString("opened"), it.getInt("cost"))
        }
        activity = root.getJSONArray("activity").map {
            Activity(it.getString("id"), it.getString("ts"), it.getString("text"), it.getString("role"))
        }
        loaded = true
    }

    fun authenticate(username: String, password: String): User? =
        users.firstOrNull { it.username == username.trim() && it.password == password }

    // ----- role-scoped views -----
    fun propertiesFor(user: User): List<Property> = when (user.role) {
        "master" -> properties
        "owner" -> properties.filter { it.ownerId == user.id }
        "staff" -> properties.filter { it.amId == user.id }
        else -> emptyList()
    }

    fun bookingsFor(user: User): List<Booking> {
        val ids = propertiesFor(user).map { it.id }.toSet()
        return bookings.filter { it.propertyId in ids }
    }

    fun statementsFor(user: User): List<Statement> {
        val ids = propertiesFor(user).map { it.id }.toSet()
        return statements.filter { it.propertyId in ids }
    }

    fun tasksFor(user: User): List<Task> = when (user.role) {
        "staff" -> tasks.filter { it.staffId == user.id }
        else -> { val ids = propertiesFor(user).map { it.id }.toSet(); tasks.filter { it.propertyId in ids } }
    }

    fun ticketsFor(user: User): List<Ticket> = when (user.role) {
        "staff" -> tickets.filter { it.staffId == user.id }
        else -> { val ids = propertiesFor(user).map { it.id }.toSet(); tickets.filter { it.propertyId in ids } }
    }
}

// small helper so we can .map over a JSONArray of objects
private inline fun <T> org.json.JSONArray.map(block: (JSONObject) -> T): List<T> =
    (0 until length()).map { block(getJSONObject(it)) }

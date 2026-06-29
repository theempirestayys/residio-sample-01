package com.theempirestays.residio.data

data class User(
    val id: String, val role: String, val name: String, val title: String,
    val email: String, val username: String, val password: String,
    val phone: String, val initials: String, val nri: Boolean = false
)

data class Property(
    val id: String, val name: String, val ownerId: String, val city: String,
    val type: String, val status: String, val adr: Int, val occupancy: Double,
    val rating: Double, val amId: String
)

data class Booking(
    val id: String, val propertyId: String, val guest: String, val platform: String,
    val checkin: String, val checkout: String, val nights: Int, val gross: Int, val status: String
)

data class Statement(
    val id: String, val propertyId: String, val month: String, val gross: Int,
    val net: Int, val ownerShare: Int, val mgmtFee: Int, val gst: Int,
    val paid: Boolean, val payoutDate: String
)

data class Task(
    val id: String, val propertyId: String, val type: String, val title: String,
    val due: String, val status: String, val staffId: String, val priority: String
)

data class Ticket(
    val id: String, val propertyId: String, val category: String, val title: String,
    val priority: String, val status: String, val staffId: String, val opened: String, val cost: Int
)

data class Activity(val id: String, val ts: String, val text: String, val role: String)

data class Org(val name: String, val brand: String, val masterId: String, val city: String, val currency: String)

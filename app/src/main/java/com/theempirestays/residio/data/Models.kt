package com.theempirestays.residio.data

data class User(
    val id: String, val role: String, val name: String, val title: String,
    val email: String, val username: String, val password: String,
    val phone: String, val initials: String, val nri: Boolean = false,
    val biometricEnabled: Boolean = false, val twoFaEnabled: Boolean = false,
    val googleLinked: Boolean = false
)

data class Property(
    val id: String, val name: String, val ownerId: String, val city: String,
    val type: String, val status: String, val adr: Int, val occupancy: Double,
    val rating: Double, val amId: String,
    val address: String = "", val bedrooms: Int = 1, val bathrooms: Int = 1,
    val maxGuests: Int = 2, val photoUrl: String = "",
    val airbnbListingId: String = "", val bookingComListingId: String = "",
    val vrboListingId: String = "", val directBookingEnabled: Boolean = false
)

data class Booking(
    val id: String, val propertyId: String, val guest: String, val platform: String,
    val checkin: String, val checkout: String, val nights: Int, val gross: Int,
    val status: String, val guestId: String = "", val smartLockCode: String = "",
    val cleaningStatus: String = "pending", val guestEmail: String = "",
    val guestPhone: String = "", val adults: Int = 2, val children: Int = 0,
    val specialRequests: String = ""
)

data class Statement(
    val id: String, val propertyId: String, val month: String, val gross: Int,
    val net: Int, val ownerShare: Int, val mgmtFee: Int, val gst: Int,
    val paid: Boolean, val payoutDate: String,
    val expenses: Int = 0, val platformFees: Int = 0, val taxDeducted: Int = 0
)

data class Task(
    val id: String, val propertyId: String, val type: String, val title: String,
    val due: String, val status: String, val staffId: String, val priority: String,
    val notes: String = "", val completedAt: String = ""
)

data class Ticket(
    val id: String, val propertyId: String, val category: String, val title: String,
    val priority: String, val status: String, val staffId: String,
    val opened: String, val cost: Int,
    val vendorName: String = "", val resolvedAt: String = "", val notes: String = ""
)

data class Activity(val id: String, val ts: String, val text: String, val role: String)

data class Org(
    val name: String, val brand: String, val masterId: String,
    val city: String, val currency: String,
    val gstin: String = "", val pan: String = "", val address: String = ""
)

// ─────────── NEW HOSPITALITY MODELS ───────────

data class Guest(
    val id: String, val name: String, val email: String, val phone: String,
    val nationality: String, val idType: String, val idNumber: String,
    val idVerified: Boolean, val vipStatus: Boolean,
    val totalStays: Int, val averageRating: Double, val notes: String,
    val preferredLanguage: String = "English", val emergencyContact: String = ""
)

data class GuestMessage(
    val id: String, val bookingId: String, val guestId: String,
    val channel: String, val direction: String,
    val text: String, val ts: String, val read: Boolean,
    val attachmentUrl: String = ""
)

data class OtaChannel(
    val id: String, val name: String, val icon: String,
    val connected: Boolean, val lastSync: String,
    val activeListings: Int, val pendingBookings: Int,
    val syncStatus: String = "ok"
)

data class Review(
    val id: String, val propertyId: String, val guestName: String,
    val platform: String, val rating: Double, val cleanliness: Double,
    val communication: Double, val location: Double, val accuracy: Double,
    val comment: String, val ownerReply: String?,
    val date: String, val responded: Boolean
)

data class SmartLock(
    val id: String, val propertyId: String, val brand: String,
    val model: String, val status: String, val battery: Int,
    val currentCode: String, val expiresAt: String?,
    val lastAccess: String = "", val accessLog: List<String> = emptyList()
)

data class LegalDocument(
    val id: String, val type: String, val title: String,
    val propertyId: String?, val version: String,
    val updatedAt: String, val signedByGuest: Boolean,
    val requiresSignature: Boolean, val content: String = ""
)

data class CleaningJob(
    val id: String, val propertyId: String, val bookingId: String?,
    val staffId: String, val staffName: String,
    val scheduledAt: String, val completedAt: String?,
    val status: String, val notes: String,
    val estimatedMinutes: Int = 90,
    val checklist: List<CleaningItem> = emptyList()
)

data class CleaningItem(
    val id: String, val room: String, val task: String, val done: Boolean
)

data class Expense(
    val id: String, val propertyId: String, val category: String,
    val description: String, val amount: Int, val date: String,
    val vendorName: String = "", val receiptAvailable: Boolean = false,
    val approved: Boolean = false, val approvedBy: String = ""
)

data class EmergencyContact(
    val id: String, val name: String, val role: String,
    val phone: String, val email: String,
    val available24h: Boolean, val propertyId: String?,
    val responseTimeMinutes: Int = 30
)

data class AppNotification(
    val id: String, val type: String, val title: String,
    val body: String, val ts: String, val read: Boolean,
    val propertyId: String? = null, val bookingId: String? = null
)

data class RevenueMetric(
    val month: String, val revpar: Double, val adr: Double,
    val occupancy: Double, val gross: Int, val expenses: Int,
    val netRevenue: Int = gross - expenses
)

data class SecuritySession(
    val deviceId: String, val deviceName: String,
    val lastActive: String, val location: String, val current: Boolean
)

data class CompetitorProperty(
    val name: String, val platform: String, val adr: Int,
    val rating: Double, val reviewCount: Int, val distance: String
)

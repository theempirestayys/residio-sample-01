package com.theempirestays.residio.data

import android.content.Context
import org.json.JSONObject

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

    // ─── Hospitality extension data (in-memory, replace with API calls) ───
    val guests: List<Guest> = listOf(
        Guest("g01","Arjun Mehta","arjun.mehta@gmail.com","+91 98200 11234","Indian","Aadhaar","XXXX-XXXX-4521",true,true,7,4.9,"Prefers late check-out","English","Priya Mehta +91 98200 11235"),
        Guest("g02","Sophie Williams","sophie.w@outlook.com","+44 7700 900123","British","Passport","UK123456",true,false,3,4.7,"Allergic to feather pillows","English",""),
        Guest("g03","Rahul Sharma","rahul.sharma@yahoo.com","+91 99300 45678","Indian","Aadhaar","XXXX-XXXX-8832",true,false,1,4.5,"First time guest","Hindi",""),
        Guest("g04","Yuki Tanaka","yuki.tanaka@email.jp","+81 90-1234-5678","Japanese","Passport","TJ9876543",true,true,5,5.0,"Loves traditional decor","Japanese",""),
        Guest("g05","Pradeep Nair","pradeep.nair@gmail.com","+91 94455 77890","Indian","PAN","ABCDE1234F",false,false,0,0.0,"New guest, ID pending","Malayalam",""),
        Guest("g06","Emma Dubois","emma.dubois@gmail.com","+33 6 12 34 56 78","French","Passport","FR7654321",true,true,9,4.8,"Wine enthusiast","French","Marc Dubois +33 6 12 34 56 79"),
        Guest("g07","Carlos Reyes","carlos.r@hotmail.com","+52 55 1234 5678","Mexican","Passport","MX456789",true,false,2,4.6,"Early bird","Spanish",""),
        Guest("g08","Anita Singh","anita.singh@corporates.in","+91 96600 33221","Indian","Aadhaar","XXXX-XXXX-2211",true,false,4,4.7,"Business traveller, needs invoice","English","HR Department: hr@corporates.in")
    )

    val messages: List<GuestMessage> = listOf(
        GuestMessage("m01","bk-001","g01","Airbnb","inbound","Hi! What time can we check in? We land at 2pm","2026-06-30T10:15:00",false),
        GuestMessage("m02","bk-001","g01","Airbnb","outbound","Welcome Arjun! Check-in is 3pm. I'll send the smart lock code at noon.","2026-06-30T10:22:00",true),
        GuestMessage("m03","bk-002","g02","Booking.com","inbound","Is there parking available? We have a rental car","2026-06-29T14:00:00",true),
        GuestMessage("m04","bk-002","g02","Booking.com","outbound","Yes! Free parking in the basement, spot B-12. I'll email directions.","2026-06-29T14:10:00",true),
        GuestMessage("m05","bk-004","g04","Direct","inbound","Could you arrange airport transfer from BOM at 6am?","2026-07-01T08:00:00",false),
        GuestMessage("m06","bk-005","g05","Airbnb","inbound","I haven't received the lock code yet","2026-07-01T15:30:00",false),
        GuestMessage("m07","bk-003","g03","VRBO","inbound","Can we get extra towels for 4 people?","2026-06-28T11:00:00",true),
        GuestMessage("m08","bk-003","g03","VRBO","outbound","Absolutely! I'll ask housekeeping to add 2 extra sets before check-in.","2026-06-28T11:15:00",true),
        GuestMessage("m09","bk-006","g06","Airbnb","inbound","The wine cooler doesn't seem to be working","2026-07-01T19:00:00",false),
        GuestMessage("m10","bk-007","g07","Direct","inbound","Is the rooftop pool open early morning?","2026-07-01T06:45:00",false)
    )

    val channels: List<OtaChannel> = listOf(
        OtaChannel("ch01","Airbnb","airbnb",true,"2026-07-01 08:00",4,2,"ok"),
        OtaChannel("ch02","Booking.com","booking",true,"2026-07-01 08:05",3,1,"ok"),
        OtaChannel("ch03","VRBO / Vrbo","vrbo",true,"2026-07-01 07:45",2,0,"ok"),
        OtaChannel("ch04","Direct Booking","direct",true,"2026-07-01 08:00",4,3,"ok"),
        OtaChannel("ch05","MakeMyTrip","mmt",false,"—",0,0,"disconnected"),
        OtaChannel("ch06","Goibibo","goibibo",false,"—",0,0,"not set up"),
        OtaChannel("ch07","Expedia","expedia",true,"2026-07-01 07:50",1,0,"ok"),
        OtaChannel("ch08","Hotels.com","hotels",false,"—",0,0,"not set up")
    )

    val reviews: List<Review> = listOf(
        Review("r01","p001","Arjun Mehta","Airbnb",5.0,5.0,5.0,4.5,5.0,"Perfect stay. Spotless property, instant responses, amazing experience. Will definitely return!","Thank you Arjun! It was a pleasure hosting you. Looking forward to your next visit.","2026-06-20",true),
        Review("r02","p001","Sophie Williams","Booking.com",4.5,4.5,5.0,4.0,4.5,"Beautiful apartment in a great location. Host was very responsive. The only minor issue was the lift was under maintenance.","Thank you Sophie! We've noted the lift issue and it's been resolved. Hope to host you again soon.","2026-06-15",true),
        Review("r03","p002","Rahul Sharma","Airbnb",5.0,5.0,5.0,5.0,5.0,"Outstanding villa! Every amenity was perfect. Smart lock made check-in super easy. Highly recommend.",null,"2026-06-10",false),
        Review("r04","p003","Emma Dubois","VRBO",4.0,4.0,4.5,5.0,3.5,"Lovely sea view. The kitchen could use an update but overall a great stay. Host very accommodating.",null,"2026-05-28",false),
        Review("r05","p001","Yuki Tanaka","Airbnb",5.0,5.0,5.0,4.5,5.0,"Exceptional hospitality. The property is exactly as described — actually even better! The traditional touches were beautiful.",null,"2026-05-15",false),
        Review("r06","p004","Carlos Reyes","Direct",4.5,5.0,4.5,4.5,4.5,"Great studio for a work trip. High-speed WiFi, quiet area, professional service. Would book again.",null,"2026-05-10",false)
    )

    val smartLocks: List<SmartLock> = listOf(
        SmartLock("sl01","p001","Yale","Linus Smart Lock Pro","online",87,"482193","2026-07-05 11:00","2026-07-01 15:00", listOf("Guest check-in 15:00","Staff cleaning 10:00","Owner visit 09:00")),
        SmartLock("sl02","p002","August","Wi-Fi Smart Lock","online",62,"751920","2026-07-03 10:00","2026-07-01 14:30"),
        SmartLock("sl03","p003","Schlage","Encode Plus","offline",15,"—",null,"2026-06-28 09:00"),
        SmartLock("sl04","p004","Samsung","SHS-H505","online",94,"394827","2026-07-02 12:00","2026-07-01 12:00"),
        SmartLock("sl05","p005","Philips","EasyKey 9300","online",71,"628491","2026-07-04 11:00","2026-06-30 16:00")
    )

    val documents: List<LegalDocument> = listOf(
        LegalDocument("d01","rental_agreement","Rental Agreement – Short Stay","p001","v2.3","2026-01-15",true,true),
        LegalDocument("d02","house_rules","House Rules & Guidelines","p001","v1.8","2026-03-01",true,false),
        LegalDocument("d03","privacy_policy","Privacy Policy","null","v1.2","2025-12-01",false,false),
        LegalDocument("d04","terms","Terms & Conditions","null","v2.0","2025-12-01",false,false),
        LegalDocument("d05","id_verification","ID Verification Consent","null","v1.0","2026-02-01",false,true),
        LegalDocument("d06","rental_agreement","Rental Agreement – Short Stay","p002","v2.3","2026-01-15",false,true),
        LegalDocument("d07","noc","No-Objection Certificate","p003","v1.0","2026-04-01",true,false),
        LegalDocument("d08","maintenance","Maintenance Liability Waiver","null","v1.1","2026-01-01",false,false),
        LegalDocument("d09","refund","Refund & Cancellation Policy","null","v3.0","2026-05-01",false,false),
        LegalDocument("d10","emergency","Emergency Procedures Document","p001","v1.0","2026-06-01",false,false)
    )

    val cleaningJobs: List<CleaningJob> = listOf(
        CleaningJob("c01","p001","bk-001","s2001","Suresh Kumar","2026-07-01 11:00",null,"in_progress","Post checkout deep clean. Check under beds.",120, listOf(
            CleaningItem("ci01","Living Room","Vacuum and mop floors",true),
            CleaningItem("ci02","Living Room","Dust all surfaces",true),
            CleaningItem("ci03","Kitchen","Clean appliances",false),
            CleaningItem("ci04","Kitchen","Replace dish soap and sponge",false),
            CleaningItem("ci05","Bedroom 1","Change bed linen",true),
            CleaningItem("ci06","Bedroom 2","Change bed linen",false),
            CleaningItem("ci07","Bathroom","Deep clean and restock amenities",false),
            CleaningItem("ci08","Balcony","Sweep and wipe furniture",false)
        )),
        CleaningJob("c02","p002","bk-003","s2002","Priya Desai","2026-07-01 10:00","2026-07-01 11:45","completed","Standard turnover",90, listOf(
            CleaningItem("ci09","All rooms","Full turnover clean",true),
            CleaningItem("ci10","Bathroom","Restock toiletries",true),
            CleaningItem("ci11","Kitchen","Restock coffee and tea",true)
        )),
        CleaningJob("c03","p003","bk-005","s2001","Suresh Kumar","2026-07-02 09:00",null,"scheduled","First guest arrival. Set welcome flowers.",120),
        CleaningJob("c04","p004",null,"s2002","Priya Desai","2026-07-03 10:00",null,"scheduled","Routine weekly maintenance clean",60)
    )

    val expenses: List<Expense> = listOf(
        Expense("e01","p001","maintenance","AC service and filter replacement",3500,"2026-06-15","Arctic Cool HVAC",true,true,"master@residio"),
        Expense("e02","p001","supplies","Guest welcome kit (x10)",1200,"2026-06-20","Local Vendor",false,true,"master@residio"),
        Expense("e03","p002","repair","Bathroom tap replacement",800,"2026-06-18","Plumber - Ravi",true,false,""),
        Expense("e04","p003","utility","Electricity bill – May 2026",4200,"2026-06-05","MSEDCL",true,true,"master@residio"),
        Expense("e05","p001","cleaning","Deep clean after long stay",2500,"2026-06-25","CleanPro Services",false,true,"master@residio"),
        Expense("e06","p004","tech","Smart lock battery replacement",150,"2026-06-28","",false,true,"master@residio"),
        Expense("e07","p002","supplies","Pool chemicals – quarterly restock",3800,"2026-06-01","AquaShop",true,true,"master@residio"),
        Expense("e08","p001","furnishing","Bedroom lamp x2",1800,"2026-05-20","IKEA",false,false,"")
    )

    val emergencyContacts: List<EmergencyContact> = listOf(
        EmergencyContact("ec01","Police Control Room","Emergency Services","100","",true,null,5),
        EmergencyContact("ec02","Ambulance / 108","Emergency Services","108","",true,null,10),
        EmergencyContact("ec03","Fire Brigade","Emergency Services","101","",true,null,8),
        EmergencyContact("ec04","Ravi Plumber","Maintenance","99200 45678","ravi.plumber@gmail.com",false,null,45),
        EmergencyContact("ec05","Arctic Cool HVAC","Maintenance","98100 33221","support@arcticcool.in",false,null,120),
        EmergencyContact("ec06","CleanPro Services","Cleaning","87900 11223","ops@cleanpro.in",false,null,60),
        EmergencyContact("ec07","Dr. Neha Sharma – GP","Medical","98700 55443","drneha@medclinic.in",true,null,20),
        EmergencyContact("ec08","Electrician – Mohan","Maintenance","96600 88771","",false,"p001",90),
        EmergencyContact("ec09","Building Manager – Apartments 1","Facility","99300 22110","mgr@luxapts.in",true,"p001",15),
        EmergencyContact("ec10","Locksmith 24x7","Security","80900 66554","",true,null,30)
    )

    val revenueMetrics: List<RevenueMetric> = listOf(
        RevenueMetric("Jan 2026",1820.0,2600.0,0.70,187200,42000),
        RevenueMetric("Feb 2026",1950.0,2750.0,0.71,198000,39500),
        RevenueMetric("Mar 2026",2100.0,2900.0,0.72,209000,41000),
        RevenueMetric("Apr 2026",2300.0,3100.0,0.74,223000,38000),
        RevenueMetric("May 2026",2200.0,3000.0,0.73,216000,40000),
        RevenueMetric("Jun 2026",2450.0,3200.0,0.77,230400,37000)
    )

    val securitySessions: List<SecuritySession> = listOf(
        SecuritySession("dev001","iPhone 15 Pro – Meet","2026-07-01 10:30","Mumbai, IN",true),
        SecuritySession("dev002","MacBook Pro – Office","2026-06-30 18:00","Mumbai, IN",false),
        SecuritySession("dev003","iPad Air","2026-06-28 09:15","Mumbai, IN",false)
    )

    val competitors: List<CompetitorProperty> = listOf(
        CompetitorProperty("Sea View Suites","Airbnb",3200,4.8,142,"0.3 km"),
        CompetitorProperty("Urban Nest BKC","Booking.com",2800,4.6,87,"0.5 km"),
        CompetitorProperty("The Mumbai Retreat","VRBO",3500,4.9,231,"0.8 km"),
        CompetitorProperty("Skyline Residences","Airbnb",2600,4.5,53,"1.1 km"),
        CompetitorProperty("Bandra Bay Villas","Direct",4000,5.0,19,"1.4 km")
    )

    // ─── Core load from assets JSON ───
    fun load(context: Context) {
        if (loaded) return
        val raw = context.assets.open("residio-data.json")
            .bufferedReader().use { it.readText() }
        val root = JSONObject(raw)

        val o = root.getJSONObject("org")
        org = Org(o.getString("name"), o.getString("brand"), o.getString("master_id"),
            o.getString("city"), o.getString("currency"),
            o.optString("gstin",""), o.optString("pan",""), o.optString("address",""))

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
                it.getString("am_id"), it.optString("address",""),
                it.optInt("bedrooms",1), it.optInt("bathrooms",1), it.optInt("max_guests",2))
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

    fun authenticateGoogle(email: String): User? =
        users.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }

    // ─── Role-scoped views ───
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

    fun reviewsFor(user: User): List<Review> {
        val ids = propertiesFor(user).map { it.id }.toSet()
        return reviews.filter { it.propertyId in ids }
    }

    fun messagesFor(user: User): List<GuestMessage> {
        val bookingIds = bookingsFor(user).map { it.id }.toSet()
        return messages.filter { it.bookingId in bookingIds }
    }

    fun smartLocksFor(user: User): List<SmartLock> {
        val ids = propertiesFor(user).map { it.id }.toSet()
        return smartLocks.filter { it.propertyId in ids }
    }

    fun cleaningFor(user: User): List<CleaningJob> = when (user.role) {
        "staff" -> cleaningJobs.filter { it.staffId == user.id }
        else -> { val ids = propertiesFor(user).map { it.id }.toSet(); cleaningJobs.filter { it.propertyId in ids } }
    }

    fun expensesFor(user: User): List<Expense> {
        val ids = propertiesFor(user).map { it.id }.toSet()
        return expenses.filter { it.propertyId in ids }
    }
}

private inline fun <T> org.json.JSONArray.map(block: (JSONObject) -> T): List<T> =
    (0 until length()).map { block(getJSONObject(it)) }

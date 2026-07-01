package com.theempirestays.residio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theempirestays.residio.data.*
import java.text.NumberFormat
import java.util.Locale

private val rupee: NumberFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
private fun inr(v: Int) = "₹" + rupee.format(v)

data class NavTab(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(user: User, onLogout: () -> Unit) {
    var tab by remember { mutableStateOf(0) }

    val tabs: List<NavTab> = remember(user.role) {
        when (user.role) {
            "master" -> listOf(
                NavTab("Overview", Icons.Filled.Dashboard),
                NavTab("Properties", Icons.Filled.Apartment),
                NavTab("Guests", Icons.Filled.People),
                NavTab("Analytics", Icons.Filled.BarChart),
                NavTab("More", Icons.Filled.GridView)
            )
            "owner" -> listOf(
                NavTab("Overview", Icons.Filled.Dashboard),
                NavTab("Bookings", Icons.Filled.EventAvailable),
                NavTab("Calendar", Icons.Filled.CalendarMonth),
                NavTab("Reviews", Icons.Filled.Star),
                NavTab("More", Icons.Filled.GridView)
            )
            else -> listOf(
                NavTab("Today", Icons.Filled.Dashboard),
                NavTab("Tasks", Icons.Filled.Checklist),
                NavTab("Cleaning", Icons.Filled.CleaningServices),
                NavTab("Emergency", Icons.Filled.Emergency),
                NavTab("More", Icons.Filled.GridView)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("residio.", fontWeight = FontWeight.Bold)
                        Text("${user.name} · ${user.title}", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                actions = {
                    // Unread badge
                    val unreadCount = ResidioRepository.messagesFor(user).count { !it.read }
                    if (unreadCount > 0) {
                        BadgedBox(badge = {
                            Badge { Text("$unreadCount", fontSize = 8.sp) }
                        }) {
                            IconButton(onClick = { tab = tabs.indexOfFirst { it.label == "Guests" || it.label == "More" }.coerceAtLeast(0) }) {
                                Icon(Icons.Filled.Notifications, "Notifications")
                            }
                        }
                    } else {
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Notifications, "Notifications")
                        }
                    }
                    Box(
                        Modifier.padding(end = 4.dp).size(36.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(user.initials, color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, "Log out")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, navTab ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(navTab.icon, navTab.label, Modifier.size(22.dp)) },
                        label = { Text(navTab.label, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (tabs[tab].label) {
                "Overview", "Today" -> OverviewTab(user)
                "Properties" -> PropertiesTab(user)
                "Bookings" -> BookingsTab(user)
                "Calendar" -> CalendarScreen(user)
                "Guests" -> GuestsScreen(user)
                "Analytics" -> AnalyticsScreen(user)
                "Reviews" -> ReviewsScreen(user)
                "Tasks" -> TasksTab(user)
                "Cleaning" -> CleaningScreen(user)
                "Emergency" -> EmergencyScreen(user)
                "More" -> MoreScreen(user)
                else -> OverviewTab(user)
            }
        }
    }
}

// ─── More Screen ──────────────────────────────────────────────────────────────

@Composable
fun MoreScreen(user: User) {
    var subScreen by remember { mutableStateOf<String?>(null) }

    when (subScreen) {
        "Payouts" -> { PayoutsTab(user); return }
        "Channels" -> { ChannelScreen(user); return }
        "Smart Locks" -> { SmartLockScreen(user); return }
        "Documents" -> { DocumentsScreen(user); return }
        "Security" -> { SecurityScreen(user); return }
        "Ops / Tasks" -> { TasksTab(user); return }
        "Tickets" -> { TicketsTab(user); return }
        "Expenses" -> { ExpensesTab(user); return }
    }

    val masterItems = listOf(
        MoreItem("Payouts", Icons.Filled.Payments, "Statements & payouts"),
        MoreItem("Channels", Icons.Filled.Language, "OTA channel manager"),
        MoreItem("Smart Locks", Icons.Filled.Lock, "Access codes & control"),
        MoreItem("Documents", Icons.Filled.Description, "Legal docs & agreements"),
        MoreItem("Expenses", Icons.Filled.Receipt, "Property expenses"),
        MoreItem("Ops / Tasks", Icons.Filled.Checklist, "Staff task management"),
        MoreItem("Tickets", Icons.Filled.Build, "Maintenance tickets"),
        MoreItem("Security", Icons.Filled.Security, "Account & access security")
    )
    val ownerItems = listOf(
        MoreItem("Payouts", Icons.Filled.Payments, "Your statements"),
        MoreItem("Properties", Icons.Filled.Apartment, "Your properties"),
        MoreItem("Smart Locks", Icons.Filled.Lock, "Access codes"),
        MoreItem("Documents", Icons.Filled.Description, "Rental agreements"),
        MoreItem("Channels", Icons.Filled.Language, "Booking platforms"),
        MoreItem("Security", Icons.Filled.Security, "Account security")
    )
    val staffItems = listOf(
        MoreItem("Tickets", Icons.Filled.Build, "Maintenance tickets"),
        MoreItem("Documents", Icons.Filled.Description, "Property docs"),
        MoreItem("Smart Locks", Icons.Filled.Lock, "Access codes"),
        MoreItem("Security", Icons.Filled.Security, "Account security")
    )

    val items = when (user.role) {
        "master" -> masterItems
        "owner" -> ownerItems
        else -> staffItems
    }

    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        item {
            Text("More", fontWeight = FontWeight.Bold, fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        }
        items(items.chunked(2)) { row ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                row.forEach { item ->
                    Surface(
                        onClick = { subScreen = item.title },
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 1.dp,
                        modifier = Modifier.weight(1f).padding(6.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Icon(item.icon, null, Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(item.subtitle, fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f).padding(6.dp))
            }
        }
    }
}

private data class MoreItem(val title: String, val icon: ImageVector, val subtitle: String)

// ─── Existing Tabs (retained + enhanced) ──────────────────────────────────────

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        content()
    }
}

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) { Column(Modifier.padding(16.dp), content = content) }
}

@Composable
private fun OverviewTab(user: User) {
    val props = ResidioRepository.propertiesFor(user)
    val stmts = ResidioRepository.statementsFor(user)
    val bookings = ResidioRepository.bookingsFor(user)
    val live = props.count { it.status == "live" }
    val occ = if (props.isNotEmpty()) props.map { it.occupancy }.average() else 0.0
    val ownerEarn = stmts.sumOf { it.ownerShare }
    val mgmt = stmts.sumOf { it.mgmtFee }
    val unreadMsgs = ResidioRepository.messagesFor(user).count { !it.read }

    LazyColumn(contentPadding = PaddingValues(vertical = 10.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                Stat(Modifier.weight(1f), "Properties", "${props.size}", "$live live")
                Stat(Modifier.weight(1f), "Avg occupancy", "${(occ * 100).toInt()}%", "this month")
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                if (user.role == "master")
                    Stat(Modifier.weight(1f), "Mgmt fees (Jun)", inr(mgmt), "earned")
                else
                    Stat(Modifier.weight(1f), "Your earnings (Jun)", inr(ownerEarn), "owner share")
                Stat(Modifier.weight(1f), "Active bookings", "${bookings.count { it.status != "pending" }}", "confirmed")
            }
            if (unreadMsgs > 0) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                    Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Chat, null, Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("$unreadMsgs unread guest message${if (unreadMsgs > 1) "s" else ""}",
                                fontSize = 13.sp, color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            Section("Recent activity") {}
        }
        items(ResidioRepository.activity) { a ->
            Card {
                Text(a.text, fontSize = 14.sp)
                Text(a.ts.replace("T", "  ·  "), fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun Stat(modifier: Modifier, label: String, value: String, sub: String) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp, modifier = modifier.padding(6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(sub, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun PropertiesTab(user: User) {
    val props = ResidioRepository.propertiesFor(user)
    LazyColumn(contentPadding = PaddingValues(vertical = 10.dp)) {
        items(props) { p ->
            Card {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(p.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Pill(p.status)
                }
                Spacer(Modifier.height(4.dp))
                Text("${p.type} · ${p.city} · ${p.bedrooms}BR ${p.bathrooms}BA · ${p.maxGuests} guests",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Mini("ADR", inr(p.adr))
                    Mini("Occupancy", "${(p.occupancy * 100).toInt()}%")
                    Mini("Rating", if (p.rating > 0) "★ ${p.rating}" else "—")
                }
            }
        }
    }
}

@Composable
private fun PayoutsTab(user: User) {
    val stmts = ResidioRepository.statementsFor(user)
    LazyColumn(contentPadding = PaddingValues(vertical = 10.dp)) {
        items(stmts) { s ->
            val prop = ResidioRepository.properties.firstOrNull { it.id == s.propertyId }
            Card {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(prop?.name ?: s.propertyId, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Pill(if (s.paid) "paid" else "due")
                }
                Spacer(Modifier.height(8.dp))
                StatementRow("Gross", inr(s.gross))
                StatementRow("Owner share", inr(s.ownerShare))
                StatementRow("Mgmt fee", inr(s.mgmtFee))
                StatementRow("GST", inr(s.gst))
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                StatementRow("Payout date", s.payoutDate, bold = true)
            }
        }
    }
}

@Composable
private fun BookingsTab(user: User) {
    val bk = ResidioRepository.bookingsFor(user)
    LazyColumn(contentPadding = PaddingValues(vertical = 10.dp)) {
        items(bk) { b ->
            val prop = ResidioRepository.properties.firstOrNull { it.id == b.propertyId }
            Card {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(b.guest, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Pill(b.status)
                }
                Text("${prop?.name ?: b.propertyId} · ${b.platform}", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Mini("Check-in", b.checkin)
                    Mini("Nights", "${b.nights}")
                    Mini("Gross", inr(b.gross))
                }
                if (b.specialRequests.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text("Note: ${b.specialRequests}", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun TasksTab(user: User) {
    val tasks = ResidioRepository.tasksFor(user)
    LazyColumn(contentPadding = PaddingValues(vertical = 10.dp)) {
        items(tasks) { t ->
            val prop = ResidioRepository.properties.firstOrNull { it.id == t.propertyId }
            Card {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(t.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Pill(t.priority)
                }
                Text("${prop?.name ?: t.propertyId} · due ${t.due}", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(t.status)
                    Pill(t.type)
                }
            }
        }
    }
}

@Composable
private fun TicketsTab(user: User) {
    val tickets = ResidioRepository.ticketsFor(user)
    LazyColumn(contentPadding = PaddingValues(vertical = 10.dp)) {
        items(tickets) { t ->
            val prop = ResidioRepository.properties.firstOrNull { it.id == t.propertyId }
            Card {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(t.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Pill(t.status)
                }
                Text("${prop?.name ?: t.propertyId} · ${t.category} · ${inr(t.cost)}", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                if (t.vendorName.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Vendor: ${t.vendorName}", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun ExpensesTab(user: User) {
    val expenses = ResidioRepository.expensesFor(user)
    val total = expenses.sumOf { it.amount }
    LazyColumn(contentPadding = PaddingValues(vertical = 10.dp)) {
        item {
            Card {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Expenses", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(inr(total), fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary)
                }
                Text("${expenses.size} items · ${expenses.count { it.approved }} approved",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            }
        }
        items(expenses) { e ->
            val prop = ResidioRepository.properties.firstOrNull { it.id == e.propertyId }
            Card {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(e.description, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        modifier = Modifier.weight(1f))
                    Text(inr(e.amount), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text("${prop?.name ?: e.propertyId} · ${e.category} · ${e.date}", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(if (e.approved) "approved" else "pending")
                    if (e.vendorName.isNotEmpty()) Pill(e.vendorName)
                }
            }
        }
    }
}

@Composable
private fun Mini(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
    }
}

@Composable
private fun StatementRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        Text(value, fontSize = 13.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun Pill(text: String) {
    Surface(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50)) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
    }
}

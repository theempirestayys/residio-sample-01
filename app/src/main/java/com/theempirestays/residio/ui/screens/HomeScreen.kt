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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(user: User, onLogout: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    val tabs = remember(user.role) {
        when (user.role) {
            "master" -> listOf("Overview", "Properties", "Payouts", "Ops")
            "owner" -> listOf("Overview", "Properties", "Payouts", "Bookings")
            else -> listOf("Today", "Tasks", "Tickets")
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
                    Box(Modifier.padding(end = 8.dp).size(36.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary), contentAlignment = Alignment.Center) {
                        Text(user.initials, color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onLogout) { Icon(Icons.Filled.Logout, "Log out") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, label ->
                    NavigationBarItem(
                        selected = tab == i, onClick = { tab = i },
                        icon = { Icon(iconFor(label), label) },
                        label = { Text(label, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (tabs[tab]) {
                "Overview", "Today" -> OverviewTab(user)
                "Properties" -> PropertiesTab(user)
                "Payouts" -> PayoutsTab(user)
                "Bookings" -> BookingsTab(user)
                "Ops", "Tasks" -> TasksTab(user)
                "Tickets" -> TicketsTab(user)
            }
        }
    }
}

private fun iconFor(label: String): ImageVector = when (label) {
    "Overview", "Today" -> Icons.Filled.Dashboard
    "Properties" -> Icons.Filled.Apartment
    "Payouts" -> Icons.Filled.Payments
    "Bookings" -> Icons.Filled.EventAvailable
    "Ops", "Tasks" -> Icons.Filled.Checklist
    "Tickets" -> Icons.Filled.Build
    else -> Icons.Filled.Circle
}

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
        }
        item { Section("Recent activity") {} }
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
    Surface(
        color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp, modifier = modifier.padding(6.dp)
    ) {
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
                Spacer(Modifier.height(6.dp))
                Text("${p.type} · ${p.city}", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
                Divider(Modifier.padding(vertical = 6.dp))
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
                Pill(t.status)
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
    Surface(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
    }
}

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theempirestays.residio.data.Guest
import com.theempirestays.residio.data.GuestMessage
import com.theempirestays.residio.data.ResidioRepository
import com.theempirestays.residio.data.User

@Composable
fun GuestsScreen(user: User) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Inbox", "Guests", "Verification")
    val messages = ResidioRepository.messagesFor(user)
    val unread = messages.count { !it.read }
    val todayBookings = ResidioRepository.bookingsFor(user).count { it.checkin == "2026-07-01" || it.checkin == "Jul 1" }
    val pendingId = ResidioRepository.guests.count { !it.idVerified }

    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                GuestStat(Modifier.weight(1f), "Unread", "$unread", "messages")
                GuestStat(Modifier.weight(1f), "Check-ins", "$todayBookings", "today")
                GuestStat(Modifier.weight(1f), "Pending ID", "$pendingId", "to verify")
            }
            Spacer(Modifier.height(4.dp))
            TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i },
                        text = { Text(label, fontSize = 12.sp) })
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        when (tabs[tab]) {
            "Inbox" -> {
                val sorted = messages.sortedByDescending { it.ts }
                items(sorted) { msg ->
                    val guest = ResidioRepository.guests.firstOrNull { it.id == msg.guestId }
                    MessageCard(msg, guest)
                }
                if (sorted.isEmpty()) item { EmptyState("No messages yet") }
            }
            "Guests" -> {
                items(ResidioRepository.guests) { g ->
                    GuestCard(g)
                }
            }
            "Verification" -> {
                val pending = ResidioRepository.guests.filter { !it.idVerified }
                items(pending) { g -> VerificationCard(g) }
                if (pending.isEmpty()) item { EmptyState("All guests verified ✓") }
            }
        }
    }
}

@Composable
private fun MessageCard(msg: GuestMessage, guest: Guest?) {
    val channelColor = when (msg.channel) {
        "Airbnb" -> Color(0xFFFF5A5F)
        "Booking.com" -> Color(0xFF003580)
        "VRBO" -> Color(0xFF4251A3)
        "Direct" -> Color(0xFF2E7D32)
        else -> Color.Gray
    }
    Surface(
        color = if (!msg.read) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center) {
                Text(guest?.name?.take(2)?.uppercase() ?: "??",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(guest?.name ?: "Guest", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(msg.ts.takeLast(8).take(5), fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = channelColor.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp)) {
                        Text(msg.channel, fontSize = 9.sp, color = channelColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(if (msg.direction == "inbound") "← Guest" else "→ You",
                        fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
                Spacer(Modifier.height(3.dp))
                Text(msg.text, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (!msg.read) 0.9f else 0.65f))
            }
            if (!msg.read) {
                Spacer(Modifier.width(6.dp))
                Box(Modifier.size(8.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary))
            }
        }
    }
}

@Composable
private fun GuestCard(g: Guest) {
    val flagMap = mapOf("Indian" to "🇮🇳","British" to "🇬🇧","Japanese" to "🇯🇵",
        "French" to "🇫🇷","Mexican" to "🇲🇽","German" to "🇩🇪","American" to "🇺🇸","Australian" to "🇦🇺")
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape)
                .background(if (g.vipStatus) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) {
                Text(g.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                    fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = if (g.vipStatus) MaterialTheme.colorScheme.onSecondary
                            else MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(g.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(flagMap[g.nationality] ?: "🌍", fontSize = 14.sp)
                    if (g.vipStatus) {
                        Spacer(Modifier.width(4.dp))
                        Text("⭐ VIP", fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                }
                Text("${g.totalStays} stays · ★ ${g.averageRating}", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(g.preferredLanguage, fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
            }
            Column(horizontalAlignment = Alignment.End) {
                if (g.idVerified) {
                    Surface(color = Color(0xFF2E7D32).copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                        Text("ID ✓", fontSize = 9.sp, color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                } else {
                    Surface(color = Color(0xFFE65100).copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                        Text("Pending", fontSize = 9.sp, color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationCard(g: Guest) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(g.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Surface(color = Color(0xFFE65100).copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
                    Text("Unverified", fontSize = 9.sp, color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("${g.idType}  ·  ${g.idNumber.take(4)}••••••••", fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
            Text(g.email, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.VerifiedUser, null, Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Verify Identity", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun GuestStat(modifier: Modifier, label: String, value: String, sub: String) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp, modifier = modifier.padding(6.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(sub, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }
}

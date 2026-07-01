package com.theempirestays.residio.ui.screens

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
import com.theempirestays.residio.data.OtaChannel
import com.theempirestays.residio.data.ResidioRepository
import com.theempirestays.residio.data.User

@Composable
fun ChannelScreen(user: User) {
    val channels = ResidioRepository.channels
    val connected = channels.count { it.connected }
    val synced = channels.count { it.connected && it.syncStatus == "ok" }
    val pending = channels.filter { it.connected }.sumOf { it.pendingBookings }

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            // Summary
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
                ChStat(Modifier.weight(1f), "Connected", "$connected", "platforms")
                ChStat(Modifier.weight(1f), "Synced", "$synced", "today")
                ChStat(Modifier.weight(1f), "Pending", "$pending", "bookings")
            }

            Text("OTA Channels",
                fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        items(channels) { ch -> ChannelCard(ch) }

        item {
            Spacer(Modifier.height(8.dp))
            Text("iCal Export Links",
                fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            listOf("Airbnb", "Booking.com", "VRBO").forEach { platform ->
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(platform, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("webcal://residio.app/ical/prop-xxx/${platform.lowercase().replace(".","")}.ics",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(onClick = {}, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Icon(Icons.Filled.ContentCopy, null, Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copy", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Sync Log — Today",
                fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            listOf(
                Triple("Airbnb", "2 new bookings synced", "08:00"),
                Triple("Booking.com", "Availability updated", "08:05"),
                Triple("VRBO", "No changes detected", "07:45"),
                Triple("Expedia", "Price update applied", "07:50")
            ).forEach { (channel, event, time) ->
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp)) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null,
                            Modifier.size(16.dp), tint = Color(0xFF2E7D32))
                        Spacer(Modifier.width(8.dp))
                        Text("$channel: $event", fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(time, fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelCard(ch: OtaChannel) {
    val statusColor = when {
        !ch.connected -> Color.Gray
        ch.syncStatus == "ok" -> Color(0xFF2E7D32)
        ch.syncStatus == "warning" -> Color(0xFFE65100)
        else -> Color.Gray
    }

    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape)) {
                        Surface(color = statusColor, modifier = Modifier.fillMaxSize()) {}
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(ch.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Surface(
                    color = when {
                        !ch.connected -> Color.Gray.copy(alpha = 0.1f)
                        ch.syncStatus == "ok" -> Color(0xFF2E7D32).copy(alpha = 0.1f)
                        else -> Color(0xFFE65100).copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        when {
                            !ch.connected && ch.syncStatus == "not set up" -> "Not Set Up"
                            !ch.connected -> "Disconnected"
                            else -> "Connected"
                        },
                        fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            if (ch.connected) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ChMini("Listings", "${ch.activeListings}")
                    ChMini("Pending", "${ch.pendingBookings}")
                    ChMini("Last Sync", ch.lastSync.takeLast(5))
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp)) {
                        Icon(Icons.Filled.Sync, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp)) {
                        Icon(Icons.Filled.OpenInNew, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Listings", fontSize = 11.sp)
                    }
                }
            } else {
                Spacer(Modifier.height(10.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)) {
                    Icon(Icons.Filled.Add, null, Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Connect ${ch.name}", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ChStat(modifier: Modifier, label: String, value: String, sub: String) {
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
private fun ChMini(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

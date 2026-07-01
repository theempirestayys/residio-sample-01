package com.theempirestays.residio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theempirestays.residio.data.ResidioRepository
import com.theempirestays.residio.data.SmartLock
import com.theempirestays.residio.data.User

@Composable
fun SmartLockScreen(user: User) {
    val locks = ResidioRepository.smartLocksFor(user)
    val online = locks.count { it.status == "online" }
    val lowBattery = locks.count { it.battery < 20 }

    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
                LockStat(Modifier.weight(1f), "Total Locks", "${locks.size}", "properties")
                LockStat(Modifier.weight(1f), "Online", "$online", "connected")
                LockStat(Modifier.weight(1f), "Low Battery", "$lowBattery", "need attention")
            }
            Text("Smart Locks", fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        items(locks) { lock -> LockCard(lock) }

        item {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp)
            ) {
                Icon(Icons.Filled.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Generate New Access Code", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun LockCard(lock: SmartLock) {
    val prop = ResidioRepository.properties.firstOrNull { it.id == lock.propertyId }
    var expanded by remember { mutableStateOf(false) }
    var remoteUnlocked by remember { mutableStateOf(false) }

    val batteryColor = when {
        lock.battery < 20 -> Color(0xFFB71C1C)
        lock.battery < 50 -> Color(0xFFE65100)
        else -> Color(0xFF2E7D32)
    }

    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(prop?.name ?: lock.propertyId, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("${lock.brand} ${lock.model}", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                }
                Surface(
                    color = if (lock.status == "online") Color(0xFF2E7D32).copy(alpha = 0.1f)
                            else Color(0xFFB71C1C).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (lock.status == "online") Icons.Filled.Wifi else Icons.Filled.WifiOff,
                            null, Modifier.size(12.dp),
                            tint = if (lock.status == "online") Color(0xFF2E7D32) else Color(0xFFB71C1C)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(lock.status.replaceFirstChar { it.uppercase() }, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (lock.status == "online") Color(0xFF2E7D32) else Color(0xFFB71C1C))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Battery
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Battery5Bar, null, Modifier.size(16.dp), tint = batteryColor)
                Spacer(Modifier.width(6.dp))
                LinearProgressIndicator(
                    progress = { lock.battery / 100f },
                    modifier = Modifier.weight(1f).height(6.dp),
                    color = batteryColor,
                    trackColor = batteryColor.copy(alpha = 0.1f)
                )
                Spacer(Modifier.width(8.dp))
                Text("${lock.battery}%", fontSize = 11.sp, color = batteryColor,
                    fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            // Access code
            if (lock.status == "online" && lock.currentCode.isNotEmpty() && lock.currentCode != "—") {
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(10.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Current Access Code", fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                            Text(lock.currentCode, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 4.sp)
                            if (lock.expiresAt != null) {
                                Text("Expires: ${lock.expiresAt}", fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                            }
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.ContentCopy, "Copy code",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            } else {
                Text("No active booking — lock secured", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))
            }

            // Remote lock/unlock toggle
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (remoteUnlocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                        null, Modifier.size(18.dp),
                        tint = if (remoteUnlocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (remoteUnlocked) "Remotely Unlocked" else "Locked",
                        fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Switch(checked = remoteUnlocked, onCheckedChange = { remoteUnlocked = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2E7D32),
                        checkedTrackColor = Color(0xFF2E7D32).copy(alpha = 0.3f)))
            }

            // Access log expandable
            if (lock.accessLog.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
                    Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (expanded) "Hide Access Log" else "Show Access Log", fontSize = 11.sp)
                }
                if (expanded) {
                    lock.accessLog.take(3).forEach { entry ->
                        Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.History, null, Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            Spacer(Modifier.width(6.dp))
                            Text(entry, fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LockStat(modifier: Modifier, label: String, value: String, sub: String) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp, modifier = modifier.padding(6.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(sub, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

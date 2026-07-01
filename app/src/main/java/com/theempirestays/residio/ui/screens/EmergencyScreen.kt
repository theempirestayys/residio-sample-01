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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theempirestays.residio.data.EmergencyContact
import com.theempirestays.residio.data.ResidioRepository
import com.theempirestays.residio.data.User

@Composable
fun EmergencyScreen(user: User) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Services", "Maintenance", "Medical")
    val contacts = ResidioRepository.emergencyContacts

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            // Emergency banner
            Surface(color = Color(0xFFB71C1C), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, Modifier.size(20.dp), tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Emergency? Call Now", fontSize = 15.sp,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Icon(Icons.Filled.Call, null, Modifier.size(16.dp), tint = Color(0xFFB71C1C))
                            Spacer(Modifier.width(6.dp))
                            Text("POLICE 100", fontSize = 13.sp,
                                fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                        }
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Icon(Icons.Filled.LocalHospital, null, Modifier.size(16.dp),
                                tint = Color(0xFFB71C1C))
                            Spacer(Modifier.width(6.dp))
                            Text("AMBULANCE 108", fontSize = 11.sp,
                                fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                        }
                    }
                }
            }

            // Tabs
            TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i },
                        text = { Text(label, fontSize = 12.sp) })
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        val filtered = when (tabs[tab]) {
            "Services" -> contacts.filter { it.role == "Emergency Services" }
            "Maintenance" -> contacts.filter {
                it.role in listOf("Maintenance","Security","Facility")
            }
            "Medical" -> contacts.filter { it.role == "Medical" }
            else -> contacts
        }

        items(filtered) { contact -> EmergencyContactCard(contact) }

        item {
            Spacer(Modifier.height(12.dp))
            Text("Quick Actions", fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple("Nearest Hospital", Icons.Filled.LocalHospital, Color(0xFF1565C0)),
                    Triple("Assembly Point", Icons.Filled.MyLocation, Color(0xFF2E7D32)),
                    Triple("Insurance", Icons.Filled.HealthAndSafety, Color(0xFF6A1B9A))
                ).forEach { (label, icon, color) ->
                    Surface(color = color.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(icon, null, Modifier.size(22.dp), tint = color)
                            Spacer(Modifier.height(4.dp))
                            Text(label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                                color = color, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(Modifier.height(6.dp))
                            OutlinedButton(onClick = {}, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 3.dp),
                                modifier = Modifier.fillMaxWidth()) {
                                Text("Call", fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EmergencyContactCard(contact: EmergencyContact) {
    val roleColor = when (contact.role) {
        "Emergency Services" -> Color(0xFFB71C1C)
        "Maintenance" -> Color(0xFFE65100)
        "Medical" -> Color(0xFF1565C0)
        "Security" -> Color(0xFF4A148C)
        "Cleaning" -> Color(0xFF006064)
        "Facility" -> Color(0xFF37474F)
        else -> Color.Gray
    }

    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(contact.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        if (contact.available24h) {
                            Surface(color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)) {
                                Text("24/7", fontSize = 8.sp, fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Surface(color = roleColor.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text(contact.role, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            color = roleColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(contact.phone, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    Text("~${contact.responseTimeMinutes} min", fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                }
            }

            if (contact.email.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(contact.email, fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}, modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = roleColor)) {
                    Icon(Icons.Filled.Call, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Call", fontSize = 12.sp)
                }
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp)) {
                    Icon(Icons.Filled.Chat, null, Modifier.size(14.dp),
                        tint = Color(0xFF2E7D32))
                    Spacer(Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 12.sp, color = Color(0xFF2E7D32))
                }
            }
        }
    }
}

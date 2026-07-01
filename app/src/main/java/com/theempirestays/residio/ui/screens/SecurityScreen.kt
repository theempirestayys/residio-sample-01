package com.theempirestays.residio.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theempirestays.residio.data.ResidioRepository
import com.theempirestays.residio.data.User

@Composable
fun SecurityScreen(user: User) {
    var biometric by remember { mutableStateOf(user.biometricEnabled) }
    var twoFa by remember { mutableStateOf(user.twoFaEnabled) }
    var pinLock by remember { mutableStateOf(false) }

    val sessions = ResidioRepository.securitySessions
    val scoreColor = Color(0xFF2E7D32)
    val arcColor = MaterialTheme.colorScheme.primary

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            // Security score
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                        val green = scoreColor
                        Canvas(Modifier.fillMaxSize()) {
                            drawArc(
                                color = green.copy(alpha = 0.1f),
                                startAngle = 135f, sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 10f, cap = StrokeCap.Round),
                                topLeft = Offset(10f, 10f),
                                size = Size(size.width - 20f, size.height - 20f)
                            )
                            drawArc(
                                color = green,
                                startAngle = 135f, sweepAngle = 270f * 0.87f,
                                useCenter = false,
                                style = Stroke(width = 10f, cap = StrokeCap.Round),
                                topLeft = Offset(10f, 10f),
                                size = Size(size.width - 20f, size.height - 20f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("87", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                color = scoreColor)
                            Text("/100", fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Security Score", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Good — 2 recommendations", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(Modifier.height(6.dp))
                        Surface(color = Color(0xFFE65100).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)) {
                            Text("2 items to improve", fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold, color = Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            SecSection("Authentication")
        }

        // Auth toggles
        item {
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column {
                    SecToggleRow("Biometric Login", Icons.Filled.Fingerprint, biometric,
                        "Face ID / Fingerprint") { biometric = it }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    SecToggleRow("Two-Factor Auth (2FA)", Icons.Filled.PhonelinkLock, twoFa,
                        if (twoFa) "Set up via authenticator app" else "Adds extra security") { twoFa = it }
                    if (twoFa) {
                        Surface(color = Color(0xFF1565C0).copy(alpha = 0.06f),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp),
                            shape = RoundedCornerShape(8.dp)) {
                            Text("Use Google Authenticator or Authy to scan your QR code",
                                fontSize = 11.sp,
                                color = Color(0xFF1565C0),
                                modifier = Modifier.padding(10.dp))
                        }
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Google, null, Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Google Account", fontSize = 14.sp)
                            Text(if (user.googleLinked) user.email else "Not linked",
                                fontSize = 11.sp,
                                color = if (user.googleLinked) Color(0xFF2E7D32)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                        }
                        if (!user.googleLinked) {
                            TextButton(onClick = {}) { Text("Link", fontSize = 12.sp) }
                        }
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    SecToggleRow("PIN Lock", Icons.Filled.Pin, pinLock,
                        "4-digit PIN for app access") { pinLock = it }
                }
            }
            Spacer(Modifier.height(4.dp))
            SecSection("Active Sessions")
        }

        items(sessions) { session ->
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (session.deviceName.contains("iPhone") || session.deviceName.contains("iPad"))
                            Icons.Filled.PhoneIphone else Icons.Filled.Laptop,
                        null, Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(session.deviceName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            if (session.current) {
                                Spacer(Modifier.width(6.dp))
                                Surface(color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)) {
                                    Text("This device", fontSize = 8.sp, color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Text("${session.location}  ·  ${session.lastActive}", fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                    }
                    if (!session.current) {
                        TextButton(onClick = {}) {
                            Text("Revoke", fontSize = 11.sp, color = Color(0xFFB71C1C))
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            SecSection("Security Log")
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    listOf(
                        "Login from Mumbai, IN — iPhone 15 Pro — Jul 1, 10:30",
                        "Password changed — Jun 20, 14:00",
                        "New device added — Jun 15, 09:00",
                        "Biometric enabled — Jun 10, 11:30"
                    ).forEachIndexed { i, entry ->
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.History, null, Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            Spacer(Modifier.width(10.dp))
                            Text(entry, fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                        }
                        if (i < 3) HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            SecSection("Recommendations")
            listOf(
                "Enable Two-Factor Authentication for stronger account protection",
                "Verify your backup email address to recover access if needed"
            ).forEach { rec ->
                Surface(color = Color(0xFFE65100).copy(alpha = 0.06f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Warning, null, Modifier.size(18.dp),
                            tint = Color(0xFFE65100))
                        Spacer(Modifier.width(10.dp))
                        Text(rec, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SecToggleRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    subtitle: String,
    onToggle: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun SecSection(title: String) {
    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
}

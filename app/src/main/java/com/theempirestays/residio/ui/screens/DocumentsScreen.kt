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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theempirestays.residio.data.LegalDocument
import com.theempirestays.residio.data.ResidioRepository
import com.theempirestays.residio.data.User

@Composable
fun DocumentsScreen(user: User) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Agreements", "Policies", "Compliance")
    val docs = ResidioRepository.documents
    val pending = docs.count { it.requiresSignature && !it.signedByGuest }
    val signed = docs.count { it.signedByGuest }

    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
                DocStat(Modifier.weight(1f), "Total", "${docs.size}", "documents")
                DocStat(Modifier.weight(1f), "Pending", "$pending", "signatures")
                DocStat(Modifier.weight(1f), "Signed", "$signed", "by guests")
            }
            TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i },
                        text = { Text(label, fontSize = 12.sp) })
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        val filtered = when (tabs[tab]) {
            "Agreements" -> docs.filter { it.type in listOf("rental_agreement","noc") }
            "Policies" -> docs.filter { it.type in listOf("privacy_policy","terms","house_rules","refund") }
            "Compliance" -> docs.filter { it.type in listOf("id_verification","maintenance","emergency") }
            else -> docs
        }

        items(filtered) { doc ->
            val prop = if (doc.propertyId != null && doc.propertyId != "null")
                ResidioRepository.properties.firstOrNull { it.id == doc.propertyId } else null

            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Icon(docIcon(doc.type), null, Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(doc.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(4.dp)) {
                                    Text(doc.version, fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                }
                                Spacer(Modifier.width(6.dp))
                                Text("Updated ${doc.updatedAt}", fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                            }
                            if (prop != null) {
                                Text(prop.name, fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        if (doc.requiresSignature) {
                            Surface(
                                color = if (doc.signedByGuest) Color(0xFF2E7D32).copy(alpha = 0.1f)
                                        else Color(0xFFE65100).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    if (doc.signedByGuest) "Signed ✓" else "Pending",
                                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                    color = if (doc.signedByGuest) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 5.dp)) {
                            Icon(Icons.Filled.Visibility, null, Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("View", fontSize = 11.sp)
                        }
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 5.dp)) {
                            Icon(Icons.Filled.Download, null, Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Download", fontSize = 11.sp)
                        }
                        if (doc.requiresSignature && !doc.signedByGuest) {
                            Button(onClick = {}, modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 5.dp)) {
                                Icon(Icons.Filled.Draw, null, Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Sign", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = {},
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp)) {
                Icon(Icons.Filled.Upload, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Upload Document", fontSize = 14.sp)
            }
        }
    }
}

private fun docIcon(type: String): ImageVector = when (type) {
    "rental_agreement" -> Icons.Filled.Description
    "house_rules" -> Icons.Filled.Home
    "privacy_policy" -> Icons.Filled.Security
    "terms" -> Icons.Filled.Gavel
    "id_verification" -> Icons.Filled.Badge
    "noc" -> Icons.Filled.Assignment
    "maintenance" -> Icons.Filled.Build
    "refund" -> Icons.Filled.CurrencyRupee
    "emergency" -> Icons.Filled.Emergency
    else -> Icons.Filled.Article
}

@Composable
private fun DocStat(modifier: Modifier, label: String, value: String, sub: String) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp, modifier = modifier.padding(6.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(sub, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

package com.theempirestays.residio.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theempirestays.residio.data.ResidioRepository
import com.theempirestays.residio.data.User
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnalyticsScreen(user: User) {
    val metrics = ResidioRepository.revenueMetrics
    val props = ResidioRepository.propertiesFor(user)
    val rupee = NumberFormat.getNumberInstance(Locale("en", "IN"))
    fun inr(v: Int) = "₹" + rupee.format(v)
    val latest = metrics.lastOrNull()

    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)) {
        item {
            // KPI Grid
            Row(Modifier.fillMaxWidth()) {
                AnStat(Modifier.weight(1f), "RevPAR", "₹${latest?.revpar?.toInt() ?: 0}", "Jun 2026")
                AnStat(Modifier.weight(1f), "ADR", "₹${latest?.adr?.toInt() ?: 0}", "avg daily rate")
            }
            Row(Modifier.fillMaxWidth()) {
                AnStat(Modifier.weight(1f), "Occupancy", "${((latest?.occupancy ?: 0.0) * 100).toInt()}%", "this month")
                AnStat(Modifier.weight(1f), "Net Revenue", inr(latest?.netRevenue ?: 0), "after expenses")
            }
            Spacer(Modifier.height(8.dp))

            // Revenue bar chart
            AnSection("Revenue Trend — 2026")
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp),
                shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    val maxGross = metrics.maxOf { it.gross }.toFloat()
                    val barColor = MaterialTheme.colorScheme.primary
                    val goldColor = MaterialTheme.colorScheme.secondary
                    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)

                    Canvas(Modifier.fillMaxWidth().height(140.dp)) {
                        val count = metrics.size
                        val barW = (size.width - 16f) / (count * 2 - 1)
                        val maxH = size.height - 40f
                        metrics.forEachIndexed { i, m ->
                            val x = i * barW * 2
                            val h = (m.gross.toFloat() / maxGross) * maxH
                            val isLast = i == count - 1
                            drawRect(
                                color = if (isLast) goldColor else barColor.copy(alpha = 0.7f),
                                topLeft = Offset(x, size.height - h - 24f),
                                size = Size(barW, h)
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                m.month.take(3),
                                x + barW / 2,
                                size.height - 4f,
                                android.graphics.Paint().apply {
                                    textSize = 24f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    color = labelColor.copy(alpha = 0.7f).toArgb()
                                }
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        metrics.forEach { m ->
                            Text("₹${m.gross / 1000}k", fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Occupancy by property
            AnSection("Occupancy by Property")
        }

        items(props) { p ->
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(p.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f))
                        Text("${(p.occupancy * 100).toInt()}%", fontSize = 13.sp,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { p.occupancy.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp).padding(end = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("ADR ₹${p.adr}  ·  ★ ${p.rating}  ·  ${p.city}",
                        fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))

            // Platform mix
            AnSection("Booking Sources")
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp),
                shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    listOf(
                        Triple("Airbnb", 0.42f, Color(0xFFFF5A5F)),
                        Triple("Booking.com", 0.28f, Color(0xFF003580)),
                        Triple("VRBO", 0.15f, Color(0xFF4251A3)),
                        Triple("Direct", 0.15f, Color(0xFF2E7D32))
                    ).forEach { (name, pct, color) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(name, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier.weight(1f).height(8.dp),
                                color = color,
                                trackColor = color.copy(alpha = 0.1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("${(pct * 100).toInt()}%", fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold, color = color)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Competitor intelligence — master only
            if (user.role == "master") {
                AnSection("Market Comparison")
                val ourAdr = props.map { it.adr }.average().toInt()
                ResidioRepository.competitors.forEach { c ->
                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(c.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("${c.platform}  ·  ★ ${c.rating}  ·  ${c.reviewCount} reviews  ·  ${c.distance}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${c.adr}", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                    color = if (ourAdr >= c.adr) Color(0xFF2E7D32) else Color(0xFFB71C1C))
                                Text(if (ourAdr >= c.adr) "We compete ✓" else "They're cheaper",
                                    fontSize = 9.sp,
                                    color = if (ourAdr >= c.adr) Color(0xFF2E7D32) else Color(0xFFB71C1C))
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
private fun AnStat(modifier: Modifier, label: String, value: String, sub: String) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp, modifier = modifier.padding(4.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(sub, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun AnSection(title: String) {
    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        modifier = Modifier.padding(vertical = 8.dp))
}

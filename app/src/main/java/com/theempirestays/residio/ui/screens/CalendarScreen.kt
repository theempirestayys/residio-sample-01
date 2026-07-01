package com.theempirestays.residio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theempirestays.residio.data.Booking
import com.theempirestays.residio.data.ResidioRepository
import com.theempirestays.residio.data.User
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CalendarScreen(user: User) {
    val bookings = ResidioRepository.bookingsFor(user)
    val rupee = NumberFormat.getNumberInstance(Locale("en", "IN"))
    fun inr(v: Int) = "₹" + rupee.format(v)

    val avgOccupancy = ResidioRepository.propertiesFor(user)
        .map { it.occupancy }.let { if (it.isEmpty()) 0.0 else it.average() }

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            // Month header
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.ChevronLeft, "Previous month")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("July 2026", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${bookings.size} bookings", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.ChevronRight, "Next month")
                    }
                }
            }

            // Day headers
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat").forEach { day ->
                    Text(day, modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center, fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                }
            }

            // Calendar grid — July 2026 starts on Wednesday (offset 3)
            val offset = 3
            val daysInMonth = 31
            val totalCells = ((daysInMonth + offset + 6) / 7) * 7

            val bookedDays = mutableMapOf<Int, String>()
            bookings.forEach { b ->
                val raw = b.checkin
                val day = raw.filter { it.isDigit() }.toIntOrNull() ?: 0
                if (day in 1..31) bookedDays[day] = b.status
            }

            Column(Modifier.padding(horizontal = 8.dp)) {
                for (week in 0 until (totalCells / 7)) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        for (col in 0..6) {
                            val cellIndex = week * 7 + col
                            val day = cellIndex - offset + 1
                            val isToday = day == 1
                            val bookingStatus = bookedDays[day]

                            Box(
                                Modifier.weight(1f).aspectRatio(1f).padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day in 1..daysInMonth) {
                                    Box(
                                        Modifier.fillMaxSize().clip(
                                            if (isToday) CircleShape else RoundedCornerShape(8.dp)
                                        ).background(
                                            when {
                                                isToday -> MaterialTheme.colorScheme.primary
                                                bookingStatus != null -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                else -> Color.Transparent
                                            }
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "$day",
                                                fontSize = 13.sp,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isToday -> MaterialTheme.colorScheme.onPrimary
                                                    day > daysInMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                            if (bookingStatus != null) {
                                                Box(
                                                    Modifier.size(5.dp).clip(CircleShape).background(
                                                        when (bookingStatus) {
                                                            "confirmed" -> Color(0xFF2E7D32)
                                                            "pending" -> MaterialTheme.colorScheme.secondary
                                                            else -> Color.Gray
                                                        }
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Occupancy chip
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "Avg Occupancy: ${(avgOccupancy * 100).toInt()}%",
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            // This month bookings
            Text("This Month — Bookings",
                fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        items(bookings) { b ->
            val prop = ResidioRepository.properties.firstOrNull { it.id == b.propertyId }
            val channelColor = when (b.platform) {
                "Airbnb" -> Color(0xFFFF5A5F)
                "Booking.com" -> Color(0xFF003580)
                "VRBO" -> Color(0xFF4251A3)
                else -> Color(0xFF2E7D32)
            }
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(3.dp).height(40.dp).clip(RoundedCornerShape(2.dp))
                        .background(channelColor))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(b.guest, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${prop?.name ?: b.propertyId} · ${b.platform}", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                        Text("${b.checkin} → ${b.checkout}  ·  ${b.nights} nights",
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${NumberFormat.getNumberInstance(Locale("en","IN")).format(b.gross)}",
                            fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        CalPill(b.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalPill(status: String) {
    val color = when (status) {
        "confirmed" -> Color(0xFF2E7D32)
        "pending" -> Color(0xFFE65100)
        else -> Color.Gray
    }
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(20.dp)) {
        Text(status, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theempirestays.residio.data.ResidioRepository
import com.theempirestays.residio.data.Review
import com.theempirestays.residio.data.User

@Composable
fun ReviewsScreen(user: User) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Unresponded", "By Property")
    val reviews = ResidioRepository.reviewsFor(user)

    val avgRating = if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
    val unresponded = reviews.count { !it.responded }
    val responseRate = if (reviews.isEmpty()) 0 else
        ((reviews.count { it.responded }.toFloat() / reviews.size) * 100).toInt()

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            // Summary card
            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("★ ${"%.1f".format(avgRating)}", fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary)
                        Text("avg rating", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("$responseRate%", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary)
                        Text("response rate", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                        if (unresponded > 0) {
                            Spacer(Modifier.height(4.dp))
                            Surface(color = Color(0xFFE65100), shape = RoundedCornerShape(8.dp)) {
                                Text("$unresponded unresponded", fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold, color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
            }

            TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i },
                        text = { Text(label, fontSize = 12.sp) })
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        when (tabs[tab]) {
            "All" -> items(reviews.sortedByDescending { it.date }) { r -> ReviewCard(r) }
            "Unresponded" -> {
                val list = reviews.filter { !it.responded }
                if (list.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("All reviews have been responded to ✓", fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }
                    }
                } else items(list) { r -> ReviewCard(r) }
            }
            "By Property" -> {
                val grouped = reviews.groupBy { it.propertyId }
                grouped.forEach { (propId, propReviews) ->
                    val prop = ResidioRepository.properties.firstOrNull { it.id == propId }
                    item {
                        Text(prop?.name ?: propId,
                            fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                    items(propReviews) { r -> ReviewCard(r) }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    val platformColor = when (review.platform) {
        "Airbnb" -> Color(0xFFFF5A5F)
        "Booking.com" -> Color(0xFF003580)
        "VRBO" -> Color(0xFF4251A3)
        "Direct" -> Color(0xFF2E7D32)
        else -> Color.Gray
    }

    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top) {
                Column {
                    Text(review.guestName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(review.date, fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                }
                Surface(color = platformColor.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                    Text(review.platform, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = platformColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Star rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Text(
                        if (i < review.rating.toInt()) "★" else "☆",
                        fontSize = 18.sp,
                        color = if (i < review.rating.toInt()) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("${"%.1f".format(review.rating)}", fontSize = 14.sp,
                    fontWeight = FontWeight.Bold)
            }

            // Sub-ratings
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "Clean" to review.cleanliness,
                    "Comms" to review.communication,
                    "Location" to review.location,
                    "Accuracy" to review.accuracy
                ).forEach { (label, score) ->
                    Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(6.dp)) {
                        Column(Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${"%.0f".format(score)}", fontSize = 12.sp,
                                fontWeight = FontWeight.Bold)
                            Text(label, fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Review text
            Text(review.comment, fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 4, overflow = TextOverflow.Ellipsis)

            // Owner reply
            if (review.ownerReply != null) {
                Spacer(Modifier.height(8.dp))
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp)) {
                    Column(Modifier.padding(10.dp)) {
                        Text("Your reply:", fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(3.dp))
                        Text(review.ownerReply, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                    }
                }
            }

            // Action buttons for unresponded
            if (!review.responded) {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp)) {
                        Icon(Icons.Filled.Reply, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reply", fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp)) {
                        Icon(Icons.Filled.AutoAwesome, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("AI Draft", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

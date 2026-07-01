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
import com.theempirestays.residio.data.CleaningJob
import com.theempirestays.residio.data.ResidioRepository
import com.theempirestays.residio.data.User

@Composable
fun CleaningScreen(user: User) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Today", "Upcoming", "Completed")
    val allJobs = ResidioRepository.cleaningFor(user)

    val today = allJobs.filter { it.scheduledAt.contains("2026-07-01") }
    val upcoming = allJobs.filter { it.scheduledAt > "2026-07-01" && it.status != "completed" }
    val completed = allJobs.filter { it.status == "completed" }

    val inProgress = today.count { it.status == "in_progress" }
    val doneToday = today.count { it.status == "completed" }

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
                CleanStat(Modifier.weight(1f), "Today", "${today.size}", "jobs")
                CleanStat(Modifier.weight(1f), "In Progress", "$inProgress", "active")
                CleanStat(Modifier.weight(1f), "Done", "$doneToday", "today")
            }
            TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i },
                        text = { Text(label, fontSize = 12.sp) })
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        val displayJobs = when (tabs[tab]) {
            "Today" -> today
            "Upcoming" -> upcoming
            else -> completed
        }

        if (displayJobs.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No jobs ${tabs[tab].lowercase()}", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
        } else {
            items(displayJobs) { job -> CleaningJobCard(job) }
        }
    }
}

@Composable
private fun CleaningJobCard(job: CleaningJob) {
    val prop = ResidioRepository.properties.firstOrNull { it.id == job.propertyId }
    var expanded by remember { mutableStateOf(job.status == "in_progress") }

    val statusColor = when (job.status) {
        "in_progress" -> Color(0xFFE65100)
        "completed" -> Color(0xFF2E7D32)
        else -> Color(0xFF1565C0)
    }
    val doneItems = job.checklist.count { it.done }
    val totalItems = job.checklist.size

    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(prop?.name ?: job.propertyId,
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(job.staffName, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("${job.scheduledAt.take(16)}  ·  ~${job.estimatedMinutes} min", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                }
                Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(job.status.replace("_", " ").replaceFirstChar { it.uppercase() },
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            if (job.notes.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(job.notes, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            }

            if (job.checklist.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { if (totalItems > 0) doneItems.toFloat() / totalItems else 0f },
                        modifier = Modifier.weight(1f).height(6.dp),
                        color = Color(0xFF2E7D32),
                        trackColor = Color(0xFF2E7D32).copy(alpha = 0.1f)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("$doneItems/$totalItems", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32))
                }

                Spacer(Modifier.height(6.dp))
                TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
                    Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (expanded) "Hide Checklist" else "View Checklist ($totalItems tasks)",
                        fontSize = 11.sp)
                }

                if (expanded) {
                    val grouped = job.checklist.groupBy { it.room }
                    grouped.forEach { (room, items) ->
                        Text(room, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 4.dp))
                        items.forEach { item ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (item.done) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                    null, Modifier.size(18.dp),
                                    tint = if (item.done) Color(0xFF2E7D32)
                                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(item.task, fontSize = 12.sp,
                                    color = if (item.done) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            if (job.status == "in_progress") {
                Spacer(Modifier.height(12.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                    Icon(Icons.Filled.CheckCircle, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Mark Complete", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun CleanStat(modifier: Modifier, label: String, value: String, sub: String) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp, modifier = modifier.padding(6.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(sub, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AlarmEntity
import com.example.model.AlarmTimeHelper
import com.example.model.ChallengeType
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.SunriseAmber
import com.example.ui.theme.SunriseGold
import com.example.ui.theme.SunriseOrange
import com.example.ui.theme.SunriseOrangeBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: AlarmViewModel,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var isPracticeHubOpen by remember { mutableStateOf(false) }
    var alarmToDelete by remember { mutableStateOf<AlarmEntity?>(null) }

    // Live Clock State
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now)
            delay(1000L)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddDialogOpen = true },
                containerColor = SunriseOrangeBright,
                contentColor = DarkBackground,
                shape = CircleShape,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("add_alarm_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Alarm",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Branding & Digital Clock
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SunriseOrangeBright.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "WakeQuest",
                                tint = SunriseOrangeBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "WakeQuest",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Challenge-Based Alarms",
                                fontSize = 12.sp,
                                color = SunriseAmber
                            )
                        }
                    }

                    // Practice Hub Action Button
                    OutlinedButton(
                        onClick = { isPracticeHubOpen = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SunriseGold
                        ),
                        modifier = Modifier.testTag("practice_hub_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Practice",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Hero Clock Card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentDateString,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentTimeString.ifBlank { "12:00:00 AM" },
                            color = SunriseGold,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = SunriseAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Each alarm triggers a mandatory 4-sequence (+0m, +5m, +10m, +15m)",
                                color = TextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Alarms Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Alarms (${alarms.size})",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Empty State
            if (alarms.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = DarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(SunriseOrangeBright.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = SunriseOrangeBright,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Alarms Set",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "WakeQuest alarms cannot be easily snoozed. Set an alarm to wake up with drawing or fruit-catching challenges!",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { isAddDialogOpen = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SunriseOrangeBright,
                                    contentColor = DarkBackground
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("set_first_alarm_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Set First Alarm", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // List of Alarm Cards
            items(items = alarms, key = { it.id }) { alarm ->
                AlarmCard(
                    alarm = alarm,
                    onToggle = { viewModel.toggleAlarm(alarm) },
                    onEdit = { editingAlarm = alarm },
                    onDelete = { alarmToDelete = alarm },
                    onTestTrigger = { viewModel.triggerImmediateAlarm(alarm, context) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB and navigation bar
            }
        }
    }

    // Add Alarm Dialog
    if (isAddDialogOpen) {
        AlarmEditDialog(
            initialAlarm = null,
            onDismiss = { isAddDialogOpen = false },
            onSave = { _, hour, minute, daysMask, challengeType, drawingTemplate, targetFruit, label ->
                viewModel.saveAlarm(
                    id = 0L,
                    hour = hour,
                    minute = minute,
                    daysMask = daysMask,
                    challengeType = challengeType,
                    drawingTemplate = drawingTemplate,
                    targetFruit = targetFruit,
                    label = label
                )
            }
        )
    }

    // Edit Alarm Dialog
    if (editingAlarm != null) {
        AlarmEditDialog(
            initialAlarm = editingAlarm,
            onDismiss = { editingAlarm = null },
            onSave = { id, hour, minute, daysMask, challengeType, drawingTemplate, targetFruit, label ->
                viewModel.saveAlarm(
                    id = id,
                    hour = hour,
                    minute = minute,
                    daysMask = daysMask,
                    challengeType = challengeType,
                    drawingTemplate = drawingTemplate,
                    targetFruit = targetFruit,
                    label = label
                )
                editingAlarm = null
            }
        )
    }

    // Practice Hub Dialog
    if (isPracticeHubOpen) {
        PracticeHubDialog(onDismiss = { isPracticeHubOpen = false })
    }

    // Delete Confirmation Dialog
    if (alarmToDelete != null) {
        AlertDialog(
            onDismissRequest = { alarmToDelete = null },
            title = { Text("Delete Alarm?", color = TextPrimary) },
            text = {
                Text(
                    "This will cancel this alarm and all four of its automatic sequential reminders.",
                    color = TextSecondary
                )
            },
            containerColor = DarkSurface,
            confirmButton = {
                TextButton(
                    onClick = {
                        alarmToDelete?.let { viewModel.deleteAlarm(it) }
                        alarmToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { alarmToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun AlarmCard(
    alarm: AlarmEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestTrigger: () -> Unit
) {
    val sequenceTimes = remember(alarm.hour, alarm.minute) {
        AlarmTimeHelper.calculateSequence(alarm.hour, alarm.minute)
    }
    val challengeType = alarm.getParsedChallengeType()
    val drawingTemplate = alarm.getParsedDrawingTemplate()
    val targetFruit = alarm.getParsedTargetFruit()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) DarkSurface else DarkSurfaceElevated.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (alarm.isEnabled) DarkSurfaceBorder else Color(0x1AFFFFFF)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onEdit() }
            .testTag("alarm_card_${alarm.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row: Time, Label, and Enable Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = AlarmTimeHelper.formatTime(alarm.hour, alarm.minute),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (alarm.isEnabled) TextPrimary else TextSecondary
                    )
                    Text(
                        text = alarm.label.ifBlank { "Wake Up!" },
                        fontSize = 13.sp,
                        color = if (alarm.isEnabled) SunriseAmber else TextTertiary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkBackground,
                        checkedTrackColor = SunriseOrangeBright,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.testTag("alarm_toggle_${alarm.id}")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4-Sequence Timeline Pills
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkBackground.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sequenceTimes.forEachIndexed { idx, seq ->
                        Text(
                            text = seq.formattedTime,
                            color = if (alarm.isEnabled) {
                                if (idx == 0) SunriseGold else TextPrimary
                            } else TextTertiary,
                            fontSize = 11.sp,
                            fontWeight = if (idx == 0) FontWeight.Bold else FontWeight.Normal
                        )
                        if (idx < 3) {
                            Text(
                                text = "→",
                                color = if (alarm.isEnabled) SunriseOrange.copy(alpha = 0.7f) else TextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Badges: Repeat Days & Challenge Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Challenge Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (challengeType == ChallengeType.DRAWING) {
                                Icon(
                                    imageVector = Icons.Default.Brush,
                                    contentDescription = null,
                                    tint = SunriseGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Draw: ${drawingTemplate.displayName}",
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(text = targetFruit.emoji, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Catch: ${targetFruit.displayName}",
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Schedule Badge
                    Text(
                        text = AlarmTimeHelper.formatDays(alarm.daysMask),
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Action Buttons: Test Ring & Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTestTrigger,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Test Alarm",
                            tint = SunriseAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Alarm",
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

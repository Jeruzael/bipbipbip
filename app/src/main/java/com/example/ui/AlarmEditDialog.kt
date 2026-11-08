package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.challenge.drawing.DrawingChallengeScreen
import com.example.challenge.fruit.FruitCatchingGameScreen
import com.example.data.AlarmEntity
import com.example.model.AlarmTimeHelper
import com.example.model.ChallengeType
import com.example.model.DrawingTemplate
import com.example.model.FruitType
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
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlarmEditDialog(
    initialAlarm: AlarmEntity?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        hour: Int,
        minute: Int,
        daysMask: Int,
        challengeType: ChallengeType,
        drawingTemplate: DrawingTemplate,
        targetFruit: FruitType,
        label: String
    ) -> Unit
) {
    val currentCal = Calendar.getInstance()
    var selectedHour by remember { mutableIntStateOf(initialAlarm?.hour ?: currentCal.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(initialAlarm?.minute ?: ((currentCal.get(Calendar.MINUTE) / 5) * 5 + 5) % 60) }
    var selectedDaysMask by remember { mutableIntStateOf(initialAlarm?.daysMask ?: 0) }
    var selectedChallengeType by remember {
        mutableStateOf(initialAlarm?.getParsedChallengeType() ?: ChallengeType.DRAWING)
    }
    var selectedDrawingTemplate by remember {
        mutableStateOf(initialAlarm?.getParsedDrawingTemplate() ?: DrawingTemplate.DINOSAUR)
    }
    var selectedTargetFruit by remember {
        mutableStateOf(initialAlarm?.getParsedTargetFruit() ?: FruitType.GRAPES)
    }
    var label by remember { mutableStateOf(initialAlarm?.label ?: "Wake Up!") }

    // Practice preview overlay state
    var showPracticePreview by remember { mutableStateOf(false) }

    val is12Hour = true
    val isPm = selectedHour >= 12
    val display12Hour = if (selectedHour % 12 == 0) 12 else selectedHour % 12

    // Mandatory 4-sequence calculation
    val sequenceTimes = remember(selectedHour, selectedMinute) {
        AlarmTimeHelper.calculateSequence(selectedHour, selectedMinute)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxSize(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(24.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialAlarm == null) "New Alarm" else "Edit Alarm",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Selector Widget (Hour, Minute, AM/PM)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {
                                selectedHour = if (selectedHour == 23) 0 else selectedHour + 1
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Hour", tint = SunriseOrangeBright)
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%02d", display12Hour),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            IconButton(onClick = {
                                selectedHour = if (selectedHour == 0) 23 else selectedHour - 1
                            }) {
                                Icon(Icons.Default.Remove, contentDescription = "Subtract Hour", tint = SunriseOrangeBright)
                            }
                        }

                        Text(
                            text = ":",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = SunriseAmber,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        // Minute
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {
                                selectedMinute = (selectedMinute + 1) % 60
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Minute", tint = SunriseOrangeBright)
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%02d", selectedMinute),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            IconButton(onClick = {
                                selectedMinute = if (selectedMinute == 0) 59 else selectedMinute - 1
                            }) {
                                Icon(Icons.Default.Remove, contentDescription = "Subtract Minute", tint = SunriseOrangeBright)
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // AM / PM Toggle
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (!isPm) SunriseOrange else DarkSurface,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (isPm) selectedHour -= 12
                                    }
                            ) {
                                Text(
                                    text = "AM",
                                    color = if (!isPm) DarkBackground else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isPm) SunriseOrange else DarkSurface,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (!isPm) selectedHour += 12
                                    }
                            ) {
                                Text(
                                    text = "PM",
                                    color = if (isPm) DarkBackground else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // MANDATORY 4-SEQUENCE ALARM PREVIEW (Cannot be disabled)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkBackground,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SunriseAmber.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Sequential Alarms",
                                tint = SunriseAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Automatic 4-Alarm Sequence",
                                color = SunriseAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Mandatory",
                                color = SunriseGold,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sequence pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            sequenceTimes.forEachIndexed { idx, seq ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (idx == 0) SunriseOrangeBright else DarkSurfaceElevated,
                                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                    ) {
                                        Text(
                                            text = seq.formattedTime,
                                            color = if (idx == 0) DarkBackground else TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "+${idx * 5}m",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                                if (idx < 3) {
                                    Text("→", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Every alarm automatically repeats 3 times at 5-minute intervals. Cannot be disabled.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Days of week repeat
                Text(
                    text = "Repeat Days",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayNames.forEachIndexed { index, name ->
                        val isDaySelected = (selectedDaysMask and (1 shl index)) != 0
                        Surface(
                            shape = CircleShape,
                            color = if (isDaySelected) SunriseOrange else DarkSurfaceElevated,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable {
                                    selectedDaysMask = if (isDaySelected) {
                                        selectedDaysMask and (1 shl index).inv()
                                    } else {
                                        selectedDaysMask or (1 shl index)
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = name.first().toString(),
                                    color = if (isDaySelected) DarkBackground else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Challenge Selection
                Text(
                    text = "Required Challenge",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                TabRow(
                    selectedTabIndex = if (selectedChallengeType == ChallengeType.DRAWING) 0 else 1,
                    containerColor = DarkSurfaceElevated,
                    contentColor = SunriseOrangeBright,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[if (selectedChallengeType == ChallengeType.DRAWING) 0 else 1]),
                            color = SunriseOrangeBright
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedChallengeType == ChallengeType.DRAWING,
                        onClick = { selectedChallengeType = ChallengeType.DRAWING },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Drawing")
                            }
                        }
                    )
                    Tab(
                        selected = selectedChallengeType == ChallengeType.FRUIT_GAME,
                        onClick = { selectedChallengeType = ChallengeType.FRUIT_GAME },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🍎", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fruit Game")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Challenge Sub-configuration
                if (selectedChallengeType == ChallengeType.DRAWING) {
                    Text(
                        text = "Select Template:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DrawingTemplate.entries.forEach { tmpl ->
                            val isSelected = selectedDrawingTemplate == tmpl
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) SunriseOrange.copy(alpha = 0.2f) else DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) SunriseOrangeBright else DarkSurfaceBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedDrawingTemplate = tmpl }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = tmpl.displayName,
                                        color = if (isSelected) SunriseGold else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = tmpl.difficulty,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Select Target Fruit to Catch (10 needed):",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FruitType.entries.forEach { fruit ->
                            val isSelected = selectedTargetFruit == fruit
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) fruit.primaryColor.copy(alpha = 0.25f) else DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) fruit.primaryColor else DarkSurfaceBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedTargetFruit = fruit }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = fruit.emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = fruit.displayName,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Practice Challenge Preview Button
                OutlinedButton(
                    onClick = { showPracticePreview = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("practice_preview_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SunriseAmber
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test & Practice Challenge Now")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Alarm Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Alarm Label") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SunriseOrangeBright,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = SunriseOrangeBright,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            onSave(
                                initialAlarm?.id ?: 0L,
                                selectedHour,
                                selectedMinute,
                                selectedDaysMask,
                                selectedChallengeType,
                                selectedDrawingTemplate,
                                selectedTargetFruit,
                                label
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SunriseOrangeBright,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_alarm_button")
                    ) {
                        Text("Save Alarm", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Interactive Practice Preview Dialog
    if (showPracticePreview) {
        Dialog(
            onDismissRequest = { showPracticePreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = DarkBackground
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedChallengeType) {
                        ChallengeType.DRAWING -> {
                            DrawingChallengeScreen(
                                template = selectedDrawingTemplate,
                                onChallengeCompleted = {
                                    showPracticePreview = false
                                },
                                isPracticeMode = true
                            )
                        }
                        ChallengeType.FRUIT_GAME -> {
                            FruitCatchingGameScreen(
                                targetFruit = selectedTargetFruit,
                                onChallengeCompleted = {
                                    showPracticePreview = false
                                },
                                isPracticeMode = true
                            )
                        }
                    }

                    // Floating close button for practice mode
                    IconButton(
                        onClick = { showPracticePreview = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp)
                            .background(DarkSurfaceElevated, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Practice",
                            tint = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

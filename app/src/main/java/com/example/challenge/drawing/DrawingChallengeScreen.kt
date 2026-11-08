package com.example.challenge.drawing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrawingTemplate
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
import kotlin.math.hypot

@Composable
fun DrawingChallengeScreen(
    template: DrawingTemplate,
    onChallengeCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    isPracticeMode: Boolean = false
) {
    var isCompleted by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf(0f) }

    // User drawn strokes: each stroke is a list of Offset points
    val userStrokes = remember { mutableStateListOf<List<Offset>>() }
    var activeStroke = remember { mutableStateListOf<Offset>() }

    // Checkpoint nodes
    var checkpoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val coveredCheckpointIndices = remember { mutableStateListOf<Int>() }

    // Pulsing animation for audio indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    fun resetDrawing() {
        userStrokes.clear()
        activeStroke.clear()
        coveredCheckpointIndices.clear()
        currentProgress = 0f
        isCompleted = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alarm Active",
                    tint = SunriseOrangeBright,
                    modifier = Modifier.size((22 * pulseScale).dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPracticeMode) "Practice Mode" else "ALARM RINGING",
                    color = SunriseOrangeBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Trace the ${template.displayName}",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Cover at least 85% of the outline to dismiss (${template.difficulty})",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Coverage",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(currentProgress * 100).toInt()}% / 85%",
                    color = if (currentProgress >= 0.85f) Color(0xFF4CAF50) else SunriseAmber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (currentProgress / 0.85f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (currentProgress >= 0.85f) Color(0xFF4CAF50) else SunriseOrange,
                trackColor = DarkSurfaceElevated
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Drawing Canvas Container
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .border(2.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
                .testTag("drawing_canvas_container"),
            contentAlignment = Alignment.Center
        ) {
            val canvasW = constraints.maxWidth.toFloat()
            val canvasH = constraints.maxHeight.toFloat()
            val templateStrokes = remember(template) { DrawingTemplatePaths.getStrokes(template) }

            // Generate checkpoints when measured
            LaunchedEffect(canvasW, canvasH, template) {
                if (canvasW > 0 && canvasH > 0) {
                    checkpoints = DrawingTemplatePaths.generateCheckpoints(
                        strokes = templateStrokes,
                        canvasWidth = canvasW,
                        canvasHeight = canvasH,
                        padding = 40f,
                        stepPx = 18f
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isCompleted, checkpoints) {
                        if (isCompleted) return@pointerInput

                        detectDragGestures(
                            onDragStart = { offset ->
                                val stroke = mutableListOf(offset)
                                activeStroke.clear()
                                activeStroke.add(offset)
                                userStrokes.add(stroke)

                                // Test proximity to checkpoints
                                val tolerancePx = 54f // Generous touch target
                                checkpoints.forEachIndexed { index, cp ->
                                    if (index !in coveredCheckpointIndices) {
                                        if (hypot(offset.x - cp.x, offset.y - cp.y) <= tolerancePx) {
                                            coveredCheckpointIndices.add(index)
                                        }
                                    }
                                }
                                if (checkpoints.isNotEmpty()) {
                                    currentProgress = coveredCheckpointIndices.size.toFloat() / checkpoints.size.toFloat()
                                    if (currentProgress >= 0.85f && !isCompleted) {
                                        isCompleted = true
                                        onChallengeCompleted()
                                    }
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val pt = change.position
                                activeStroke.add(pt)

                                if (userStrokes.isNotEmpty()) {
                                    val currentList = userStrokes.last().toMutableList()
                                    currentList.add(pt)
                                    userStrokes[userStrokes.size - 1] = currentList
                                }

                                // Check proximity
                                val tolerancePx = 54f
                                checkpoints.forEachIndexed { index, cp ->
                                    if (index !in coveredCheckpointIndices) {
                                        if (hypot(pt.x - cp.x, pt.y - cp.y) <= tolerancePx) {
                                            coveredCheckpointIndices.add(index)
                                        }
                                    }
                                }

                                if (checkpoints.isNotEmpty()) {
                                    currentProgress = coveredCheckpointIndices.size.toFloat() / checkpoints.size.toFloat()
                                    if (currentProgress >= 0.85f && !isCompleted) {
                                        isCompleted = true
                                        onChallengeCompleted()
                                    }
                                }
                            },
                            onDragEnd = {
                                activeStroke.clear()
                            },
                            onDragCancel = {
                                activeStroke.clear()
                            }
                        )
                    }
            ) {
                val padding = 40f
                val usableW = size.width - (padding * 2f)
                val usableH = size.height - (padding * 2f)

                // 1. Draw Template Guide Lines (dashed/dotted faint outline)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                for (stroke in templateStrokes) {
                    val pts = stroke.points
                    if (pts.size < 2) continue
                    val path = Path()
                    path.moveTo(padding + pts[0].x * usableW, padding + pts[0].y * usableH)
                    for (i in 1 until pts.size) {
                        path.lineTo(padding + pts[i].x * usableW, padding + pts[i].y * usableH)
                    }
                    drawPath(
                        path = path,
                        color = Color(0x55FFFFFF),
                        style = Stroke(
                            width = 6f,
                            pathEffect = dashEffect,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // 2. Draw Checkpoint indicators: covered vs pending
                val coveredSet = coveredCheckpointIndices.toSet()
                checkpoints.forEachIndexed { index, cp ->
                    if (index in coveredSet) {
                        drawCircle(
                            color = SunriseGold.copy(alpha = 0.5f),
                            radius = 6f,
                            center = cp
                        )
                    } else {
                        drawCircle(
                            color = Color(0x33FFFFFF),
                            radius = 3.5f,
                            center = cp
                        )
                    }
                }

                // 3. Draw User's glowing finger trails
                for (stroke in userStrokes) {
                    if (stroke.size < 2) continue
                    val path = Path()
                    path.moveTo(stroke[0].x, stroke[0].y)
                    for (i in 1 until stroke.size) {
                        path.lineTo(stroke[i].x, stroke[i].y)
                    }
                    // Outer glow
                    drawPath(
                        path = path,
                        color = SunriseOrange.copy(alpha = 0.35f),
                        style = Stroke(
                            width = 24f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                    // Core stroke
                    drawPath(
                        path = path,
                        color = SunriseGold,
                        style = Stroke(
                            width = 12f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Success overlay when completed
            androidx.compose.animation.AnimatedVisibility(
                visible = isCompleted,
                enter = fadeIn() + scaleIn(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DarkSurfaceElevated.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4CAF50)),
                    shadowElevation = 12.dp,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tracing Complete!",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Alarm Dismissed",
                            color = Color(0xFF81C784),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Action Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { resetDrawing() },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("reset_drawing_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart Drawing",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restart")
            }

            Text(
                text = "Trace with continuous strokes",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

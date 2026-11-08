package com.example.challenge.fruit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.hypot
import kotlin.random.Random

data class ActiveFruit(
    val id: Long,
    val type: FruitType,
    var x: Float, // Pixels
    var y: Float, // Pixels
    val speed: Float, // Pixels per second
    val sizePx: Float, // Diameter
    var rotation: Float = 0f,
    var isPopped: Boolean = false
)

@Composable
fun FruitCatchingGameScreen(
    targetFruit: FruitType,
    onChallengeCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    isPracticeMode: Boolean = false
) {
    var score by remember { mutableIntStateOf(0) }
    var isCompleted by remember { mutableStateOf(false) }
    val fruits = remember { mutableStateListOf<ActiveFruit>() }

    // Pulsing animation for audio indicator
    val infiniteTransition = rememberInfiniteTransition(label = "audioPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

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

            // Prominent Target Fruit Banner
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(2.dp, targetFruit.primaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(targetFruit.primaryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = targetFruit.emoji, fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Catch Target: ${targetFruit.displayName}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Tap only the ${targetFruit.displayName.lowercase()}s",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Score Display
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$score / 10",
                            color = if (score >= 10) Color(0xFF4CAF50) else SunriseAmber,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Collected",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Linear Progress
        LinearProgressIndicator(
            progress = { (score / 10f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (score >= 10) Color(0xFF4CAF50) else SunriseOrange,
            trackColor = DarkSurfaceElevated
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Falling Fruit Arcade Arena
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .border(2.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
                .testTag("fruit_game_arena"),
            contentAlignment = Alignment.TopStart
        ) {
            val arenaW = constraints.maxWidth.toFloat()
            val arenaH = constraints.maxHeight.toFloat()
            val density = LocalDensity.current

            // Fruit physics & spawner game loop
            LaunchedEffect(isCompleted, arenaW, arenaH) {
                if (isCompleted || arenaW <= 0 || arenaH <= 0) return@LaunchedEffect

                var lastFrameNanos = 0L
                var nextSpawnTime = 0L
                var idCounter = 1L
                val allFruitTypes = FruitType.entries.toTypedArray()

                while (!isCompleted) {
                    withFrameNanos { nowNanos ->
                        val dt = if (lastFrameNanos == 0L) 0.016f else ((nowNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                        lastFrameNanos = nowNanos

                        // 1. Spawning
                        if (nowNanos >= nextSpawnTime) {
                            // Target fruit spawns ~45% of the time, other fruits 55%
                            val isTarget = Random.nextFloat() < 0.45f
                            val chosenFruit = if (isTarget) targetFruit else {
                                val others = allFruitTypes.filter { it != targetFruit }
                                others[Random.nextInt(others.size)]
                            }

                            val fruitSizePx = with(density) { Random.nextInt(64, 76).dp.toPx() }
                            val startX = Random.nextFloat() * (arenaW - fruitSizePx).coerceAtLeast(10f)
                            // Speed gradually accelerates based on score
                            val baseSpeed = with(density) { Random.nextInt(320, 480).dp.toPx() }
                            val speedBoost = (score * with(density) { 15.dp.toPx() })

                            fruits.add(
                                ActiveFruit(
                                    id = idCounter++,
                                    type = chosenFruit,
                                    x = startX,
                                    y = -fruitSizePx,
                                    speed = baseSpeed + speedBoost,
                                    sizePx = fruitSizePx,
                                    rotation = Random.nextFloat() * 360f
                                )
                            )

                            // Interval: 450ms - 800ms
                            val delayMs = Random.nextLong(450, 750)
                            nextSpawnTime = nowNanos + delayMs * 1_000_000L
                        }

                        // 2. Update positions and remove fruits that exit the screen
                        val iterator = fruits.iterator()
                        while (iterator.hasNext()) {
                            val fruit = iterator.next()
                            fruit.y += fruit.speed * dt
                            fruit.rotation = (fruit.rotation + 45f * dt) % 360f

                            // Remove if out of bottom boundary or popped
                            if (fruit.y > arenaH + 100f || fruit.isPopped) {
                                iterator.remove()
                            }
                        }
                    }
                }
            }

            // Interactive Touch Area for Fruit Tapping
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isCompleted, targetFruit) {
                        if (isCompleted) return@pointerInput

                        detectTapGestures { tapOffset ->
                            // Find closest tapped un-popped fruit
                            var tappedFruit: ActiveFruit? = null
                            for (fruit in fruits.reversed()) {
                                if (fruit.isPopped) continue
                                val centerX = fruit.x + fruit.sizePx / 2f
                                val centerY = fruit.y + fruit.sizePx / 2f
                                val tapRadius = fruit.sizePx * 0.75f // Generous hit-box for comfortable morning tap
                                if (hypot(tapOffset.x - centerX, tapOffset.y - centerY) <= tapRadius) {
                                    tappedFruit = fruit
                                    break
                                }
                            }

                            tappedFruit?.let { fruit ->
                                if (fruit.type == targetFruit) {
                                    fruit.isPopped = true
                                    score++
                                    if (score >= 10 && !isCompleted) {
                                        isCompleted = true
                                        onChallengeCompleted()
                                    }
                                } else {
                                    // Wrong fruit tapped: no score, slight visual reaction
                                    fruit.rotation += 25f
                                }
                            }
                        }
                    }
            ) {
                // Render Falling Fruits
                for (fruit in fruits) {
                    if (fruit.isPopped) continue

                    val sizeDp = with(density) { fruit.sizePx.toDp() }
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(fruit.x.toInt(), fruit.y.toInt()) }
                            .size(sizeDp)
                            .rotate(fruit.rotation)
                            .clip(CircleShape)
                            .background(
                                if (fruit.type == targetFruit) {
                                    fruit.type.primaryColor.copy(alpha = 0.25f)
                                } else {
                                    DarkSurfaceElevated.copy(alpha = 0.7f)
                                }
                            )
                            .border(
                                width = if (fruit.type == targetFruit) 2.5.dp else 1.dp,
                                color = if (fruit.type == targetFruit) SunriseGold else Color(0x33FFFFFF),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fruit.type.emoji,
                            fontSize = with(density) { (fruit.sizePx * 0.55f).toSp() }
                        )
                    }
                }
            }

            // Victory Success Overlay
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
                            text = "10 ${targetFruit.displayName}s Collected!",
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

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom status note
        Text(
            text = "Tap 10 ${targetFruit.displayName.lowercase()}s to silence the alarm",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

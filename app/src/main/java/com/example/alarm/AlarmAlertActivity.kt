package com.example.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.WakeQuestApplication
import com.example.challenge.drawing.DrawingChallengeScreen
import com.example.challenge.fruit.FruitCatchingGameScreen
import com.example.data.AlarmEntity
import com.example.model.AlarmTimeHelper
import com.example.model.ChallengeType
import com.example.model.DrawingTemplate
import com.example.model.FruitType
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SunriseAmber
import com.example.ui.theme.SunriseGold
import com.example.ui.theme.SunriseOrange
import com.example.ui.theme.SunriseOrangeBright
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class AlarmAlertActivity : ComponentActivity() {

    private var currentAlarmId = -1L
    private var currentSequenceIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureLockScreenDisplay()

        currentAlarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        currentSequenceIndex = intent.getIntExtra(AlarmScheduler.EXTRA_SEQUENCE_INDEX, 0)

        renderContent()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Maintain the active challenge session without restarting or disrupting user progress
        currentSequenceIndex = intent.getIntExtra(AlarmScheduler.EXTRA_SEQUENCE_INDEX, currentSequenceIndex)
    }

    private fun configureLockScreenDisplay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun renderContent() {
        val app = applicationContext as WakeQuestApplication

        setContent {
            MyApplicationTheme {
                var alarmEntity by remember { mutableStateOf<AlarmEntity?>(null) }
                var isDismissedScreenVisible by remember { mutableStateOf(false) }

                LaunchedEffect(currentAlarmId) {
                    if (currentAlarmId > 0) {
                        alarmEntity = app.repository.getAlarmById(currentAlarmId)
                    }
                }

                // Prevent dismiss via back button until challenge completion
                BackHandler(enabled = !isDismissedScreenVisible) {
                    // Do nothing - challenge required!
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    if (isDismissedScreenVisible) {
                        AlarmDismissedSummary(
                            alarm = alarmEntity,
                            sequenceIndex = currentSequenceIndex,
                            onClose = {
                                finish()
                            }
                        )
                    } else {
                        val entity = alarmEntity
                        val challengeType = entity?.getParsedChallengeType() ?: ChallengeType.DRAWING
                        val drawingTemplate = entity?.getParsedDrawingTemplate() ?: DrawingTemplate.DINOSAUR
                        val targetFruit = entity?.getParsedTargetFruit() ?: FruitType.GRAPES

                        when (challengeType) {
                            ChallengeType.DRAWING -> {
                                DrawingChallengeScreen(
                                    template = drawingTemplate,
                                    onChallengeCompleted = {
                                        AlarmRingingService.stop(this@AlarmAlertActivity)
                                        isDismissedScreenVisible = true
                                    }
                                )
                            }
                            ChallengeType.FRUIT_GAME -> {
                                FruitCatchingGameScreen(
                                    targetFruit = targetFruit,
                                    onChallengeCompleted = {
                                        AlarmRingingService.stop(this@AlarmAlertActivity)
                                        isDismissedScreenVisible = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmDismissedSummary(
    alarm: AlarmEntity?,
    sequenceIndex: Int,
    onClose: () -> Unit
) {
    val sequenceTimes = remember(alarm) {
        if (alarm != null) {
            AlarmTimeHelper.calculateSequence(alarm.hour, alarm.minute)
        } else emptyList()
    }

    val nextOccurrence = sequenceTimes.getOrNull(sequenceIndex + 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Dismissed",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Great Job!",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Alarm dismissed. You're wide awake!",
            color = TextSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Next Scheduled Occurrence Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DarkSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Next Alarm",
                        tint = SunriseAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Automatic 4-Sequence Schedule",
                        color = SunriseAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (nextOccurrence != null) {
                    Text(
                        text = "Next reminder in 5 minutes at ${nextOccurrence.formattedTime}",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Reminder ${sequenceIndex + 2} of 4 remains scheduled.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = "All 4 sequential reminders completed!",
                        color = Color(0xFF81C784),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(
                containerColor = SunriseOrangeBright,
                contentColor = DarkBackground
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("dismiss_got_it_button")
        ) {
            Text(
                text = "Got It",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

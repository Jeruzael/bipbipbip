package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarm.AlarmScheduler
import com.example.data.AlarmEntity
import com.example.model.ChallengeType
import com.example.model.DrawingTemplate
import com.example.model.FruitType
import com.example.ui.AlarmViewModel
import com.example.ui.HomeScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        seedInitialAlarmIfEmpty()

        setContent {
            MyApplicationTheme {
                // Request POST_NOTIFICATIONS permission on Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { /* Handled */ }
                    )
                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                val viewModel: AlarmViewModel = viewModel()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun seedInitialAlarmIfEmpty() {
        val app = applicationContext as WakeQuestApplication
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existing = app.repository.getEnabledAlarms()
                if (existing.isEmpty()) {
                    // Seed a default 7:00 AM alarm with Dinosaur drawing challenge
                    val defaultAlarm = AlarmEntity(
                        hour = 7,
                        minute = 0,
                        isEnabled = true,
                        daysMask = 0b0111110, // Mon-Fri
                        challengeType = ChallengeType.DRAWING.name,
                        drawingTemplate = DrawingTemplate.DINOSAUR.name,
                        targetFruit = FruitType.GRAPES.name,
                        label = "Rise & Shine"
                    )
                    val id = app.repository.insertAlarm(defaultAlarm)
                    val inserted = defaultAlarm.copy(id = id)
                    AlarmScheduler.scheduleSequentialAlarms(this@MainActivity, inserted)
                }
            } catch (_: Exception) {}
        }
    }
}

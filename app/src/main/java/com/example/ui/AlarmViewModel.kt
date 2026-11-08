package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.WakeQuestApplication
import com.example.alarm.AlarmAlertActivity
import com.example.alarm.AlarmRingingService
import com.example.alarm.AlarmScheduler
import com.example.data.AlarmEntity
import com.example.model.ChallengeType
import com.example.model.DrawingTemplate
import com.example.model.FruitType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as WakeQuestApplication
    private val repository = app.repository

    val alarms: StateFlow<List<AlarmEntity>> = repository.allAlarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updated)
            if (updated.isEnabled) {
                AlarmScheduler.scheduleSequentialAlarms(app, updated)
            } else {
                AlarmScheduler.cancelSequentialAlarms(app, updated)
            }
        }
    }

    fun saveAlarm(
        id: Long = 0,
        hour: Int,
        minute: Int,
        daysMask: Int,
        challengeType: ChallengeType,
        drawingTemplate: DrawingTemplate,
        targetFruit: FruitType,
        label: String
    ) {
        viewModelScope.launch {
            val entity = AlarmEntity(
                id = id,
                hour = hour,
                minute = minute,
                isEnabled = true,
                daysMask = daysMask,
                challengeType = challengeType.name,
                drawingTemplate = drawingTemplate.name,
                targetFruit = targetFruit.name,
                label = label.ifBlank { "Wake Up!" }
            )

            if (id > 0) {
                // Cancel existing occurrences before rescheduling
                AlarmScheduler.cancelSequentialAlarms(app, entity)
                repository.updateAlarm(entity)
                AlarmScheduler.scheduleSequentialAlarms(app, entity)
            } else {
                val newId = repository.insertAlarm(entity)
                val newEntity = entity.copy(id = newId)
                AlarmScheduler.scheduleSequentialAlarms(app, newEntity)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancelSequentialAlarms(app, alarm)
            repository.deleteAlarm(alarm)
        }
    }

    /**
     * Instantly triggers an active alarm challenge for the given entity (helpful for testing and preview)
     */
    fun triggerImmediateAlarm(alarm: AlarmEntity, context: Context) {
        AlarmRingingService.start(context, alarm.id, sequenceIndex = 0)
        val intent = Intent(context, AlarmAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmScheduler.EXTRA_SEQUENCE_INDEX, 0)
        }
        context.startActivity(intent)
    }
}

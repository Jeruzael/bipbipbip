package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.AlarmEntity
import java.util.Calendar

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"
    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_SEQUENCE_INDEX = "extra_sequence_index"

    /**
     * Schedules all 4 sequential alarms for the given entity (+0m, +5m, +10m, +15m).
     */
    fun scheduleSequentialAlarms(context: Context, alarm: AlarmEntity) {
        if (!alarm.isEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (sequenceIndex in 0..3) {
            val triggerMillis = calculateNextTriggerTime(
                hour = alarm.hour,
                minute = alarm.minute,
                daysMask = alarm.daysMask,
                offsetMinutes = sequenceIndex * 5
            )

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.example.wakequest.ALARM_TRIGGER"
                putExtra(EXTRA_ALARM_ID, alarm.id)
                putExtra(EXTRA_SEQUENCE_INDEX, sequenceIndex)
            }

            val requestCode = getRequestCode(alarm.id, sequenceIndex)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
                Log.d(TAG, "Scheduled alarm ID ${alarm.id} seq $sequenceIndex for $triggerMillis")
            } catch (e: SecurityException) {
                Log.e(TAG, "Permission denied to set exact alarm: ${e.message}")
            }
        }
    }

    /**
     * Cancels all 4 sequential alarms for the given entity.
     */
    fun cancelSequentialAlarms(context: Context, alarm: AlarmEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (sequenceIndex in 0..3) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.example.wakequest.ALARM_TRIGGER"
            }
            val requestCode = getRequestCode(alarm.id, sequenceIndex)
            val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled alarm ID ${alarm.id} seq $sequenceIndex")
            }
        }
    }

    private fun getRequestCode(alarmId: Long, sequenceIndex: Int): Int {
        return (alarmId * 10 + sequenceIndex).toInt()
    }

    /**
     * Calculates the timestamp (in millis) for an alarm occurrence.
     */
    fun calculateNextTriggerTime(
        hour: Int,
        minute: Int,
        daysMask: Int,
        offsetMinutes: Int
    ): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, offsetMinutes)
        }

        if (daysMask == 0) {
            // One-time alarm
            if (target.before(now)) {
                // If this occurrence has passed, schedule for tomorrow
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis
        }

        // Repeating on specific days
        // daysMask bit 0: Sunday, bit 1: Monday, ... bit 6: Saturday
        // Calendar.SUNDAY is 1, so bit = cal.get(DAY_OF_WEEK) - 1
        var daysAdded = 0
        while (daysAdded < 14) {
            val dayOfWeekBit = target.get(Calendar.DAY_OF_WEEK) - 1
            val isDayActive = (daysMask and (1 shl dayOfWeekBit)) != 0
            if (isDayActive && target.after(now)) {
                return target.timeInMillis
            }
            target.add(Calendar.DAY_OF_YEAR, 1)
            daysAdded++
        }

        return target.timeInMillis
    }
}

package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        val sequenceIndex = intent.getIntExtra(AlarmScheduler.EXTRA_SEQUENCE_INDEX, 0)
        Log.d(TAG, "Alarm triggered! ID: $alarmId, Seq: $sequenceIndex")

        // Acquire a temporary wake lock to ensure the screen turns on reliably
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "WakeQuest:AlarmWakeLock"
        )
        wakeLock?.acquire(10_000L) // 10 seconds timeout

        // Start ringing foreground service
        AlarmRingingService.start(context, alarmId, sequenceIndex)

        // Launch full-screen AlarmAlertActivity
        val alertIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_SEQUENCE_INDEX, sequenceIndex)
        }
        context.startActivity(alertIntent)
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}

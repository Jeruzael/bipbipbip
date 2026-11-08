package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.WakeQuestApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Received system broadcast: $action")

        val app = context.applicationContext as? WakeQuestApplication ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarms = app.repository.getEnabledAlarms()
                Log.d(TAG, "Restoring ${alarms.size} enabled alarms after reboot")
                for (alarm in alarms) {
                    AlarmScheduler.scheduleSequentialAlarms(context, alarm)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring alarms: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}

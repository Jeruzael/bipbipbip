package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.example.data.AlarmDatabase
import com.example.data.AlarmRepository

class WakeQuestApplication : Application() {

    lateinit var repository: AlarmRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AlarmDatabase.getInstance(this)
        repository = AlarmRepository(database.alarmDao())

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "WakeQuest Challenge Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority alarms requiring challenges to dismiss"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 300, 600, 300, 800)
                setSound(alarmSoundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(alarmChannel)
        }
    }

    companion object {
        const val ALARM_CHANNEL_ID = "wakequest_alarm_channel"
        const val ALARM_NOTIFICATION_ID = 1001
    }
}

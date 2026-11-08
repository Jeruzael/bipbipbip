package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FruitApple
import com.example.ui.theme.FruitBanana
import com.example.ui.theme.FruitGrapes
import com.example.ui.theme.FruitOrange
import com.example.ui.theme.FruitStrawberry
import com.example.ui.theme.FruitWatermelon
import java.util.Calendar
import java.util.Locale

enum class ChallengeType(val title: String, val subtitle: String) {
    DRAWING("Drawing Challenge", "Trace the template with your finger"),
    FRUIT_GAME("Fruit Catching Game", "Tap 10 falling target fruits")
}

enum class DrawingTemplate(val displayName: String, val difficulty: String) {
    STAR("Star", "Easy"),
    HOUSE("House", "Medium"),
    DINOSAUR("Dinosaur", "Hard")
}

enum class FruitType(
    val displayName: String,
    val emoji: String,
    val primaryColor: Color
) {
    APPLE("Apple", "🍎", FruitApple),
    BANANA("Banana", "🍌", FruitBanana),
    GRAPES("Grapes", "🍇", FruitGrapes),
    ORANGE("Orange", "🍊", FruitOrange),
    STRAWBERRY("Strawberry", "🍓", FruitStrawberry),
    WATERMELON("Watermelon", "🍉", FruitWatermelon)
}

data class SequentialAlarmTime(
    val index: Int, // 0 to 3
    val hour: Int,
    val minute: Int,
    val formattedTime: String
)

object AlarmTimeHelper {
    /**
     * Calculates the mandatory 4-sequence alarm times (+0m, +5m, +10m, +15m).
     */
    fun calculateSequence(baseHour: Int, baseMinute: Int): List<SequentialAlarmTime> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, baseHour)
            set(Calendar.MINUTE, baseMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return (0..3).map { index ->
            val occurrenceCal = (calendar.clone() as Calendar).apply {
                add(Calendar.MINUTE, index * 5)
            }
            val h = occurrenceCal.get(Calendar.HOUR_OF_DAY)
            val m = occurrenceCal.get(Calendar.MINUTE)
            val displayHour = if (h % 12 == 0) 12 else h % 12
            val amPm = if (h < 12) "AM" else "PM"
            val formatted = String.format(Locale.getDefault(), "%d:%02d %s", displayHour, m, amPm)
            SequentialAlarmTime(
                index = index,
                hour = h,
                minute = m,
                formattedTime = formatted
            )
        }
    }

    fun formatTime(hour: Int, minute: Int): String {
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        val amPm = if (hour < 12) "AM" else "PM"
        return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
    }

    fun formatDays(daysMask: Int): String {
        if (daysMask == 0) return "Once (Tomorrow if past)"
        if (daysMask == 0b1111111) return "Every day"
        if (daysMask == 0b0111110) return "Weekdays (Mon-Fri)"
        if (daysMask == 0b1000001) return "Weekends (Sat, Sun)"

        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val active = mutableListOf<String>()
        for (i in 0..6) {
            if ((daysMask and (1 shl i)) != 0) {
                active.add(days[i])
            }
        }
        return active.joinToString(", ")
    }
}

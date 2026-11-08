package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ChallengeType
import com.example.model.DrawingTemplate
import com.example.model.FruitType

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val daysMask: Int = 0, // Bitmask: bit 0=Sun, 1=Mon, ..., 6=Sat. 0 = one-time
    val challengeType: String = ChallengeType.DRAWING.name,
    val drawingTemplate: String = DrawingTemplate.DINOSAUR.name,
    val targetFruit: String = FruitType.GRAPES.name,
    val label: String = "Wake Up!"
) {
    fun getParsedChallengeType(): ChallengeType = try {
        ChallengeType.valueOf(challengeType)
    } catch (_: Exception) {
        ChallengeType.DRAWING
    }

    fun getParsedDrawingTemplate(): DrawingTemplate = try {
        DrawingTemplate.valueOf(drawingTemplate)
    } catch (_: Exception) {
        DrawingTemplate.DINOSAUR
    }

    fun getParsedTargetFruit(): FruitType = try {
        FruitType.valueOf(targetFruit)
    } catch (_: Exception) {
        FruitType.GRAPES
    }
}

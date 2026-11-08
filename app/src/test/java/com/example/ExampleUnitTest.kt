package com.example

import com.example.challenge.drawing.DrawingTemplatePaths
import com.example.model.AlarmTimeHelper
import com.example.model.DrawingTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMandatoryFourSequenceAlarms() {
        val baseHour = 5
        val baseMinute = 30
        val sequence = AlarmTimeHelper.calculateSequence(baseHour, baseMinute)

        // Must always generate 4 sequential alarms
        assertEquals(4, sequence.size)

        // Must maintain exact 5-minute intervals
        assertEquals(5, sequence[0].hour)
        assertEquals(30, sequence[0].minute)

        assertEquals(5, sequence[1].hour)
        assertEquals(35, sequence[1].minute)

        assertEquals(5, sequence[2].hour)
        assertEquals(40, sequence[2].minute)

        assertEquals(5, sequence[3].hour)
        assertEquals(45, sequence[3].minute)
    }

    @Test
    fun testDrawingTemplateCheckpointsGeneration() {
        val starStrokes = DrawingTemplatePaths.getStrokes(DrawingTemplate.STAR)
        val dinoStrokes = DrawingTemplatePaths.getStrokes(DrawingTemplate.DINOSAUR)

        val starCheckpoints = DrawingTemplatePaths.generateCheckpoints(
            strokes = starStrokes,
            canvasWidth = 400f,
            canvasHeight = 400f
        )
        val dinoCheckpoints = DrawingTemplatePaths.generateCheckpoints(
            strokes = dinoStrokes,
            canvasWidth = 400f,
            canvasHeight = 400f
        )

        // Verify dense sampling points along the paths
        assertTrue(starCheckpoints.size > 20)
        assertTrue(dinoCheckpoints.size > 50)
    }

    @Test
    fun testDaysMaskFormatting() {
        assertEquals("Once (Tomorrow if past)", AlarmTimeHelper.formatDays(0))
        assertEquals("Every day", AlarmTimeHelper.formatDays(0b1111111))
        assertEquals("Weekdays (Mon-Fri)", AlarmTimeHelper.formatDays(0b0111110))
    }
}

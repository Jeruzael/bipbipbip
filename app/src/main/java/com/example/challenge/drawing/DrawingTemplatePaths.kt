package com.example.challenge.drawing

import androidx.compose.ui.geometry.Offset
import com.example.model.DrawingTemplate
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class NormalizedPoint(val x: Float, val y: Float)

data class NormalizedStroke(val points: List<NormalizedPoint>)

object DrawingTemplatePaths {

    /**
     * Returns the stroke lines defining the selected template normalized to 0.0f..1.0f.
     */
    fun getStrokes(template: DrawingTemplate): List<NormalizedStroke> {
        return when (template) {
            DrawingTemplate.STAR -> createStarStrokes()
            DrawingTemplate.HOUSE -> createHouseStrokes()
            DrawingTemplate.DINOSAUR -> createDinosaurStrokes()
        }
    }

    /**
     * 5-pointed star path (continuous stroke)
     */
    private fun createStarStrokes(): List<NormalizedStroke> {
        val centerX = 0.5f
        val centerY = 0.5f
        val outerRadius = 0.42f
        val innerRadius = 0.19f
        val points = mutableListOf<NormalizedPoint>()

        for (i in 0 until 10) {
            val angle = (Math.PI / 2.0 - i * Math.PI / 5.0).toFloat()
            val r = if (i % 2 == 0) outerRadius else innerRadius
            points.add(NormalizedPoint(centerX + r * cos(angle), centerY - r * sin(angle)))
        }
        // Close star
        points.add(points.first())
        return listOf(NormalizedStroke(points))
    }

    /**
     * House template: Roof, base walls, door, chimney
     */
    private fun createHouseStrokes(): List<NormalizedStroke> {
        // Walls & base
        val walls = NormalizedStroke(
            listOf(
                NormalizedPoint(0.20f, 0.50f),
                NormalizedPoint(0.20f, 0.85f),
                NormalizedPoint(0.80f, 0.85f),
                NormalizedPoint(0.80f, 0.50f)
            )
        )
        // Roof
        val roof = NormalizedStroke(
            listOf(
                NormalizedPoint(0.15f, 0.50f),
                NormalizedPoint(0.50f, 0.20f),
                NormalizedPoint(0.85f, 0.50f),
                NormalizedPoint(0.15f, 0.50f)
            )
        )
        // Door
        val door = NormalizedStroke(
            listOf(
                NormalizedPoint(0.42f, 0.85f),
                NormalizedPoint(0.42f, 0.65f),
                NormalizedPoint(0.58f, 0.65f),
                NormalizedPoint(0.58f, 0.85f)
            )
        )
        // Chimney
        val chimney = NormalizedStroke(
            listOf(
                NormalizedPoint(0.68f, 0.36f),
                NormalizedPoint(0.68f, 0.22f),
                NormalizedPoint(0.76f, 0.22f),
                NormalizedPoint(0.76f, 0.43f)
            )
        )

        return listOf(walls, roof, door, chimney)
    }

    /**
     * Dinosaur silhouette strokes (T-Rex / Brontosaurus outline)
     */
    private fun createDinosaurStrokes(): List<NormalizedStroke> {
        // Main body, head, back, tail, and feet
        val dinoBody = NormalizedStroke(
            listOf(
                // Snout & head
                NormalizedPoint(0.60f, 0.18f),
                NormalizedPoint(0.76f, 0.18f),
                NormalizedPoint(0.78f, 0.25f),
                NormalizedPoint(0.68f, 0.26f), // mouth open
                NormalizedPoint(0.74f, 0.30f),
                NormalizedPoint(0.66f, 0.36f), // jaw & throat
                // Neck & back curve
                NormalizedPoint(0.54f, 0.38f),
                NormalizedPoint(0.42f, 0.44f),
                NormalizedPoint(0.32f, 0.52f),
                // Tail
                NormalizedPoint(0.18f, 0.60f),
                NormalizedPoint(0.08f, 0.68f),
                NormalizedPoint(0.14f, 0.74f),
                NormalizedPoint(0.26f, 0.72f),
                // Belly & leg 1
                NormalizedPoint(0.36f, 0.68f),
                NormalizedPoint(0.38f, 0.84f),
                NormalizedPoint(0.46f, 0.84f),
                NormalizedPoint(0.48f, 0.70f),
                // Leg 2
                NormalizedPoint(0.54f, 0.70f),
                NormalizedPoint(0.56f, 0.84f),
                NormalizedPoint(0.64f, 0.84f),
                NormalizedPoint(0.62f, 0.64f),
                // Chest & tiny T-Rex arm
                NormalizedPoint(0.62f, 0.52f),
                NormalizedPoint(0.70f, 0.54f),
                NormalizedPoint(0.64f, 0.48f),
                // Front neck to top of head
                NormalizedPoint(0.60f, 0.34f),
                NormalizedPoint(0.60f, 0.18f)
            )
        )

        // Dino eye
        val eye = NormalizedStroke(
            listOf(
                NormalizedPoint(0.68f, 0.21f),
                NormalizedPoint(0.70f, 0.21f)
            )
        )

        // Back spine ridges
        val spines = NormalizedStroke(
            listOf(
                NormalizedPoint(0.52f, 0.34f),
                NormalizedPoint(0.50f, 0.30f),
                NormalizedPoint(0.46f, 0.38f),
                NormalizedPoint(0.43f, 0.34f),
                NormalizedPoint(0.38f, 0.44f),
                NormalizedPoint(0.34f, 0.40f),
                NormalizedPoint(0.28f, 0.50f)
            )
        )

        return listOf(dinoBody, eye, spines)
    }

    /**
     * Interpolates checkpoints along the template strokes scaled to the given canvas size.
     */
    fun generateCheckpoints(
        strokes: List<NormalizedStroke>,
        canvasWidth: Float,
        canvasHeight: Float,
        padding: Float = 40f,
        stepPx: Float = 14f
    ): List<Offset> {
        val usableW = canvasWidth - (padding * 2f)
        val usableH = canvasHeight - (padding * 2f)
        val checkpoints = mutableListOf<Offset>()

        for (stroke in strokes) {
            val pts = stroke.points
            if (pts.size < 2) continue

            for (i in 0 until pts.size - 1) {
                val p1 = Offset(padding + pts[i].x * usableW, padding + pts[i].y * usableH)
                val p2 = Offset(padding + pts[i + 1].x * usableW, padding + pts[i + 1].y * usableH)

                val dist = hypot(p2.x - p1.x, p2.y - p1.y)
                val segments = (dist / stepPx).toInt().coerceAtLeast(1)

                for (s in 0..segments) {
                    val t = s.toFloat() / segments
                    val sampled = Offset(
                        p1.x + (p2.x - p1.x) * t,
                        p1.y + (p2.y - p1.y) * t
                    )
                    checkpoints.add(sampled)
                }
            }
        }
        return checkpoints
    }
}

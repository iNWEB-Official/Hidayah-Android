package com.hidayah.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * The Hidayah cover emblem from mockup 00: gold double ring with a stylised
 * mosque (dome + finial + body + two minarets) drawn inside.
 */
@Composable
fun HidayahEmblem(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.minDimension
        val c = Offset(size.width / 2f, size.height / 2f)
        val gold = Color(0xFFD4AF37)
        val deep = Color(0xFF062A20)

        // double ring
        drawCircle(gold, radius = w * 0.46f, center = c, style = Stroke(width = w * 0.022f))
        drawCircle(gold, radius = w * 0.375f, center = c, style = Stroke(width = w * 0.012f))

        // mosque geometry (centered in the ring)
        val cy = c.y + w * 0.02f
        // finial
        drawRect(gold, topLeft = Offset(c.x - w * 0.007f, cy - w * 0.235f), size = Size(w * 0.014f, w * 0.055f))
        drawCircle(gold, radius = w * 0.011f, center = Offset(c.x, cy - w * 0.242f))
        // dome
        val dome = Path().apply {
            moveTo(c.x - w * 0.135f, cy - w * 0.045f)
            quadraticBezierTo(c.x - w * 0.135f, cy - w * 0.185f, c.x, cy - w * 0.185f)
            quadraticBezierTo(c.x + w * 0.135f, cy - w * 0.185f, c.x + w * 0.135f, cy - w * 0.045f)
            close()
        }
        drawPath(dome, gold)
        // body
        drawRoundRect(
            gold,
            topLeft = Offset(c.x - w * 0.185f, cy - w * 0.045f),
            size = Size(w * 0.37f, w * 0.185f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.012f)
        )
        // minarets
        listOf(-1f, 1f).forEach { side ->
            val mx = c.x + side * w * 0.245f
            drawRect(gold, topLeft = Offset(mx - w * 0.028f, cy - w * 0.115f), size = Size(w * 0.056f, w * 0.255f))
            val tip = Path().apply {
                moveTo(mx - w * 0.042f, cy - w * 0.115f)
                lineTo(mx, cy - w * 0.19f)
                lineTo(mx + w * 0.042f, cy - w * 0.115f)
                close()
            }
            drawPath(tip, gold)
        }
        // door (knock-out)
        val door = Path().apply {
            val dw = w * 0.052f
            moveTo(c.x - dw, cy + w * 0.14f)
            lineTo(c.x - dw, cy + w * 0.035f)
            quadraticBezierTo(c.x, cy - w * 0.025f, c.x + dw, cy + w * 0.035f)
            lineTo(c.x + dw, cy + w * 0.14f)
            close()
        }
        drawPath(door, deep)
    }
}

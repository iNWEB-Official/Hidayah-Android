package com.hidayah.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.drawBehind
import kotlin.math.cos
import kotlin.math.sin

/** The 8-pointed star lattice from the Hidayah mockup — reusable behind any emerald surface. */
fun Modifier.hidayahStarLattice(): Modifier = drawBehind {
    val density = this.density
    val cell = 74f * density
    val starR = 15f * density
    val dotR = 1.6f * density
    val starColor = Color(0xFFD4AF37).copy(alpha = 0.17f)
    val dotColor = Color(0xFFD4AF37).copy(alpha = 0.30f)
    val cols = (size.width / cell).toInt() + 3
    val rows = (size.height / (cell * 0.92f)).toInt() + 3
    var y = -cell
    for (row in 0 until rows) {
        val xOffset = if (row % 2 == 0) 0f else cell / 2f
        var x = -cell + xOffset
        for (col in 0 until cols) {
            eightPointedStar(Offset(x, y), starR, starColor)
            drawCircle(dotColor, radius = dotR, center = Offset(x + cell / 2f, y + cell * 0.46f))
            x += cell
        }
        y += cell * 0.92f
    }
}

/** Deep-emerald background with the 8-pointed star lattice from the Hidayah mockup (00-cover). */
@Composable
fun HidayahBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF11493A), Color(0xFF0B3527), Color(0xFF062A20))
                )
            )
            .hidayahStarLattice()
    ) {
        content()
    }
}

internal fun androidx.compose.ui.graphics.drawscope.DrawScope.eightPointedStar(center: Offset, radius: Float, color: Color) {
    val inner = radius * 0.44f
    val path = Path()
    for (i in 0 until 16) {
        val angle = (Math.PI / 8) * i - Math.PI / 2
        val r = if (i % 2 == 0) radius else inner
        val px = center.x + (r * cos(angle)).toFloat()
        val py = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color = color, style = Stroke(width = radius * 0.09f))
}

package com.junkfood.seal.ui.page.downloadv2

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private val ChristmasRed = Color(0xFFCD2D32)
private val SnowWhite = Color(0xFFF2F7F3)

private data class Flake(
    val x: Float,
    val y0: Float,
    val speed: Float,
    val radius: Float,
    val phase: Float,
)

/** Nieve cayendo (animada) sobre toda la pantalla. No intercepta toques. */
@Composable
fun SnowfallOverlay(modifier: Modifier = Modifier) {
    val flakes = remember {
        val rnd = Random(42)
        List(48) {
            Flake(
                x = rnd.nextFloat(),
                y0 = rnd.nextFloat(),
                speed = 0.5f + rnd.nextFloat() * 0.8f,
                radius = 2f + rnd.nextFloat() * 3f,
                phase = rnd.nextFloat() * (2f * PI.toFloat()),
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "snowfall")
    val progress by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Restart),
            label = "snowProgress",
        )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        for (flake in flakes) {
            val y = ((flake.y0 + progress * flake.speed) % 1f) * h
            val x =
                flake.x * w +
                    sin(progress * 4f * PI.toFloat() * flake.speed + flake.phase) * 14f
            drawCircle(
                color = SnowWhite.copy(alpha = 0.85f),
                radius = flake.radius,
                center = Offset(x, y),
            )
        }
    }
}

/** Guirnalda de luces navideñas en la parte superior de la pantalla. */
@Composable
fun ChristmasGarland(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(30.dp)) {
        val w = size.width
        val baseY = 4.dp.toPx()
        val sag = 12.dp.toPx()
        val path =
            Path().apply {
                moveTo(0f, baseY)
                quadraticTo(w / 2f, baseY + sag * 2f, w, baseY)
            }
        drawPath(path, color = Color(0xFF1E5C40), style = Stroke(width = 2.dp.toPx()))
        val bulbColors =
            listOf(
                ChristmasRed,
                Color(0xFF2EA35C),
                Color(0xFFE8BE50),
                Color(0xFF4682E0),
                Color.White,
            )
        val count = 9
        for (i in 0 until count) {
            val t = (i + 0.5f) / count
            val x = t * w
            val y = baseY + 4f * t * (1f - t) * sag
            val cy = y + 9.dp.toPx()
            drawLine(
                color = Color(0xFF1E5C40),
                start = Offset(x, y),
                end = Offset(x, cy - 5.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
            )
            drawCircle(
                color = bulbColors[i % bulbColors.size],
                radius = 5.dp.toPx(),
                center = Offset(x, cy),
            )
            drawCircle(
                color = Color.White,
                radius = 1.5.dp.toPx(),
                center = Offset(x - 2.dp.toPx(), cy - 2.dp.toPx()),
            )
        }
    }
}

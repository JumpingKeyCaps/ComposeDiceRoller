package com.lebaillyapp.composediceroller.ui.composition

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.hypot

@Composable
fun NeonCirclesRefined(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    radius: Float = 200f,
    glowEnabled: Boolean = false,
    animated: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neonGlow")
    val glowProgress by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2
        val cy = size.height / 2

        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 6f

                val intensity = if (animated) glowProgress else 1f
                val shadowRadius = 25f + 40f * intensity

                this.color = if (glowEnabled)
                    color.copy(alpha = 0.9f * intensity).toArgb()//on
                else
                    color.copy(alpha = 0.01f).toArgb() // off

                if (glowEnabled) {
                    setShadowLayer(
                        shadowRadius,
                        0f,
                        0f,
                        color.copy(alpha = 0.95f * intensity).toArgb()
                    )
                } else {
                    clearShadowLayer()
                }
            }

            canvas.nativeCanvas.drawCircle(cx, cy, radius, paint)
        }
    }
}
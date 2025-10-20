package com.lebaillyapp.composediceroller.ui.composition

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.lebaillyapp.composediceroller.model.Vec3
import com.lebaillyapp.composediceroller.model.normalizeOrZero
import kotlin.math.max
import kotlin.math.min


@Composable
fun InteractiveCubeV3(
    modifier: Modifier = Modifier,
    size: Float = 300f
) {
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }

    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }

    // position du doigt pour reflets dynamiques
    var pointerPos by remember { mutableStateOf(Offset.Zero) }

    val damping = 0.92f

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        velocityX = 0f
                        velocityY = 0f
                        pointerPos = offset
                    },
                    onDragEnd = {
                        // inertie
                    },
                    onDrag = { change, dragAmount ->
                        val (dx, dy) = dragAmount
                        val factor = 0.004f
                        rotationY -= dx * factor
                        rotationX += dy * factor
                        velocityX = -dx * factor
                        velocityY = dy * factor
                        pointerPos = change.position
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val scale = min(this.size.width, this.size.height) * 0.2f

            val cubeSize = 1f
            val vertices = listOf(
                Vec3(-cubeSize, -cubeSize, -cubeSize),
                Vec3(cubeSize, -cubeSize, -cubeSize),
                Vec3(cubeSize, cubeSize, -cubeSize),
                Vec3(-cubeSize, cubeSize, -cubeSize),
                Vec3(-cubeSize, -cubeSize, cubeSize),
                Vec3(cubeSize, -cubeSize, cubeSize),
                Vec3(cubeSize, cubeSize, cubeSize),
                Vec3(-cubeSize, cubeSize, cubeSize)
            )

            val rotated = vertices.map { it.rotateX(rotationX).rotateY(rotationY) }

            val faces = listOf(
                listOf(0, 1, 2, 3) to Color(0xFFE74C3C),
                listOf(4, 5, 6, 7) to Color(0xFF3498DB),
                listOf(0, 1, 5, 4) to Color(0xFF2ECC71),
                listOf(2, 3, 7, 6) to Color(0xFFF39C12),
                listOf(0, 3, 7, 4) to Color(0xFF9B59B6),
                listOf(1, 2, 6, 5) to Color(0xFF1ABC9C)
            )

            val light = Vec3(0.5f, 0.7f, -1f).normalize()

            val facesWithDepth = faces.map { (indices, color) ->
                val fv = indices.map { rotated[it] }
                val avgZ = fv.map { it.z }.average()
                Triple(fv, color, avgZ)
            }.sortedByDescending { it.third }

            facesWithDepth.forEach { (fv, color, _) ->
                val v1 = fv[1] - fv[0]
                val v2 = fv[2] - fv[0]
                val normal = v1.cross(v2).normalize()

                val projected = fv.map { v ->
                    val perspective = 6f / (6f + v.z)
                    Offset(
                        center.x + v.x * scale * perspective,
                        center.y - v.y * scale * perspective
                    )
                }

                val path = Path().apply {
                    moveTo(projected[0].x, projected[0].y)
                    projected.drop(1).forEach { lineTo(it.x, it.y) }
                    close()
                }

                // Reflets dynamiques selon position du doigt
                val reflectDir = (pointerPos - center).normalizeOrZero()
                val reflectBrightness = 0.5f + 0.5f * max(0f, normal.dot(
                    Vec3(
                        reflectDir.x,
                        -reflectDir.y,
                        -0.5f
                    ).normalize()))
                val brightness = (normal.dot(light).coerceIn(0f, 1f) * 0.6f + 0.4f) * reflectBrightness

                val shadedColor = color.copy(
                    red = (color.red * brightness).coerceIn(0f,1f),
                    green = (color.green * brightness).coerceIn(0f,1f),
                    blue = (color.blue * brightness).coerceIn(0f,1f),
                    alpha = 0.9f
                )

                val gradient = Brush.linearGradient(
                    colors = listOf(shadedColor, Color.White.copy(alpha = 0.25f)),
                    start = projected[0],
                    end = projected[2]
                )

                drawPath(path, gradient)
                drawPath(path, Color.Black.copy(alpha = 0.25f), style = Stroke(1.5f))
            }

            // Apply inertia
            rotationX += velocityY
            rotationY += velocityX
            velocityX *= damping
            velocityY *= damping
        }
    }
}
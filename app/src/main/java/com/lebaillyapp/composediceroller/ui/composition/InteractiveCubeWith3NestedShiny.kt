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
fun InteractiveCubeWith3NestedShiny(
    modifier: Modifier = Modifier,
    size: Float = 300f,
    damping: Float = 0.99f,
    dragFactor: Float = 0.004f,
    innerCubeRatio1: Float = 0.90f,  // cube 2
    innerCubeRatio2: Float = 0.3f,   // cube 3
    innerLagFactor: Float = 0.2f,    // lag cube 3
    parentColors: List<Color> = listOf(
        Color(0xFFE74C3C),
        Color(0xFF3498DB),
        Color(0xFF2ECC71),
        Color(0xFFF39C12),
        Color(0xFF9B59B6),
        Color(0xFF1ABC9C)
    ),
    parentAlpha: Float = 0.3f,
    innerColors1: List<Color>? = null,
    innerAlpha1: Float = 0.5f,
    innerColors2: List<Color>? = null,
    innerAlpha2: Float = 0.7f
) {
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }

    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }

    var innerRotationX by remember { mutableStateOf(0f) }
    var innerRotationY by remember { mutableStateOf(0f) }

    var pointerPos by remember { mutableStateOf(Offset.Zero) }

    val innerCubeColors1 = innerColors1 ?: parentColors
    val innerCubeColors2 = innerColors2 ?: parentColors

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
                    onDragEnd = {},
                    onDrag = { change, dragAmount ->
                        rotationY -= dragAmount.x * dragFactor
                        rotationX += dragAmount.y * dragFactor
                        velocityX = -dragAmount.x * dragFactor
                        velocityY = dragAmount.y * dragFactor
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
            val baseVertices = listOf(
                Vec3(-cubeSize, -cubeSize, -cubeSize),
                Vec3(cubeSize, -cubeSize, -cubeSize),
                Vec3(cubeSize, cubeSize, -cubeSize),
                Vec3(-cubeSize, cubeSize, -cubeSize),
                Vec3(-cubeSize, -cubeSize, cubeSize),
                Vec3(cubeSize, -cubeSize, cubeSize),
                Vec3(cubeSize, cubeSize, cubeSize),
                Vec3(-cubeSize, cubeSize, cubeSize)
            )

            val innerVertices1 = baseVertices.map { it * innerCubeRatio1 }
            val innerVertices2 = baseVertices.map { it * innerCubeRatio2 }

            val faces = listOf(
                listOf(0, 1, 2, 3),
                listOf(4, 5, 6, 7),
                listOf(0, 1, 5, 4),
                listOf(2, 3, 7, 6),
                listOf(0, 3, 7, 4),
                listOf(1, 2, 6, 5)
            )

            val light = Vec3(0.5f, 0.7f, -1f).normalize()
            val cameraDir = Vec3(0f, 0f, -1f)

            fun drawCubeShiny(vertices: List<Vec3>, colors: List<Color>, alpha: Float, rotX: Float, rotY: Float) {
                val rotated = vertices.map { it.rotateX(rotX).rotateY(rotY) }

                val facesWithDepth = faces.mapIndexed { i, indices ->
                    val fv = indices.map { rotated[it] }
                    val avgZ = fv.map { it.z }.average()
                    Triple(fv, colors[i % colors.size], avgZ)
                }.sortedByDescending { it.third }

                facesWithDepth.forEach { (fv, color, _) ->
                    val v1 = fv[1] - fv[0]
                    val v2 = fv[2] - fv[0]
                    val normal = v1.cross(v2).normalize()

                    val projected = fv.map { v ->
                        val perspective = 6f / (6f + v.z)
                        Offset(center.x + v.x * scale * perspective,
                            center.y - v.y * scale * perspective)
                    }

                    val path = Path().apply {
                        moveTo(projected[0].x, projected[0].y)
                        projected.drop(1).forEach { lineTo(it.x, it.y) }
                        close()
                    }

                    // Fresnel / glow effect
                    val dotView = max(0f, normal.dot(cameraDir))
                    val fresnel = 0.2f + 0.8f * (1f - dotView)

                    val reflectDir = (pointerPos - center).normalizeOrZero()
                    val reflectBrightness = 0.5f + 0.5f * max(0f, normal.dot(
                        Vec3(
                            reflectDir.x,
                            -reflectDir.y,
                            -0.5f
                        ).normalize()))

                    val brightness = (normal.dot(light).coerceIn(0f,1f) * 0.6f + 0.4f) * reflectBrightness * fresnel

                    val shadedColor = color.copy(
                        red = (color.red * brightness).coerceIn(0f,1f),
                        green = (color.green * brightness).coerceIn(0f,1f),
                        blue = (color.blue * brightness).coerceIn(0f,1f),
                        alpha = alpha
                    )

                    val gradient = Brush.linearGradient(
                        colors = listOf(shadedColor, Color.White.copy(alpha = 0.25f)),
                        start = projected[0],
                        end = projected[2]
                    )

                    drawPath(path, gradient)
                    drawPath(path, Color.Black.copy(alpha = 0.2f), style = Stroke(1.5f))
                }
            }

            // parent cube
            drawCubeShiny(baseVertices, parentColors, parentAlpha, rotationX, rotationY)

            // inner cube 1
            if (innerCubeRatio1 > 0f)
                drawCubeShiny(innerVertices1, innerCubeColors1, innerAlpha1, rotationX, rotationY)

            // inner cube 2 (lag inertiel + glow)
            if (innerCubeRatio2 > 0f) {
                innerRotationX += (rotationX - innerRotationX) * innerLagFactor
                innerRotationY += (rotationY - innerRotationY) * innerLagFactor
                drawCubeShiny(innerVertices2, innerCubeColors2, innerAlpha2, innerRotationX, innerRotationY)
            }

            // inertia parent
            rotationX += velocityY
            rotationY += velocityX
            velocityX *= damping
            velocityY *= damping
        }
    }
}
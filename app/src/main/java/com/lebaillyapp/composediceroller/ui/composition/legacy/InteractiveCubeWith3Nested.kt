package com.lebaillyapp.composediceroller.ui.composition.legacy

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.lebaillyapp.composediceroller.model.Vec3
import kotlin.math.max
import kotlin.math.min

@Composable
fun InteractiveCubeWith3Nested(
    modifier: Modifier = Modifier,
    size: Float = 300f,
    damping: Float = 0.99f,
    dragFactor: Float = 0.004f,
    innerCubeRatio1: Float = 0.95f,  // ratio cube 2
    innerCubeRatio2: Float = 0.4f,  // ratio cube 3 à l'intérieur du cube 2
    parentColors: List<Color> = listOf(
        Color(0xFFE74C3C),
        Color(0xFF3498DB),
        Color(0xFF2ECC71),
        Color(0xFFF39C12),
        Color(0xFF9B59B6),
        Color(0xFF1ABC9C)
    ),
    parentAlpha: Float = 0.55f,
    innerColors1: List<Color>? = null, // cube 2
    innerAlpha1: Float = 0.8f,
    innerColors2: List<Color>? = null, // cube 3
    innerAlpha2: Float = 0.9f
) {
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }

    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }

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

            fun drawCube(vertices: List<Vec3>, colors: List<Color>, alpha: Float) {
                val rotated = vertices.map { it.rotateX(rotationX).rotateY(rotationY) }

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

                    val brightness = max(0.3f, normal.dot(light).coerceIn(0f,1f))
                    val shadedColor = color.copy(
                        red = color.red * brightness,
                        green = color.green * brightness,
                        blue = color.blue * brightness,
                        alpha = alpha
                    )

                    drawPath(path, shadedColor)
                    drawPath(path, Color.Black.copy(alpha = 0.2f), style = Stroke(1.5f))
                }
            }

            // parent cube
            drawCube(baseVertices, parentColors, parentAlpha)

            // inner cube 1
            if (innerCubeRatio1 > 0f) drawCube(innerVertices1, innerCubeColors1, innerAlpha1)

            // inner cube 2
            if (innerCubeRatio2 > 0f) drawCube(innerVertices2, innerCubeColors2, innerAlpha2)

            // inertia
            rotationX += velocityY
            rotationY += velocityX
            velocityX *= damping
            velocityY *= damping
        }
    }
}
package com.lebaillyapp.composediceroller.ui.composition

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.lebaillyapp.composediceroller.model.CubeConfig
import kotlin.math.*

@Composable
fun InteractiveDiceComposable(
    modifier: Modifier = Modifier,
    size: Float = 120f,
    damping: Float = 0.99f,
    dragFactor: Float = 0.003f,
    pipPadding: Float = 0.08f,
    pipRadius: Float = 0.18f,
    pipColor: Color = Color.White,
    showWires: Boolean = false,
    faceColors: List<Color>? = null,
    cubeConfig: CubeConfig = CubeConfig.createDefaultDice()
) {
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }
    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }

    val appliedFaceColors = faceColors ?: cubeConfig.faces.map { it.color }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        velocityX = 0f
                        velocityY = 0f
                    },
                    onDrag = { _, drag ->
                        rotationY -= drag.x * dragFactor
                        rotationX += drag.y * dragFactor
                        velocityX = -drag.x * dragFactor
                        velocityY = drag.y * dragFactor
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val scale = min(this.size.width, this.size.height) * 0.25f

            val rotatedVertices = cubeConfig.vertices.map { it.rotateX(rotationX).rotateY(rotationY) }
            val light = Vec3(0.5f, 0.7f, -1f).normalize()

            val facesWithDepth = cubeConfig.faces.mapIndexed { index, face ->
                val fv = face.indices.map { rotatedVertices[it] }
                val avgZ = fv.map { it.z }.average()
                Triple(face.copy(color = appliedFaceColors.getOrElse(index) { face.color }), fv, avgZ)
            }.sortedByDescending { it.third }

            facesWithDepth.forEach { (face, fv, _) ->
                val v1 = fv[1] - fv[0]
                val v2 = fv[3] - fv[0]
                val normal = v1.cross(v2).normalize()

                val brightness = max(0.3f, normal.dot(light).coerceIn(0f, 1f))

                val projected = fv.map { v ->
                    val perspective = 5f / (5f + v.z)
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

                drawPath(path, face.color.copy(
                    red = face.color.red * brightness,
                    green = face.color.green * brightness,
                    blue = face.color.blue * brightness,
                    alpha = 0.95f
                ))

                if(showWires){
                    drawPath(path, Color.Black.copy(alpha = 0.25f), style = Stroke(1.5f))
                }


                face.pips.forEach { pip ->
                    val u = pipPadding + pip.x * (1f - 2f * pipPadding)
                    val v = pipPadding + pip.y * (1f - 2f * pipPadding)

                    val uVec = fv[1] - fv[0]
                    val vVec = fv[3] - fv[0]
                    val pipCenter3D = fv[0] + uVec * u + vVec * v

                    val steps = 24
                    val pipPoints = (0 until steps).map { i ->
                        val angle = i / steps.toFloat() * 2 * PI.toFloat()
                        val dx = cos(angle) * pipRadius
                        val dy = sin(angle) * pipRadius
                        val point3D = pipCenter3D + uVec.normalize() * dx + vVec.normalize() * dy
                        val perspective = 5f / (5f + point3D.z)
                        Offset(
                            center.x + point3D.x * scale * perspective,
                            center.y - point3D.y * scale * perspective
                        )
                    }

                    val pipPath = Path().apply {
                        moveTo(pipPoints.first().x, pipPoints.first().y)
                        pipPoints.drop(1).forEach { lineTo(it.x, it.y) }
                        close()
                    }

                    drawPath(pipPath, pipColor)
                }
            }

            rotationX += velocityY
            rotationY += velocityX
            velocityX *= damping
            velocityY *= damping
        }
    }
}

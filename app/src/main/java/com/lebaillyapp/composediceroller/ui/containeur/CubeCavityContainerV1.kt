package com.lebaillyapp.composediceroller.ui.containeur

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.lebaillyapp.composediceroller.model.Vec3
import kotlin.math.min

@Composable
fun CubeCavityContainerV1(
    modifier: Modifier = Modifier,
    size: Float = 400f,
    depth: Float = 1.14f,
    perspectiveFactor: Float = 9.55f,
    rotationX: Float = -0.01f,
    rotationY: Float = -0.032f,
    lightDirection: Vec3 = Vec3(0.5f, 0.7f, -1f),
    cavityAlpha: Float = 0.55f,
    backgroundColor: Color = Color(0xFFE8E6E6), // Couleur claire du fond
    shadowIntensity: Float = 0.25f,              // Force des ombres internes
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {

            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val scale = min(this.size.width, this.size.height) * 0.25f

            val cubeSize = depth
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

            val faces = listOf(
                listOf(0, 1, 2, 3), // back
                listOf(4, 5, 6, 7), // front
                listOf(0, 1, 5, 4), // bottom
                listOf(2, 3, 7, 6), // top
                listOf(0, 3, 7, 4), // left
                listOf(1, 2, 6, 5)  // right
            )

            val rotated = vertices.map { it.rotateX(rotationX).rotateY(rotationY) }
            val facesWithDepth = faces.map { indices ->
                val fv = indices.map { rotated[it] }
                val avgZ = fv.map { it.z }.average()
                Pair(fv, avgZ)
            }.sortedByDescending { it.second }

            facesWithDepth.forEach { (fv, _) ->
                val v1 = fv[1] - fv[0]
                val v2 = fv[2] - fv[0]
                val normal = v1.cross(v2).normalize()

                val projected = fv.map { v ->
                    val perspective = perspectiveFactor / (perspectiveFactor + v.z)
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

                // Luminance de la face (simulation de l’ombre)
                val brightness = (normal.dot(lightDirection).coerceIn(-1f, 1f) * 0.5f + 0.5f)
                val darken = 1f - (1f - brightness) * shadowIntensity

                // Couleur de base issue du fond, légèrement modifiée selon la lumière
                val shadedColor = backgroundColor.copy(
                    red = backgroundColor.red * darken,
                    green = backgroundColor.green * darken,
                    blue = backgroundColor.blue * darken,
                    alpha = cavityAlpha
                )

                drawPath(path, shadedColor)
            }
        }

        // Contenu au centre
        Box(
            modifier = Modifier
                .size((size * 0.7f).dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

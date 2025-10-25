package com.lebaillyapp.composediceroller.ui.containeur.legacy

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
import com.lebaillyapp.composediceroller.model.dice.Vec3
import com.lebaillyapp.composediceroller.model.dice.state.RotationState
import kotlin.math.max
import kotlin.math.min



@Composable
fun CubeCavityContainerV2(
    modifier: Modifier = Modifier,
    size: Float = 500f, // MODIFIÉ
    scaleFactor: Float = 0.3f, // AJOUTÉ
    damping: Float = 0.99f,
    dragFactor: Float = 0.004f,

    isParentInteractive: Boolean = true,
    isInner1Interactive: Boolean = true,
    isInner2Interactive: Boolean = true,
    isInner3Interactive: Boolean = true,

    // Cube Parent (Cube 1) - Palette : Bleu Marine Profond (Dark Sapphire)
    parentColors: List<Color> = listOf(
        Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF0D1B2A),
        Color(0xFF1B263B), Color(0xFF0D1B2A), Color(0xFF1B263B)
    ),
    parentAlpha: Float = 0.05f, // MODIFIÉ

    // Cube 2 (Inner 1)
    innerRatio1: Float = 0.95f,
    innerLagFactor1: Float = 1.0f,
    innerColors1: List<Color> = listOf(
        Color(0xFF415A77), Color(0xFF415A77), Color(0xFF778DA9),
        Color(0xFF778DA9), Color(0xFF415A77), Color(0xFF778DA9)
    ),
    innerAlpha1: Float = 0.05f, // MODIFIÉ

    // Cube 3 (Inner 2)
    innerRatio2: Float = 0.5f, // MODIFIÉ
    innerLagFactor2: Float = 0.2f, // MODIFIÉ
    innerColors2: List<Color> = listOf(
        Color(0xFFE74C3C), Color(0xFF3498DB), Color(0xFF2ECC71),
        Color(0xFFF39C12), Color(0xFF9B59B6), Color(0xFF1ABC9C)
    ),
    innerAlpha2: Float = 0.55f,

    // Cube 4 (Inner 3)
    innerRatio3: Float = 0.25f,
    innerLagFactor3: Float = 0.05f, // MODIFIÉ
    innerColors3: List<Color> = listOf(
        Color(0xFFF50057), Color(0xFF00B0FF), Color(0xFF00E676),
        Color(0xFFFF9100), Color(0xFF651FFF), Color(0xFF1DE9B6)
    ),
    innerAlpha3: Float = 0.95f
) {
    // Rotation cible globale (Source de Vérité) - TOUJOURS mise à jour par le Drag
    var targetRotationX by remember { mutableStateOf(0f) }
    var targetRotationY by remember { mutableStateOf(0f) }
    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }

    val scaleFac by remember { mutableStateOf(scaleFactor) } // AJOUTÉ

    // État fixe du Cube 1 (Parent) quand il est verrouillé
    val parentFixedRotationX = remember { mutableStateOf(0f) }
    val parentFixedRotationY = remember { mutableStateOf(0f) }
    val wasParentInteractive = remember { mutableStateOf(true) }

    // Rotation et Verrouillage des cubes intérieurs
    val rotState1 = remember { RotationState() }
    val rotState2 = remember { RotationState() }
    val rotState3 = remember { RotationState() }


    Box(
        modifier = modifier
            .fillMaxSize()
            // L'interaction de drag met TOUJOURS à jour la cible globale
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { velocityX = 0f; velocityY = 0f },
                    onDragEnd = {},
                    onDrag = { _, dragAmount ->
                        // L'input met TOUJOURS à jour la cible de rotation globale
                        targetRotationY -= dragAmount.x * dragFactor
                        targetRotationX += dragAmount.y * dragFactor
                        velocityX = -dragAmount.x * dragFactor
                        velocityY = dragAmount.y * dragFactor
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val scale = min(this.size.width, this.size.height) * scaleFac // UTILISATION DE scaleFac

            val cubeSize = 1f
            // Définition des 8 sommets de base du cube
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

            // Faces (indices des sommets)
            val faces = listOf(
                listOf(0, 1, 2, 3), listOf(4, 5, 6, 7),
                listOf(0, 1, 5, 4), listOf(2, 3, 7, 6),
                listOf(0, 3, 7, 4), listOf(1, 2, 6, 5)
            )

            val light = Vec3(0.5f, 0.7f, -1f).normalize()

            fun drawCube(
                vertices: List<Vec3>,
                colors: List<Color>,
                alpha: Float,
                rotX: Float,
                rotY: Float
            ) {
                val rotated = vertices.map { it.rotateX(rotX).rotateY(rotY) }

                // Tri des faces par profondeur (avgZ)
                val facesWithDepth = faces.mapIndexed { i, indices ->
                    val fv = indices.map { rotated[it] }
                    val avgZ = fv.map { it.z }.average()
                    Triple(fv, colors[i % colors.size], avgZ)
                }.sortedByDescending { it.third }

                facesWithDepth.forEach { (fv, color, _) ->
                    // Calcul de la normale pour l'éclairage
                    val v1 = fv[1] - fv[0]
                    val v2 = fv[2] - fv[0]
                    val normal = v1.cross(v2).normalize()

                    // Projection 3D -> 2D (Perspective)
                    val projected = fv.map { v ->
                        val perspective = 6f / (6f + v.z)
                        Offset(
                            center.x + v.x * scale * perspective,
                            center.y - v.y * scale * perspective
                        )
                    }

                    // Création du chemin (Path)
                    val path = Path().apply {
                        moveTo(projected[0].x, projected[0].y)
                        projected.drop(1).forEach { lineTo(it.x, it.y) }
                        close()
                    }

                    // Application de l'ombre/lumière
                    val brightness = max(0.3f, normal.dot(light).coerceIn(0f, 1f))
                    val shadedColor = color.copy(
                        red = color.red * brightness,
                        green = color.green * brightness,
                        blue = color.blue * brightness,
                        alpha = alpha
                    )

                    // Dessin de la face et du contour
                    drawPath(path, shadedColor)
                    drawPath(path, Color.Black.copy(alpha = 0.2f), style = Stroke(1.5f))
                }
            }

            // --- Mise à jour de l'inertie de la cible globale ---
            targetRotationX += velocityY
            targetRotationY += velocityX
            velocityX *= damping
            velocityY *= damping

            // --- Logique de Verrouillage du Cube 1 (Parent) ---
            if (wasParentInteractive.value && !isParentInteractive) {
                parentFixedRotationX.value = targetRotationX
                parentFixedRotationY.value = targetRotationY
            }
            wasParentInteractive.value = isParentInteractive

            // Détermine quelle rotation utiliser pour le dessin du Cube 1
            val parentCubeRotationX = if (isParentInteractive) targetRotationX else parentFixedRotationX.value
            val parentCubeRotationY = if (isParentInteractive) targetRotationY else parentFixedRotationY.value

            // --- Logique de Suivi des Cubes Intérieurs (Mise à jour des états) ---

            // 2. Cube Intérieur 1 (Cube 2)
            if (innerRatio1 > 0f && isInner1Interactive) {
                rotState1.rotX += (targetRotationX - rotState1.rotX) * innerLagFactor1
                rotState1.rotY += (targetRotationY - rotState1.rotY) * innerLagFactor1
            }

            // 3. Cube Intérieur 2 (Cube 3)
            if (innerRatio2 > 0f && isInner2Interactive) {
                rotState2.rotX += (targetRotationX - rotState2.rotX) * innerLagFactor2
                rotState2.rotY += (targetRotationY - rotState2.rotY) * innerLagFactor2
            }

            // 4. Cube Intérieur 3 (Cube 4)
            if (innerRatio3 > 0f && isInner3Interactive) {
                rotState3.rotX += (targetRotationX - rotState3.rotX) * innerLagFactor3
                rotState3.rotY += (targetRotationY - rotState3.rotY) * innerLagFactor3
            }

            // ===============================================
            // ORDRE DE DESSIN : DU PLUS PETIT AU PLUS GRAND (Arrière vers Avant)
            // ===============================================

            // 1. Dessin du cube intérieur 3 (Cube 4 - Le plus petit, le plus profond)
            if (innerRatio3 > 0f) {
                drawCube(
                    baseVertices.map { it * innerRatio3 },
                    innerColors3,
                    innerAlpha3,
                    rotState3.rotX,
                    rotState3.rotY
                )
            }

            // 2. Dessin du cube intérieur 2 (Cube 3)
            if (innerRatio2 > 0f) {
                drawCube(
                    baseVertices.map { it * innerRatio2 },
                    innerColors2,
                    innerAlpha2,
                    rotState2.rotX,
                    rotState2.rotY
                )
            }

            // 3. Dessin du cube intérieur 1 (Cube 2)
            if (innerRatio1 > 0f) {
                drawCube(
                    baseVertices.map { it * innerRatio1 },
                    innerColors1,
                    innerAlpha1,
                    rotState1.rotX,
                    rotState1.rotY
                )
            }

            // 4. Dessin du cube parent (Cube 1 - Le plus grand, le plus proche)
            drawCube(baseVertices, parentColors, parentAlpha, parentCubeRotationX, parentCubeRotationY)
        }
    }
}



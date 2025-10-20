package com.lebaillyapp.composediceroller.ui.composition

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import com.lebaillyapp.composediceroller.model.DiceLayerConfig
import com.lebaillyapp.composediceroller.model.LayerLockState
import com.lebaillyapp.composediceroller.model.RotationState
import com.lebaillyapp.composediceroller.model.Vec3
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Composable de dé interactif multi-couches avec système de lag et effets visuels avancés.
 *
 * Supporte jusqu'à 4 couches de dés imbriqués, chacune avec :
 * - Configuration indépendante (faces, couleurs, pips)
 * - Rotation avec lag/traînée
 * - Inversion d'axes
 * - Verrouillage positionnel
 * - Transparence
 *
 * @param modifier Modificateur Compose
 * @param layers Liste des configurations de couches (max 4 recommandé)
 * @param layerLocks Liste des états de verrouillage par couche (si vide, aucun cube n'est verrouillé)
 * @param size Taille du canvas en dp
 * @param scaleFactor Facteur d'échelle de rendu (0.25 = 25% de la taille du canvas)
 * @param damping Facteur d'amortissement de l'inertie (0.99 = 99% de conservation)
 * @param dragFactor Sensibilité du drag (plus élevé = rotation plus rapide)
 * @param pipPadding Padding des pips par rapport aux bords de la face (0.08 = 8%)
 * @param pipRadius Rayon des pips en proportion de la taille de la face (0.18 = 18%)
 * @param pipColor Couleur des pips
 * @param isParentInteractive Si false, le premier cube (parent) se fige
 */
@Composable
fun NestedInteractiveDice(
    modifier: Modifier = Modifier,
    layers: List<DiceLayerConfig>,
    layerLocks: List<LayerLockState> = emptyList(),
    size: Float = 300f,
    scaleFactor: Float = 0.25f,
    damping: Float = 0.99f,
    dragFactor: Float = 0.004f,
    pipPadding: Float = 0.08f,
    pipRadius: Float = 0.18f,
    pipColor: Color = Color.White,
    isParentInteractive: Boolean = true
) {
    var targetRotationX by remember { mutableStateOf(0f) }
    var targetRotationY by remember { mutableStateOf(0f) }
    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }

    val parentFixedRotationX = remember { mutableStateOf(0f) }
    val parentFixedRotationY = remember { mutableStateOf(0f) }
    val wasParentInteractive = remember { mutableStateOf(true) }

    val rotationStates = remember { List(layers.size) { RotationState() } }

    fun applyInversion(value: Float, invert: Boolean): Float =
        if (invert) -value else value

    // --- Animation du zoom central ---
    val cubeScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        velocityX = 0f
                        velocityY = 0f
                        coroutineScope.launch {
                            cubeScale.animateTo(
                                targetValue = 0.7f,
                                animationSpec = tween(
                                    durationMillis = 120,
                                    easing = LinearOutSlowInEasing
                                )
                            )
                        }
                    },
                    onDrag = { _, dragAmount ->
                        targetRotationY -= dragAmount.x * dragFactor
                        targetRotationX += dragAmount.y * dragFactor
                        velocityX = -dragAmount.x * dragFactor
                        velocityY = dragAmount.y * dragFactor
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            cubeScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            cubeScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val scale = min(this.size.width, this.size.height) * scaleFactor
            val light = Vec3(0.5f, 0.7f, -1f).normalize()

            // === Inertie ===
            targetRotationX += velocityY
            targetRotationY += velocityX
            velocityX *= damping
            velocityY *= damping

            // === Lock du parent ===
            if (wasParentInteractive.value && !isParentInteractive) {
                parentFixedRotationX.value = targetRotationX
                parentFixedRotationY.value = targetRotationY
            }
            wasParentInteractive.value = isParentInteractive

            // === Mise à jour du lag ===
            layers.forEachIndexed { index, layer ->
                val lockState = layerLocks.getOrNull(index)
                val isLocked = lockState?.isLocked == true
                val interactive = layer.isInteractive && !isLocked
                if (layer.isEnabled && interactive) {
                    val state = rotationStates[index]
                    state.rotX += (targetRotationX - state.rotX) * layer.lagFactor
                    state.rotY += (targetRotationY - state.rotY) * layer.lagFactor
                }
            }

            // === Dessin ===
            data class FaceWithLayer(
                val face: com.lebaillyapp.composediceroller.model.FaceConfig,
                val fv: List<Vec3>,
                val avgZ: Double,
                val layer: DiceLayerConfig
            )

            val allFaces = mutableListOf<FaceWithLayer>()

            layers.forEachIndexed { index, layer ->
                if (!layer.isEnabled) return@forEachIndexed
                val lockState = layerLocks.getOrNull(index)
                val isLocked = lockState?.isLocked == true

                val (baseRotX, baseRotY) = when {
                    isLocked -> lockState!!.targetRotX to lockState.targetRotY
                    index == 0 -> if (isParentInteractive)
                        targetRotationX to targetRotationY
                    else
                        parentFixedRotationX.value to parentFixedRotationY.value
                    else -> rotationStates[index].rotX to rotationStates[index].rotY
                }

                val rotX = applyInversion(baseRotX, layer.invertRotationX)
                val rotY = applyInversion(baseRotY, layer.invertRotationY)

                // Applique cubeScale uniquement sur la 2e couche (index 1)
                val effectiveRatio = if (index == 2)
                    layer.ratio * cubeScale.value
                else
                    layer.ratio

                val vertices = layer.cubeConfig.vertices.map { it * effectiveRatio }
                val rotated = vertices.map { it.rotateX(rotX).rotateY(rotY) }

                layer.cubeConfig.faces.forEach { face ->
                    val fv = face.indices.map { rotated[it] }
                    val avgZ = fv.map { it.z }.average()
                    allFaces.add(FaceWithLayer(face, fv, avgZ, layer))
                }
            }

            allFaces.sortedByDescending { it.avgZ }.forEach { (face, fv, _, layer) ->
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

                val shadedColor = face.color.copy(
                    red = face.color.red * brightness,
                    green = face.color.green * brightness,
                    blue = face.color.blue * brightness,
                    alpha = layer.alpha
                )

                drawPath(path, shadedColor)

                if (layer.showEdges)
                    drawPath(path, Color.Black.copy(alpha = 0.25f), style = Stroke(1.5f))

                if (layer.showPips) {
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
            }
        }
    }
}
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
import com.lebaillyapp.composediceroller.model.DiceLayerConfig
import com.lebaillyapp.composediceroller.model.LayerLockState
import com.lebaillyapp.composediceroller.ui.utils.RotationState
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
    // Rotation cible globale (Source de Vérité) - TOUJOURS mise à jour par le Drag
    var targetRotationX by remember { mutableStateOf(0f) }
    var targetRotationY by remember { mutableStateOf(0f) }
    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }

    // État fixe du cube parent quand il est verrouillé
    val parentFixedRotationX = remember { mutableStateOf(0f) }
    val parentFixedRotationY = remember { mutableStateOf(0f) }
    val wasParentInteractive = remember { mutableStateOf(true) }

    // États de rotation pour chaque couche (max 4)
    val rotationStates = remember {
        List(layers.size) { RotationState() }
    }

    // Fonction helper pour appliquer l'inversion
    fun applyInversion(value: Float, invert: Boolean): Float {
        return if (invert) -value else value
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        velocityX = 0f
                        velocityY = 0f
                    },
                    onDrag = { _, dragAmount ->
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
            val scale = min(this.size.width, this.size.height) * scaleFactor

            val light = Vec3(0.5f, 0.7f, -1f).normalize()

            // Fonction interne pour dessiner un cube avec sa config complète
            fun drawDiceLayer(
                layer: DiceLayerConfig,
                rotX: Float,
                rotY: Float
            ) {
                val vertices = layer.cubeConfig.vertices.map { it * layer.ratio }
                val rotatedVertices = vertices.map { it.rotateX(rotX).rotateY(rotY) }

                // Tri des faces par profondeur (z-sorting)
                val facesWithDepth = layer.cubeConfig.faces.mapIndexed { index, face ->
                    val fv = face.indices.map { rotatedVertices[it] }
                    val avgZ = fv.map { it.z }.average()
                    Triple(face, fv, avgZ)
                }.sortedByDescending { it.third }

                facesWithDepth.forEach { (face, fv, _) ->
                    // Calcul de la normale pour l'éclairage
                    val v1 = fv[1] - fv[0]
                    val v2 = fv[3] - fv[0]
                    val normal = v1.cross(v2).normalize()

                    val brightness = max(0.3f, normal.dot(light).coerceIn(0f, 1f))

                    // Projection 3D -> 2D avec perspective
                    val projected = fv.map { v ->
                        val perspective = 5f / (5f + v.z)
                        Offset(
                            center.x + v.x * scale * perspective,
                            center.y - v.y * scale * perspective
                        )
                    }

                    // Création du chemin de la face
                    val path = Path().apply {
                        moveTo(projected[0].x, projected[0].y)
                        projected.drop(1).forEach { lineTo(it.x, it.y) }
                        close()
                    }

                    // Application de l'éclairage
                    val shadedColor = face.color.copy(
                        red = face.color.red * brightness,
                        green = face.color.green * brightness,
                        blue = face.color.blue * brightness,
                        alpha = layer.alpha
                    )

                    // Dessin de la face
                    drawPath(path, shadedColor)

                    // Dessin des arêtes si activé
                    if (layer.showEdges) {
                        drawPath(path, Color.Black.copy(alpha = 0.25f), style = Stroke(1.5f))
                    }

                    // Dessin des pips si activé
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

            // === Mise à jour de l'inertie de la cible globale ===
            targetRotationX += velocityY
            targetRotationY += velocityX
            velocityX *= damping
            velocityY *= damping

            // === Logique de verrouillage du cube parent (layer 0) ===
            if (wasParentInteractive.value && !isParentInteractive) {
                parentFixedRotationX.value = targetRotationX
                parentFixedRotationY.value = targetRotationY
            }
            wasParentInteractive.value = isParentInteractive

            // === Mise à jour des états de rotation pour chaque couche ===
            layers.forEachIndexed { index, layer ->
                // Récupère l'état de lock pour cette couche (si existe)
                val lockState = layerLocks.getOrNull(index)
                val isLayerLocked = lockState?.isLocked == true

                // Si la couche est lockée, elle n'est plus interactive
                val isEffectivelyInteractive = layer.isInteractive && !isLayerLocked

                if (layer.isEnabled && isEffectivelyInteractive) {
                    val state = rotationStates[index]
                    state.rotX += (targetRotationX - state.rotX) * layer.lagFactor
                    state.rotY += (targetRotationY - state.rotY) * layer.lagFactor
                }
            }

            // === DESSIN : Collecte toutes les faces de tous les cubes ===
            data class FaceWithLayer(
                val face: com.lebaillyapp.composediceroller.model.FaceConfig,
                val fv: List<Vec3>,
                val avgZ: Double,
                val layer: DiceLayerConfig
            )

            val allFaces = mutableListOf<FaceWithLayer>()

            layers.forEachIndexed { index, layer ->
                if (layer.isEnabled) {
                    // Récupère l'état de lock pour cette couche
                    val lockState = layerLocks.getOrNull(index)
                    val isLayerLocked = lockState?.isLocked == true

                    // Détermine la rotation à utiliser
                    val (baseRotX, baseRotY) = if (isLayerLocked) {
                        // Si locked, utilise la rotation définie dans le lock state
                        lockState!!.targetRotX to lockState.targetRotY
                    } else if (index == 0) {
                        // Cube parent : suit isParentInteractive
                        if (isParentInteractive) {
                            targetRotationX to targetRotationY
                        } else {
                            parentFixedRotationX.value to parentFixedRotationY.value
                        }
                    } else {
                        // Cubes intérieurs : utilisent leur état de rotation
                        rotationStates[index].rotX to rotationStates[index].rotY
                    }

                    // Application des inversions
                    val finalRotX = applyInversion(baseRotX, layer.invertRotationX)
                    val finalRotY = applyInversion(baseRotY, layer.invertRotationY)

                    // Calcul des vertices pour ce cube
                    val vertices = layer.cubeConfig.vertices.map { it * layer.ratio }
                    val rotatedVertices = vertices.map { it.rotateX(finalRotX).rotateY(finalRotY) }

                    // Ajout des faces à la liste globale
                    layer.cubeConfig.faces.forEach { face ->
                        val fv = face.indices.map { rotatedVertices[it] }
                        val avgZ = fv.map { it.z }.average()
                        allFaces.add(FaceWithLayer(face, fv, avgZ, layer))
                    }
                }
            }

            // Tri GLOBAL par profondeur Z
            allFaces.sortedByDescending { it.avgZ }.forEach { faceData ->
                val (face, fv, _, layer) = faceData

                // Calcul de la normale pour l'éclairage
                val v1 = fv[1] - fv[0]
                val v2 = fv[3] - fv[0]
                val normal = v1.cross(v2).normalize()

                val brightness = max(0.3f, normal.dot(light).coerceIn(0f, 1f))

                // Projection 3D -> 2D avec perspective
                val projected = fv.map { v ->
                    val perspective = 5f / (5f + v.z)
                    Offset(
                        center.x + v.x * scale * perspective,
                        center.y - v.y * scale * perspective
                    )
                }

                // Création du chemin de la face
                val path = Path().apply {
                    moveTo(projected[0].x, projected[0].y)
                    projected.drop(1).forEach { lineTo(it.x, it.y) }
                    close()
                }

                // Application de l'éclairage
                val shadedColor = face.color.copy(
                    red = face.color.red * brightness,
                    green = face.color.green * brightness,
                    blue = face.color.blue * brightness,
                    alpha = layer.alpha
                )

                // Dessin de la face
                drawPath(path, shadedColor)

                // Dessin des arêtes si activé
                if (layer.showEdges) {
                    drawPath(path, Color.Black.copy(alpha = 0.25f), style = Stroke(1.5f))
                }

                // Dessin des pips si activé
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
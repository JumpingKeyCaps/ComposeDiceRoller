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
import com.lebaillyapp.composediceroller.model.DiceAnimationConfig
import com.lebaillyapp.composediceroller.model.DiceLayerConfig
import com.lebaillyapp.composediceroller.model.DiceState
import com.lebaillyapp.composediceroller.model.FaceConfig
import com.lebaillyapp.composediceroller.model.LayerLockState
import com.lebaillyapp.composediceroller.model.RotationState
import com.lebaillyapp.composediceroller.model.Vec3
import com.lebaillyapp.composediceroller.ui.utils.DiceAnimationController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

/**
 * Composable de dé interactif multi-couches avec système d'animation par états.
 *
 * États supportés :
 * - IDLE : Rotation auto douce, drag possible (coupe l'auto-rotation)
 * - ROLLING : Mode 6 faces, rotation chaotique, drag bloqué
 * - LANDING : Mode uniforme (targetValue), ralentissement, drag bloqué → IDLE
 *
 * @param modifier Modificateur Compose
 * @param layers Liste des configurations de couches
 * @param animationConfig Configuration de l'état d'animation du dé
 * @param layerLocks Liste des états de verrouillage par couche
 * @param size Taille du canvas en dp
 * @param scaleFactor Facteur d'échelle de rendu
 * @param damping Facteur d'amortissement de l'inertie
 * @param dragFactor Sensibilité du drag
 * @param pipPadding Padding des pips
 * @param pipRadius Rayon des pips
 * @param pipColor Couleur des pips
 * @param isParentInteractive Si false, le premier cube se fige
 * @param onAnimationStateChange Callback appelé lors des transitions d'état
 * @param onValueChange Callback appelé quand la valeur du dé change
 */
@Composable
fun NestedInteractiveDice(
    modifier: Modifier = Modifier,
    layers: List<DiceLayerConfig>,
    animationConfig: DiceAnimationConfig = DiceAnimationConfig.idle(0),
    layerLocks: List<LayerLockState> = emptyList(),
    size: Float = 300f,
    scaleFactor: Float = 0.25f,
    damping: Float = 0.99f,
    dragFactor: Float = 0.004f,
    pipPadding: Float = 0.08f,
    pipRadius: Float = 0.18f,
    pipColor: Color = Color.White,
    isParentInteractive: Boolean = true,
    onAnimationStateChange: ((DiceAnimationConfig) -> Unit)? = null,
    onValueChange: ((Int) -> Unit)? = null
) {
    var targetRotationX by remember { mutableStateOf(0f) }
    var targetRotationY by remember { mutableStateOf(0f) }
    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }

    val parentFixedRotationX = remember { mutableStateOf(0f) }
    val parentFixedRotationY = remember { mutableStateOf(0f) }
    val wasParentInteractive = remember { mutableStateOf(true) }

    val rotationStates = remember { List(layers.size) { RotationState() } }

    // === Controller d'animation ===
    val animController = remember { DiceAnimationController() }
    var internalAnimState by remember { mutableStateOf(animationConfig.currentState) }
    var internalTargetValue by remember { mutableStateOf(animationConfig.targetValue) }
    var internalRotationsX by remember { mutableStateOf(animationConfig.rollingRotationsX) }
    var internalRotationsY by remember { mutableStateOf(animationConfig.rollingRotationsY) }
    var internalRollingDuration by remember { mutableStateOf(animationConfig.rollingDuration) }
    var internalLandingDuration by remember { mutableStateOf(animationConfig.landingDuration) }
    var currentDisplayValue by remember { mutableStateOf(animationConfig.targetValue) }
    val coroutineScope = rememberCoroutineScope()

    // Timestamp pour gérer les durées des phases
    var phaseStartTime by remember { mutableStateOf(0L) }

    // Flag pour savoir si l'user est en train de drag (coupe l'auto-rotation IDLE)
    var isUserDragging by remember { mutableStateOf(false) }

    fun applyInversion(value: Float, invert: Boolean): Float =
        if (invert) -value else value

    // === Animation du zoom central ===
    val cubeScale = remember { Animatable(1f) }

    // === Synchronisation avec l'état externe ===
    LaunchedEffect(animationConfig.currentState, animationConfig.targetValue,
        animationConfig.rollingRotationsX, animationConfig.rollingRotationsY,
        animationConfig.rollingDuration, animationConfig.landingDuration) {
        internalAnimState = animationConfig.currentState
        internalTargetValue = animationConfig.targetValue
        internalRotationsX = animationConfig.rollingRotationsX
        internalRotationsY = animationConfig.rollingRotationsY
        internalRollingDuration = animationConfig.rollingDuration
        internalLandingDuration = animationConfig.landingDuration
        phaseStartTime = System.currentTimeMillis()
    }

    // === Génération des velocités pour ROLLING basée sur le nombre de tours ===
    LaunchedEffect(internalAnimState, internalRotationsX, internalRotationsY) {
        if (internalAnimState == DiceState.ROLLING) {
            // Calcule les velocités pour atteindre exactement le nombre de tours voulu
            val (velX, velY) = animController.calculateVelocitiesForRotations(
                rotationsX = internalRotationsX,
                rotationsY = internalRotationsY,
                durationMs = internalRollingDuration
            )

            // Direction aléatoire
            val dirX = if (Random.nextBoolean()) 1f else -1f
            val dirY = if (Random.nextBoolean()) 1f else -1f

            velocityX = velX * dirX
            velocityY = velY * dirY

            // Les velocités restent constantes pendant tout le rolling
        }
    }

    // === Gestion des transitions automatiques d'état ===
    LaunchedEffect(internalAnimState, internalTargetValue) {
        when (internalAnimState) {
            DiceState.ROLLING -> {
                currentDisplayValue = 0
                onValueChange?.invoke(0)

                delay(internalRollingDuration)

                currentDisplayValue = internalTargetValue
                onValueChange?.invoke(internalTargetValue)

                // Transition vers LANDING
                internalAnimState = DiceState.LANDING
                onAnimationStateChange?.invoke(
                    animationConfig.copy(
                        currentState = DiceState.LANDING,
                        targetValue = internalTargetValue
                    )
                )
            }

            DiceState.LANDING -> {
                // La valeur est déjà changée, on ralentit juste
                delay(internalLandingDuration)

                // Transition vers IDLE
                internalAnimState = DiceState.IDLE
                onAnimationStateChange?.invoke(
                    animationConfig.copy(
                        currentState = DiceState.IDLE,
                        targetValue = internalTargetValue
                    )
                )
            }

            DiceState.IDLE -> {
                if (currentDisplayValue != internalTargetValue) {
                    currentDisplayValue = internalTargetValue
                    onValueChange?.invoke(internalTargetValue)
                }
            }
        }
    }

    // === Désactivation du drag selon l'état ===
    val interactionEnabled = !animController.shouldDisableInteraction(internalAnimState)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(interactionEnabled) {
                if (interactionEnabled) {
                    detectDragGestures(
                        onDragStart = {
                            isUserDragging = true  // Coupe l'auto-rotation IDLE
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
                            isUserDragging = false  // Reprend l'auto-rotation IDLE
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
                            isUserDragging = false  // Reprend l'auto-rotation IDLE
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
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val scale = min(this.size.width, this.size.height) * scaleFactor
            val light = Vec3(0.5f, 0.7f, -1f).normalize()

            // === Auto-rotation en IDLE (si pas de drag) ===
            if (internalAnimState == DiceState.IDLE && !isUserDragging) {
                val (newRotX, newRotY) = animController.applyIdleRotation(
                    targetRotationX,
                    targetRotationY
                )
                targetRotationX = newRotX
                targetRotationY = newRotY
            }

            // === Inertie avec damping adaptatif ===
            val effectiveDamping = animController.getDampingForState(internalAnimState, damping)
            targetRotationX += velocityY
            targetRotationY += velocityX
            velocityX *= effectiveDamping
            velocityY *= effectiveDamping

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
                val face: FaceConfig,
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




//todo to remove after test ...

@Composable
fun NestedInteractiveDiceOLD(
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
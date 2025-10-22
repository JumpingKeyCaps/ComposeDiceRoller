package com.lebaillyapp.composediceroller.ui.utils

import com.lebaillyapp.composediceroller.model.DiceState
import kotlin.math.PI
import kotlin.random.Random

/**
 * Contrôleur d'animation pour un dé.
 * Gère la logique d'animation et les calculs de rotation selon l'état.
 */
class DiceAnimationController {

    /**
     * Applique une rotation automatique douce en mode IDLE
     *
     * @param currentRotX Rotation X actuelle
     * @param currentRotY Rotation Y actuelle
     * @param deltaTime Temps écoulé depuis la dernière frame (pour consistance)
     * @return Pair(newRotX, newRotY)
     */
    fun applyIdleRotation(
        currentRotX: Float,
        currentRotY: Float,
        deltaTime: Float = 0.016f  // ~60fps
    ): Pair<Float, Float> {
        // Rotation douce constante
        val idleSpeedX = 0.3f * deltaTime
        val idleSpeedY = 0.5f * deltaTime

        return (currentRotX + idleSpeedX) to (currentRotY + idleSpeedY)
    }

    /**
     * Calcule les velocités pour atteindre un nombre de rotations spécifique
     * pendant une durée donnée
     *
     * @param rotationsX Nombre de tours complets sur l'axe X
     * @param rotationsY Nombre de tours complets sur l'axe Y
     * @param durationMs Durée en millisecondes
     * @return Pair(velocityX, velocityY) - velocités constantes pour atteindre le nombre de tours
     */
    fun calculateVelocitiesForRotations(
        rotationsX: Float,
        rotationsY: Float,
        durationMs: Long
    ): Pair<Float, Float> {
        // 1 tour complet = 2π radians
        val targetRadiansX = rotationsX * 2f * PI.toFloat()
        val targetRadiansY = rotationsY * 2f * PI.toFloat()

        // Durée en secondes (approximation à 60fps)
        val durationSeconds = durationMs / 1000f
        val framesApprox = durationSeconds * 60f

        // Velocity nécessaire par frame pour atteindre la rotation totale
        val velocityX = targetRadiansX / framesApprox
        val velocityY = targetRadiansY / framesApprox

        return velocityX to velocityY
    }

    /**
     * Calcule le facteur de damping selon l'état
     *
     * @param state État actuel
     * @param baseDamping Damping de base (défaut 0.99)
     * @return Damping ajusté
     */
    fun getDampingForState(state: DiceState, baseDamping: Float = 0.99f): Float {
        return when (state) {
            DiceState.IDLE -> baseDamping      // Conservation normale en idle
            DiceState.ROLLING -> 0.99f         // Très peu de perte pour garder la vitesse
            DiceState.LANDING -> 0.90f         // Fort freinage en landing
        }
    }

    /**
     * Détermine si l'interaction utilisateur doit être désactivée
     *
     * @param state État actuel
     * @return true si le drag doit être bloqué
     */
    fun shouldDisableInteraction(state: DiceState): Boolean {
        return state == DiceState.ROLLING || state == DiceState.LANDING
    }

    /**
     * Calcule la vitesse de rotation en mode LANDING
     * Commence rapide, ralentit progressivement
     *
     * @param landingProgress Progression dans la phase LANDING (0.0 à 1.0)
     * @return Facteur de vitesse (1.0 = vitesse normale, 0.0 = arrêt)
     */
    fun getLandingSpeedFactor(landingProgress: Float): Float {
        // Courbe de ralentissement exponentielle
        return (1f - landingProgress * landingProgress).coerceIn(0.05f, 1f)
    }
}
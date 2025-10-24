package com.lebaillyapp.composediceroller.ui.utils

import com.lebaillyapp.composediceroller.model.state.DiceState
import kotlin.math.PI

/**
 * Contrôleur d'animation pour un dé.
 * Version simplifiée sans état LANDING.
 * Gère la logique d'animation fluide et les rotations continues.
 */
class DiceAnimationController {

    /**
     * Applique une rotation douce en mode IDLE (léger flottement constant).
     */
    fun applyIdleRotation(
        currentRotX: Float,
        currentRotY: Float,
        deltaTime: Float = 0.032f // 0.016f for 60fps
    ): Pair<Float, Float> {
        val idleSpeedX = 0.3f * deltaTime
        val idleSpeedY = 0.5f * deltaTime
        return (currentRotX + idleSpeedX) to (currentRotY + idleSpeedY)
    }

    /**
     * Calcule les vitesses nécessaires pour effectuer un nombre de rotations
     * complet sur chaque axe pendant la durée donnée.
     */
    fun calculateVelocitiesForRotations(
        rotationsX: Float,
        rotationsY: Float,
        durationMs: Long
    ): Pair<Float, Float> {
        val targetRadiansX = rotationsX * 2f * PI.toFloat()
        val targetRadiansY = rotationsY * 2f * PI.toFloat()

        val frames = (durationMs / 1000f) * 30f //30fps par défaut , to be replaced by 60
        val velocityX = targetRadiansX / frames
        val velocityY = targetRadiansY / frames

        return velocityX to velocityY
    }

    /**
     * Calcule un damping progressif selon la progression du roll.
     * Exemple : plus on approche de la fin du roll, plus on ralentit.
     *
     * @param progress Avancement de 0f à 1f dans le roll
     */
    fun getRollingDamping(progress: Float): Float {
        // entre 0.99 (début) et 0.90 (fin)
        return 0.99f - (0.09f * progress.coerceIn(0f, 1f))
    }

    /**
     * Détermine si l'interaction utilisateur doit être bloquée.
     * En rolling, toujours bloqué. En idle, libre.
     */
    fun shouldDisableInteraction(state: DiceState): Boolean {
        return state == DiceState.ROLLING
    }

    /**
     * Calcule si on doit appliquer la valeur cible du dé.
     * Si le roll a dépassé 50% du nombre total de tours, on fixe la face.
     *
     * @param progress Avancement du roll (0f..1f)
     * @return true si la valeur finale doit être appliquée
     */
    fun shouldApplyTargetValue(progress: Float): Boolean {
        return progress >= 0.5f
    }
}
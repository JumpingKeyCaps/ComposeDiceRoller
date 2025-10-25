package com.lebaillyapp.composediceroller.model.dice.config

import com.lebaillyapp.composediceroller.model.dice.state.DiceState

/**
 * Configuration de l'animation d'un dé.
 *
 * @param currentState État actuel de l'animation
 * @param targetValue Valeur finale visée (0 = mode libre 6 faces différentes, 1-6 = mode uniforme)
 * @param rollingDuration Durée totale de la phase ROLLING en millisecondes (défaut 1500ms)
 * @param rollingRotationsX Nombre de tours complets sur l'axe X pendant le rolling (défaut 3.0)
 * @param rollingRotationsY Nombre de tours complets sur l'axe Y pendant le rolling (défaut 4.0)
 */
data class DiceAnimationConfig(
    val currentState: DiceState = DiceState.IDLE,
    val targetValue: Int = 0,
    val rollingDuration: Long = 1500L,
    val rollingRotationsX: Float = 3.0f,
    val rollingRotationsY: Float = 4.0f,
    val diceTicker: Int = 0
) {
    init {
        require(targetValue in 0..6) {
            "targetValue must be between 0 (classic dice) and 6, got $targetValue"
        }
        require(rollingRotationsX >= 0) {
            "rollingRotationsX must be >= 0, got $rollingRotationsX"
        }
        require(rollingRotationsY >= 0) {
            "rollingRotationsY must be >= 0, got $rollingRotationsY"
        }
    }

    companion object {

        /**
         * Configuration par défaut : dé en mode IDLE avec auto-rotation.
         */
        fun idle(targetValue: Int = 0) = DiceAnimationConfig(
            currentState = DiceState.IDLE,
            targetValue = targetValue
        )

        /**
         * Lance un dé vers une valeur cible.
         * ROLLING → IDLE avec blend mid-rotation.
         *
         * @param targetValue Valeur finale du dé (1–6)
         * @param rotationsX Nombre de tours sur l'axe X (défaut 3.0)
         * @param rotationsY Nombre de tours sur l'axe Y (défaut 4.0)
         * @param rollingDuration Durée totale du rolling en ms (défaut 1500)
         */
        fun rollTo(
            targetValue: Int,
            rotationsX: Float = 3.0f,
            rotationsY: Float = 4.0f,
            rollingDuration: Long = 1500L,
            diceTicker: Int = 0
        ) = DiceAnimationConfig(
            currentState = DiceState.ROLLING,
            targetValue = targetValue,
            rollingDuration = rollingDuration,
            rollingRotationsX = rotationsX,
            rollingRotationsY = rotationsY,
            diceTicker = diceTicker
        )
    }
}
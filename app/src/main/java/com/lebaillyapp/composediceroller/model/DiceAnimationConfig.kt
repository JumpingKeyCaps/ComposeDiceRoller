package com.lebaillyapp.composediceroller.model

/**
 * Configuration de l'animation d'un dé.
 *
 * @param currentState État actuel de l'animation
 * @param targetValue Valeur finale visée (0 = mode libre/pas de cible, 1-6 = valeur spécifique)
 * @param autoTransition Si true, les transitions ROLLING -> LANDING -> LOCKED se font automatiquement
 * @param rollingDuration Durée de la phase ROLLING en millisecondes (défaut 1500ms)
 * @param landingDuration Durée de la phase LANDING en millisecondes (défaut 800ms)
 */
data class DiceAnimationConfig(
    val currentState: DiceState = DiceState.IDLE,
    val targetValue: Int = 0,
    val autoTransition: Boolean = true,
    val rollingDuration: Long = 1500L,
    val landingDuration: Long = 800L
) {
    init {
        require(targetValue in 0..6) {
            "targetValue must be between 0 (free) and 6, got $targetValue"
        }
    }

    companion object {
        /**
         * Configuration par défaut : dé en mode IDLE, libre
         */
        fun idle() = DiceAnimationConfig(
            currentState = DiceState.IDLE,
            targetValue = 0
        )

        /**
         * Lance un dé vers une valeur cible avec transitions automatiques
         */
        fun rollTo(value: Int) = DiceAnimationConfig(
            currentState = DiceState.ROLLING,
            targetValue = value,
            autoTransition = true
        )


    }
}
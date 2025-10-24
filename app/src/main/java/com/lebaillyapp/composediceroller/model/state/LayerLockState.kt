package com.lebaillyapp.composediceroller.model.state

/**
 * État de verrouillage d'une couche de dé.
 *
 * Permet de figer un cube à une rotation spécifique, indépendamment de la rotation globale.
 *
 * @param isLocked Si true, le cube ignore la rotation globale et reste à la position définie
 * @param targetRotX Rotation cible sur l'axe X (en radians) quand le cube est verrouillé
 * @param targetRotY Rotation cible sur l'axe Y (en radians) quand le cube est verrouillé
 */
data class LayerLockState(
    val isLocked: Boolean = false,
    val targetRotX: Float = 0f,
    val targetRotY: Float = 0f
) {
    companion object {
        /**
         * Crée un état déverrouillé (comportement par défaut)
         */
        fun unlocked() = LayerLockState(isLocked = false)

        /**
         * Crée un état verrouillé à une rotation spécifique
         */
        fun locked(rotX: Float, rotY: Float) = LayerLockState(
            isLocked = true,
            targetRotX = rotX,
            targetRotY = rotY
        )
    }
}
package com.lebaillyapp.composediceroller.model.dice.config

/**
 * Configuration d'une couche de dé dans un système de dés imbriqués.
 *
 * @param cubeConfig Configuration des faces, vertices et pips du cube
 * @param ratio Taille relative du cube (1.0 = taille normale, 0.5 = moitié, etc.)
 * @param lagFactor Facteur de suivi de la rotation globale (1.0 = instantané, 0.0 = figé, entre les deux = traînée)
 * @param isEnabled Si false, le cube n'est pas dessiné
 * @param isInteractive Si false, le cube ne suit pas la rotation globale (reste figé à sa dernière position)
 * @param invertRotationX Inverse la rotation sur l'axe X
 * @param invertRotationY Inverse la rotation sur l'axe Y
 * @param showEdges Affiche les arêtes du cube en noir
 * @param showPips Affiche les pips (points) sur les faces du dé
 * @param alpha Transparence du cube (0.0 = transparent, 1.0 = opaque)
 */
data class DiceLayerConfig(
    val cubeConfig: CubeConfig,
    val ratio: Float = 1.0f,
    val lagFactor: Float = 1.0f,
    val isEnabled: Boolean = true,
    val isInteractive: Boolean = true,
    val invertRotationX: Boolean = false,
    val invertRotationY: Boolean = false,
    val showEdges: Boolean = false,
    val showPips: Boolean = true,
    val alpha: Float = 0.95f
) {
    companion object {
        /**
         * Crée une configuration de couche par défaut (dé standard)
         */
        fun createDefault(): DiceLayerConfig {
            return DiceLayerConfig(
                cubeConfig = CubeConfig.createDefaultDice(false),
                ratio = 1.0f,
                lagFactor = 1.0f,
                showPips = true,
                alpha = 0.95f
            )
        }

        /**
         * Crée une configuration pour un cube parent transparent
         */
        fun createGhostParent(): DiceLayerConfig {
            return DiceLayerConfig(
                cubeConfig = CubeConfig.createDefaultDice(true),
                ratio = 1.0f,
                lagFactor = 1.0f,
                showPips = false,
                showEdges = false,
                alpha = 0.05f
            )
        }

        /**
         * Crée une configuration pour un cube intérieur avec lag
         */
        fun createInnerWithLag(
            ratio: Float = 0.7f,
            lagFactor: Float = 0.5f,
            cubeConfig: CubeConfig = CubeConfig.createDefaultDice(false)
        ): DiceLayerConfig {
            return DiceLayerConfig(
                cubeConfig = cubeConfig,
                ratio = ratio,
                lagFactor = lagFactor,
                showPips = true,
                alpha = 0.95f
            )
        }
    }
}
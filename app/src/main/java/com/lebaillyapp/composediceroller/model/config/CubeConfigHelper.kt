package com.lebaillyapp.composediceroller.model.config

import androidx.compose.ui.graphics.Color
import com.lebaillyapp.composediceroller.model.Pip
import com.lebaillyapp.composediceroller.model.Vec3

/**
 * Crée un cube où toutes les faces affichent la même valeur de dé (1-6).
 * Toutes les faces auront la même couleur et le même nombre de pips.
 *
 * @param value Valeur du dé (1 à 6)
 * @return CubeConfig avec 6 faces identiques
 */
fun CubeConfig.Companion.createUniformDice(value: Int): CubeConfig {
    require(value in 1..6) { "Dice value must be between 1 and 6, got $value" }

    // Couleur selon la valeur
    val color = getColorForValue(value)

    // Récupère les pips pour cette valeur
    val pips = getPipsForValue(value)

    // Vertices standards
    val vertices = getStandardVertices()

    // Toutes les 6 faces avec la même config !
    val faces = createUniformFaces(color, pips)

    return CubeConfig(vertices, faces)
}

/**
 * Crée un cube "ghost" uniforme sans pips, avec la couleur correspondant à la valeur.
 * Utile pour les layers intermédiaires qui doivent matcher la couleur du dé principal.
 *
 * @param value Valeur du dé (1 à 6) - détermine la couleur
 * @return CubeConfig avec 6 faces identiques de couleur uniforme, sans pips
 */
fun CubeConfig.Companion.createUniformGhost(value: Int): CubeConfig {
    require(value in 1..6) { "Dice value must be between 1 and 6, got $value" }

    // Même couleur que le dé principal
    val color = getColorForValue(value)

    // Vertices standards
    val vertices = getStandardVertices()

    // Toutes les faces avec la couleur, SANS pips
    val faces = createUniformFaces(color, emptyList())

    return CubeConfig(vertices, faces)
}

/**
 * Retourne la couleur associée à une valeur de dé
 */
private fun getColorForValue(value: Int): Color {
    return when(value) {
        1 -> Color(0xFFE74C3C)  // Rouge
        2 -> Color(0xFF3498DB)  // Bleu
        3 -> Color(0xFF2ECC71)  // Vert
        4 -> Color(0xFFF39C12)  // Orange
        5 -> Color(0xFF9B59B6)  // Violet
        6 -> Color(0xFF1ABC9C)  // Turquoise
        else -> Color.White
    }
}

/**
 * Retourne les vertices standards d'un cube
 */
private fun getStandardVertices(): List<Vec3> {
    val s = 1f
    return listOf(
        Vec3(-s, -s, -s), Vec3(s, -s, -s),
        Vec3(s, s, -s), Vec3(-s, s, -s),
        Vec3(-s, -s, s), Vec3(s, -s, s),
        Vec3(s, s, s), Vec3(-s, s, s)
    )
}

/**
 * Crée 6 faces uniformes avec la couleur et les pips donnés
 */
private fun createUniformFaces(color: Color, pips: List<Pip>): List<FaceConfig> {
    return listOf(
        FaceConfig(listOf(0, 3, 2, 1), color, pips),  // Face avant
        FaceConfig(listOf(4, 5, 6, 7), color, pips),  // Face arrière
        FaceConfig(listOf(0, 1, 5, 4), color, pips),  // Face bas
        FaceConfig(listOf(2, 3, 7, 6), color, pips),  // Face haut
        FaceConfig(listOf(0, 4, 7, 3), color, pips),  // Face gauche
        FaceConfig(listOf(1, 2, 6, 5), color, pips)   // Face droite
    )
}

/**
 * Retourne les positions des pips pour une valeur donnée
 */
private fun getPipsForValue(value: Int): List<Pip> {
    fun pip(x: Float, y: Float) = Pip(x, y)

    val mid = 0.5f
    val low = 0.25f
    val high = 0.75f

    return when(value) {
        1 -> listOf(pip(mid, mid))
        2 -> listOf(pip(low, low), pip(high, high))
        3 -> listOf(pip(low, low), pip(mid, mid), pip(high, high))
        4 -> listOf(pip(low, low), pip(low, high), pip(high, low), pip(high, high))
        5 -> listOf(pip(low, low), pip(low, high), pip(mid, mid), pip(high, low), pip(high, high))
        6 -> listOf(
            pip(low, low), pip(low, mid), pip(low, high),
            pip(high, low), pip(high, mid), pip(high, high)
        )
        else -> emptyList()
    }
}
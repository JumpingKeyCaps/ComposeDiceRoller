package com.lebaillyapp.composediceroller.model.score

/**
 * Représente une combinaison gagnante détectée sur un lancer.
 *
 * @param type Type de combinaison (suite, brelan, etc.).
 * @param diceUsed Liste des valeurs des dés impliqués.
 * @param score Points attribués à cette combinaison.
 */
data class CombinationResult(
    val type: CombinationType,
    val diceUsed: List<Int>,
    val score: Int
)
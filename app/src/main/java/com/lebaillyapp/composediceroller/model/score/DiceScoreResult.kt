package com.lebaillyapp.composediceroller.model.score

/**
 * Représente le résultat d’un lancer de dés.
 *
 * @param totalScore Score cumulé pour le lancer.
 * @param combinations Liste détaillée des combinaisons trouvées.
 * @param isFanny Indique si aucun dé ne rapporte de points.
 */
data class DiceScoreResult(
    val totalScore: Int,
    val combinations: List<CombinationResult>,
    val isFanny: Boolean
)
package com.lebaillyapp.composediceroller.model.score

/**
 *  Moteur de scoring complet et extensible.
 *
 * Cette implémentation gère toutes les combinaisons classiques :
 * - Suite (1–6)
 * - Trois paires
 * - Deux triplés
 * - Multiples de 3 à 6 dés identiques
 * - Dés unitaires (1 et 5)
 *
 * Les règles sont strictes, mais chaque valeur de combinaison
 * reste personnalisable via [DiceRuleset].
 *
 */
class DiceScorer(private val rules: DiceRuleset = DiceRuleset()) {

    fun evaluate(dice: List<Int>): DiceScoreResult {
        require(dice.isNotEmpty()) { "Dice list cannot be empty" }

        val counts = dice.groupingBy { it }.eachCount().toMutableMap()
        val combinations = mutableListOf<CombinationResult>()

        // --- Étape 0 : Full House (3+2) ---
        val triple = counts.filterValues { it == 3 }.keys.firstOrNull()
        val pair = counts.filterValues { it == 2 }.keys.firstOrNull()
        if (triple != null && pair != null) {
            combinations += CombinationResult(
                type = CombinationType.FULL_HOUSE,
                diceUsed = List(3) { triple } + List(2) { pair },
                score = rules.scoreFullHouse
            )
            counts.remove(triple)
            counts.remove(pair)
        }

        // --- Étape 1 : Suite complète 1–6 ---
        if (counts.keys.containsAll((1..6).toList())) {
            combinations += CombinationResult(
                type = CombinationType.STRAIGHT,
                diceUsed = listOf(1, 2, 3, 4, 5, 6),
                score = rules.scoreStraight
            )
            counts.clear()
        } else {
            // --- Étape 1b : Petite suite (5 dés consécutifs) ---
            val smallStraightSequences = listOf(
                setOf(1,2,3,4,5),
                setOf(2,3,4,5,6)
            )
            val smallStraight = smallStraightSequences.firstOrNull { seq -> counts.keys.containsAll(seq) }
            if (smallStraight != null) {
                combinations += CombinationResult(
                    type = CombinationType.SMALL_STRAIGHT,
                    diceUsed = smallStraight.toList(),
                    score = rules.scoreSmallStraight
                )
                smallStraight.forEach { counts.remove(it) }
            } else {
                // --- Étape 2 : Trois paires ---
                val pairs = counts.filterValues { it == 2 }.keys
                if (pairs.size == 3) {
                    combinations += CombinationResult(
                        type = CombinationType.THREE_PAIRS,
                        diceUsed = pairs.flatMap { listOf(it, it) },
                        score = rules.scoreThreePairs
                    )
                    pairs.forEach { counts.remove(it) }
                } else {
                    // --- Étape 3 : Deux triplés ---
                    val triples = counts.filterValues { it == 3 }.keys
                    if (triples.size == 2) {
                        combinations += CombinationResult(
                            type = CombinationType.TWO_TRIPLETS,
                            diceUsed = triples.flatMap { listOf(it, it, it) },
                            score = 2500
                        )
                        triples.forEach { counts.remove(it) }
                    }
                }
            }
        }

        // --- Étape 4 : Multiples de 3 à 6 dés identiques ---
        val multiples = counts.filterValues { it >= 3 }
        multiples.forEach { (value, qty) ->
            val base = rules.baseTripletScores[value] ?: 0
            val multiplier = when (qty) {
                3 -> 1
                4 -> 2
                5 -> 3
                6 -> 4
                else -> 1
            }

            combinations += CombinationResult(
                type = when (qty) {
                    3 -> CombinationType.THREE_OF_A_KIND
                    4 -> CombinationType.FOUR_OF_A_KIND
                    5 -> CombinationType.FIVE_OF_A_KIND
                    else -> CombinationType.SIX_OF_A_KIND
                },
                diceUsed = List(qty) { value },
                score = base * multiplier
            )
        }
        multiples.keys.forEach { counts.remove(it) }

        // --- Étape 5 : Dés unitaires (1 et 5 restants) ---
        counts.forEach { (value, qty) ->
            val score = when (value) {
                1 -> rules.scoreSingleOne * qty
                5 -> rules.scoreSingleFive * qty
                else -> 0
            }
            if (score > 0) {
                combinations += CombinationResult(
                    type = if (value == 1) CombinationType.SINGLE_ONE else CombinationType.SINGLE_FIVE,
                    diceUsed = List(qty) { value },
                    score = score
                )
            }
        }

        // --- Étape 6 : Résumé final ---
        val total = combinations.sumOf { it.score }
        val isFanny = total == 0

        return DiceScoreResult(
            totalScore = total,
            combinations = combinations,
            isFanny = isFanny
        )
    }
}
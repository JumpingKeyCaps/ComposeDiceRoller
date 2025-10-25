package com.lebaillyapp.composediceroller.model.score

/**
 * Règles de calcul des points pour le jeu .
 * Permet d’ajuster les valeurs sans modifier le moteur.
 */
data class DiceRuleset(
    val scoreSingleOne: Int = 100,
    val scoreSingleFive: Int = 50,
    val scoreThreePairs: Int = 750,
    val scoreStraight: Int = 1500,
    val baseTripletScores: Map<Int, Int> = mapOf(
        1 to 1000,
        2 to 200,
        3 to 300,
        4 to 400,
        5 to 500,
        6 to 600
    ),
    val scoreFullHouse: Int = 1200,
    val scoreSmallStraight: Int = 1000
)
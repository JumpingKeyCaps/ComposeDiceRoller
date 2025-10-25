package com.lebaillyapp.composediceroller.model.score

/**
 * Type de combinaison possible dans le jeu du 10 000.
 */
enum class CombinationType {
    STRAIGHT,        // Suite 1–6
    THREE_PAIRS,     // Trois paires distinctes
    TWO_TRIPLETS,    // Deux triplés distincts
    THREE_OF_A_KIND, // Trois dés identiques
    FOUR_OF_A_KIND,  // Quatre dés identiques
    FIVE_OF_A_KIND,  // Cinq dés identiques
    SIX_OF_A_KIND,   // Six dés identiques
    SINGLE_ONE,      // Dé unitaire de valeur 1
    SINGLE_FIVE,      // Dé unitaire de valeur 5
    FULL_HOUSE,     // 3+2 identiques
    SMALL_STRAIGHT  // Suite de 5 dés
}
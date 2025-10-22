package com.lebaillyapp.composediceroller.model

/**
 * États possibles d'animation d'un dé.
 */
enum class DiceState {
    /**
     * État de repos - rotation douce et calme
     * Le dé peut être manipulé librement par l'utilisateur
     */
    IDLE,

    /**
     * État de lancer - rotation chaotique et rapide
     * Le dé tourne rapidement dans toutes les directions
     * Interaction utilisateur désactivée
     */
    ROLLING,

    /**
     * État d'atterrissage - ralentissement progressif
     * Le dé ralentit et s'oriente vers la valeur cible
     * Transition automatique vers LOCKED
     */
    LANDING
}
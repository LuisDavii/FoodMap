package com.example.foodmap

/**
 * Representa um único resultado de scan salvo no histórico.
 * Usado pelo HistoryManager e pela HistoryActivity.
 */
data class ScanResult(
    val foodName: String,
    val calories: Int,
    val protein: Double,
    val fiber: Double,
    val timestamp: String // Data e hora que o scan foi salvo
)
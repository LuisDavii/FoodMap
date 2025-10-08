package com.example.foodmap // Verifique se o pacote está correto

// Data class para representar um único resultado de scan salvo no histórico.
data class ScanResult(
    val foodName: String,
    val calories: Int,
    val protein: Double,
    val fiber: Double,
    val timestamp: String // Usaremos String para simplificar a data e hora
)
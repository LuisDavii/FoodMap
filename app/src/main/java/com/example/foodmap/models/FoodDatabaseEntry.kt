package com.example.foodmap

/**
 * Representa um item da nossa base de dados 'food_database.json'.
 * Inclui a 'key' que corresponde ao label do TFLite.
 */
data class FoodDatabaseEntry(
    val key: String,
    val name: String,
    val caloriesKcal: Int,
    val proteinGrams: Double,
    val fiberGrams: Double
)
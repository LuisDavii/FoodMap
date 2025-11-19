package com.example.foodmap

data class Meal(
    val id: Long = System.currentTimeMillis(), // ID único baseado no tempo
    val dayOfWeek: String,    // Ex: "Segunda-feira"
    val type: String,         // Ex: "Almoço"
    val description: String,  // Ex: "Frango com batata"
    val calories: Int,        // Ex: 500
    var isDone: Boolean = false // Se já comeu ou não
)
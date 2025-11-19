package com.example.foodmap

data class Meal(
    val id: Long = System.currentTimeMillis(),
    val dayOfWeek: String,
    val type: String,
    val description: String,
    val calories: Int,
    var isDone: Boolean = false
)
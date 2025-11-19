package com.example.foodmap

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object MealManager {
    private const val PREFS_NAME = "FoodMapMealPrefs"
    private const val KEY_MEALS = "saved_meals"
    private val gson = Gson()

    // Salva uma nova refeição
    fun saveMeal(context: Context, meal: Meal) {
        val currentList = getMeals(context).toMutableList()
        currentList.add(meal)
        saveList(context, currentList)
    }

    // Atualiza o status (checkbox) de uma refeição
    fun updateMealStatus(context: Context, mealId: Long, isDone: Boolean) {
        val currentList = getMeals(context).toMutableList()
        val index = currentList.indexOfFirst { it.id == mealId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isDone = isDone)
            saveList(context, currentList)
        }
    }

    // Recupera todas as refeições
    fun getMeals(context: Context): List<Meal> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_MEALS, null) ?: return emptyList()

        val type = object : TypeToken<List<Meal>>() {}.type
        return gson.fromJson(json, type)
    }

    // Função auxiliar interna para salvar
    private fun saveList(context: Context, list: List<Meal>) {
        val json = gson.toJson(list)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MEALS, json).apply()
    }

    // Calcula estatísticas para a tela de Estatísticas
    fun getStats(context: Context): Triple<Int, Int, Int> {
        val list = getMeals(context)
        val totalMeals = list.size
        val completedMeals = list.count { it.isDone }
        val caloriesEaten = list.filter { it.isDone }.sumOf { it.calories }

        return Triple(totalMeals, completedMeals, caloriesEaten)
    }
}
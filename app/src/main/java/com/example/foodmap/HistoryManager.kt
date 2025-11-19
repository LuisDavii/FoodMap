package com.example.foodmap

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Objeto Singleton para gerir o armazenamento e recuperação
 * do histórico de scans. AGORA INDIVIDUAL POR UTILIZADOR.
 */
object HistoryManager {

    private const val PREFS_NAME = "FoodMapHistoryPrefs"
    // Mudamos de uma chave fixa para um prefixo
    private const val KEY_PREFIX = "scan_history_"
    private val gson = Gson()

    /**
     * Função auxiliar: Gera a chave única para o utilizador atual.
     * Ex: se o ID for 5, a chave será "scan_history_5"
     */
    private fun getUserKey(context: Context): String {
        // Acede às preferências onde o Login.kt salvou os dados
        val userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = userPrefs.getInt("user_id", -1)

        // Se não houver user (-1), usa "guest" como fallback
        val idSuffix = if (userId != -1) userId.toString() else "guest"

        return KEY_PREFIX + idSuffix
    }

    /**
     * Salva um novo resultado de scan no histórico DO UTILIZADOR ATUAL.
     */
    fun saveScan(context: Context, scanResult: ScanResult) {
        // 1. Obtém a lista atual (usando a chave do utilizador)
        val currentHistory = getHistory(context).toMutableList()

        // 2. Adiciona o novo item no topo
        currentHistory.add(0, scanResult)

        // 3. Converte para JSON
        val jsonHistory = gson.toJson(currentHistory)

        // 4. Salva no SharedPreferences com a chave ÚNICA do utilizador
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = getUserKey(context)

        prefs.edit().putString(key, jsonHistory).apply()
    }

    /**
     * Recupera a lista de scans DO UTILIZADOR ATUAL.
     */
    fun getHistory(context: Context): List<ScanResult> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 1. Define qual chave buscar baseada no utilizador logado
        val key = getUserKey(context)

        // 2. Lê o JSON dessa chave específica
        val jsonHistory = prefs.getString(key, null)

        if (jsonHistory.isNullOrEmpty()) {
            return emptyList()
        }

        try {
            val type = object : TypeToken<List<ScanResult>>() {}.type
            return gson.fromJson(jsonHistory, type)
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}
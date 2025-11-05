package com.example.foodmap

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Objeto Singleton para gerir o armazenamento e recuperação
 * do histórico de scans usando SharedPreferences e JSON (Gson).
 *
 * ESTE FICHEIRO SÓ DEVE USAR 'ScanResult'.
 */
object HistoryManager {

    private const val PREFS_NAME = "FoodMapHistoryPrefs"
    private const val KEY_HISTORY = "scan_history"
    private val gson = Gson()

    /**
     * Salva um novo resultado de scan no histórico.
     * Ele lê a lista atual, adiciona o novo item e salva a lista atualizada.
     */
    fun saveScan(context: Context, scanResult: ScanResult) {
        // 1. Obtém a lista atual
        val currentHistory = getHistory(context).toMutableList()

        // 2. Adiciona o novo item (no início, para o mais recente aparecer primeiro)
        currentHistory.add(0, scanResult)

        // 3. Converte a lista para JSON
        val jsonHistory = gson.toJson(currentHistory)

        // 4. Salva no SharedPreferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_HISTORY, jsonHistory).apply()
    }

    /**
     * Recupera a lista completa de scans salvos.
     */
    fun getHistory(context: Context): List<ScanResult> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 1. Lê o string JSON do SharedPreferences
        val jsonHistory = prefs.getString(KEY_HISTORY, null)

        if (jsonHistory.isNullOrEmpty()) {
            // Se não houver nada salvo, retorna uma lista vazia
            return emptyList()
        }

        // 2. Converte o JSON de volta para uma Lista de ScanResult
        try {
            // Define o "tipo" que o Gson deve usar para desserializar
            val type = object : TypeToken<List<ScanResult>>() {}.type
            return gson.fromJson(jsonHistory, type)
        } catch (e: Exception) {
            // Em caso de erro de leitura, retorna lista vazia
            e.printStackTrace()
            return emptyList()
        }
    }
}
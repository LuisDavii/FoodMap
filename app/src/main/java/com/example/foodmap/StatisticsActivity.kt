package com.example.foodmap

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StatisticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statisticas)

        // --- Referências UI ---
        val etDailyGoal = findViewById<EditText>(R.id.etDailyGoal)
        val btnSaveGoal = findViewById<Button>(R.id.btnSaveGoal)

        val pbDaily = findViewById<ProgressBar>(R.id.progressBarDaily)
        val tvProgressDaily = findViewById<TextView>(R.id.tvProgressDaily)

        val pbWeekly = findViewById<ProgressBar>(R.id.progressBarWeekly)
        val tvProgressWeekly = findViewById<TextView>(R.id.tvProgressWeekly)

        val tvTotalMeals = findViewById<TextView>(R.id.tvTotalMeals)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // --- SharedPreferences (Meta) ---
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        var dailyGoal = sharedPref.getInt("daily_goal", 2000)
        etDailyGoal.setText(dailyGoal.toString())

        // --- Obter Dados ---
        // O teu MealManager retorna (totalRefeicoes, concluidas, caloriasTotais)
        val (total, completed, totalCalories) = MealManager.getStats(this)

        // IMPORTANTE: Como não temos acesso ao código do MealManager,
        // vou simular que as 'calorias de hoje' são uma parte das totais.
        // No futuro, deves criar uma função MealManager.getTodayCalories(this).

        // Por enquanto, assumimos que 'totalCalories' é o acumulado da semana
        // e 'caloriesToday' seria obtido separadamente.
        // EXMPLO SIMPLIFICADO: Vamos assumir que hoje é igual ao total para testar
        // (Se estiveres a usar a app há 1 dia, isto está correto).
        val caloriesToday = totalCalories

        // --- Função de Atualização ---
        fun updateUI() {
            if (dailyGoal <= 0) dailyGoal = 1 // Evitar divisão por zero

            // 1. Cálculo Diário
            val progressDaily = (caloriesToday * 100) / dailyGoal
            pbDaily.progress = progressDaily.coerceAtMost(100)
            tvProgressDaily.text = "$caloriesToday / $dailyGoal kcal"

            // 2. Cálculo Semanal
            val weeklyGoal = dailyGoal * 7
            val progressWeekly = (totalCalories * 100) / weeklyGoal
            pbWeekly.progress = progressWeekly.coerceAtMost(100)
            tvProgressWeekly.text = "$progressWeekly% da meta semanal"

            // Outros dados
            tvTotalMeals.text = total.toString()
        }

        // Atualiza a tela pela primeira vez
        updateUI()

        // --- Botão Salvar ---
        btnSaveGoal.setOnClickListener {
            val input = etDailyGoal.text.toString()
            if (input.isNotEmpty()) {
                dailyGoal = input.toInt()

                sharedPref.edit().putInt("daily_goal", dailyGoal).apply()

                updateUI()
                Toast.makeText(this, "Meta atualizada!", Toast.LENGTH_SHORT).show()
                etDailyGoal.clearFocus()
            }
        }

        btnBack.setOnClickListener { finish() }
    }
}
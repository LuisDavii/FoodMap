package com.example.foodmap

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodmap.models.RefeicaoResponse
import com.example.foodmap.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class StatisticsActivity : AppCompatActivity() {

    private lateinit var pbDaily: ProgressBar
    private lateinit var tvProgressDaily: TextView
    private lateinit var pbWeekly: ProgressBar
    private lateinit var tvProgressWeekly: TextView
    private lateinit var tvTotalMeals: TextView
    private var dailyGoal = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statisticas)

        val etDailyGoal = findViewById<EditText>(R.id.etDailyGoal)
        val btnSaveGoal = findViewById<Button>(R.id.btnSaveGoal)
        val btnBack = findViewById<Button>(R.id.btnBack)

        pbDaily = findViewById(R.id.progressBarDaily)
        tvProgressDaily = findViewById(R.id.tvProgressDaily)
        pbWeekly = findViewById(R.id.progressBarWeekly)
        tvProgressWeekly = findViewById(R.id.tvProgressWeekly)
        tvTotalMeals = findViewById(R.id.tvTotalMeals)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        dailyGoal = sharedPref.getInt("daily_goal", 2000)
        etDailyGoal.setText(dailyGoal.toString())

        carregarEstatisticas()

        btnSaveGoal.setOnClickListener {
            val input = etDailyGoal.text.toString()
            if (input.isNotEmpty()) {
                dailyGoal = input.toInt()
                sharedPref.edit().putInt("daily_goal", dailyGoal).apply()
                carregarEstatisticas()
                Toast.makeText(this, "Meta atualizada!", Toast.LENGTH_SHORT).show()
                etDailyGoal.clearFocus()
            }
        }

        btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        carregarEstatisticas()
    }

    private fun carregarEstatisticas() {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) return

        RetrofitClient.instance.getRefeicoes(userId).enqueue(object : Callback<List<RefeicaoResponse>> {
            override fun onResponse(call: Call<List<RefeicaoResponse>>, response: Response<List<RefeicaoResponse>>) {
                if (response.isSuccessful) {
                    val refeicoes = response.body() ?: emptyList()
                    calcularEAtualizarUI(refeicoes)
                }
            }

            override fun onFailure(call: Call<List<RefeicaoResponse>>, t: Throwable) {
                Toast.makeText(this@StatisticsActivity, "Erro ao calcular estatísticas", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calcularEAtualizarUI(todasRefeicoes: List<RefeicaoResponse>) {

        val refeicoesConcluidas = todasRefeicoes.filter { it.concluido }

        val caloriasIngeridasSemana = refeicoesConcluidas.sumOf { it.calorias }

        val metaSemanal = dailyGoal * 7

        val diasSemana = arrayOf("Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")
        val calendar = Calendar.getInstance()
        val hojeIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val diaHojeString = diasSemana[hojeIndex] 

        val caloriasIngeridasHoje = refeicoesConcluidas
            .filter { it.dia_semana.contains(diaHojeString, ignoreCase = true) }
            .sumOf { it.calorias }

        val progressoDiario = if (dailyGoal > 0) (caloriasIngeridasHoje * 100) / dailyGoal else 0
        pbDaily.progress = progressoDiario.coerceAtMost(100)

        tvProgressDaily.text = "$caloriasIngeridasHoje / $dailyGoal kcal (Ingeridas)"

        val progressoSemanal = if (metaSemanal > 0) (caloriasIngeridasSemana * 100) / metaSemanal else 0
        pbWeekly.progress = progressoSemanal.coerceAtMost(100)

        tvProgressWeekly.text = "$progressoSemanal% da meta semanal ($caloriasIngeridasSemana kcal ingeridas)"

        val totalPlanejado = todasRefeicoes.size
        val totalConcluido = refeicoesConcluidas.size
        tvTotalMeals.text = "$totalConcluido feitas / $totalPlanejado planejadas"
    }
}
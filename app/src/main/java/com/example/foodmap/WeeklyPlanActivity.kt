package com.example.foodmap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmap.models.RefeicaoResponse
import com.example.foodmap.models.StatusRequest
import com.example.foodmap.network.ApiResponse
import com.example.foodmap.network.FoodScannerActivity
import com.example.foodmap.network.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeeklyPlanActivity : AppCompatActivity() {

    private lateinit var recyclerPlan: RecyclerView
    private lateinit var adapter: WeeklyPlanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_plan)

        recyclerPlan = findViewById(R.id.recyclerPlan)
        recyclerPlan.layoutManager = LinearLayoutManager(this)

        val btnAddMeal = findViewById<Button>(R.id.btnAddMeal)
        val btnStats = findViewById<Button>(R.id.btnStats)
        val btnOpenScanner = findViewById<FloatingActionButton>(R.id.btnOpenScanner)

        btnAddMeal.setOnClickListener {
            startActivity(Intent(this, AddMealActivity::class.java))
        }

        btnStats.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        btnOpenScanner.setOnClickListener {
            startActivity(Intent(this, FoodScannerActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        carregarRefeicoesDoServidor()
    }

    private fun carregarRefeicoesDoServidor() {

        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "Faça login novamente", Toast.LENGTH_SHORT).show()
            return
        }


        RetrofitClient.instance.getRefeicoes(userId).enqueue(object : Callback<List<RefeicaoResponse>> {
            override fun onResponse(call: Call<List<RefeicaoResponse>>, response: Response<List<RefeicaoResponse>>) {
                if (response.isSuccessful) {
                    val listaDoServidor = response.body() ?: emptyList()


                    val listaMeal = listaDoServidor.map { ref ->
                        Meal(
                            id = ref.id.toLong(),
                            dayOfWeek = ref.dia_semana,
                            type = ref.tipo_refeicao,
                            description = ref.descricao,
                            calories = ref.calorias,
                            isDone = ref.concluido
                        )
                    }


                    adapter = WeeklyPlanAdapter(listaMeal) { meal, isChecked ->
                        atualizarStatusNoServidor(meal.id, isChecked)
                    }
                    recyclerPlan.adapter = adapter
                } else {
                    Toast.makeText(this@WeeklyPlanActivity, "Erro ao carregar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<RefeicaoResponse>>, t: Throwable) {
                Toast.makeText(this@WeeklyPlanActivity, "Falha na conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun atualizarStatusNoServidor(mealId: Long, isChecked: Boolean) {
        val request = StatusRequest(concluido = isChecked)

        RetrofitClient.instance.atualizarStatus(mealId, request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {

                    println("Status atualizado com sucesso para ID: $mealId")
                } else {
                    Toast.makeText(this@WeeklyPlanActivity, "Erro ao atualizar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@WeeklyPlanActivity, "Falha de conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
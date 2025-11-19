package com.example.foodmap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmap.network.FoodScannerActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WeeklyPlanActivity : AppCompatActivity() {

    private lateinit var recyclerPlan: RecyclerView
    private lateinit var adapter: WeeklyPlanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_plan)

        // 1. Configurar o RecyclerView (Lista)
        recyclerPlan = findViewById(R.id.recyclerPlan)
        recyclerPlan.layoutManager = LinearLayoutManager(this)

        // 2. Encontrar os botões
        val btnAddMeal = findViewById<Button>(R.id.btnAddMeal)
        val btnStats = findViewById<Button>(R.id.btnStats)
        val btnOpenScanner = findViewById<FloatingActionButton>(R.id.btnOpenScanner) // O novo botão

        // 3. Configurar cliques

        // Botão "Adicionar": Vai para a tela de criar refeição
        btnAddMeal.setOnClickListener {
            startActivity(Intent(this, AddMealActivity::class.java))
        }

        // Botão "Estatísticas": Vai para a tela de gráficos
        btnStats.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        // Botão "Câmera" (Flutuante): Abre o Scanner de Alimentos
        btnOpenScanner.setOnClickListener {
            val intent = Intent(this, FoodScannerActivity::class.java)
            startActivity(intent)
        }
    }

    // onResume é chamado sempre que a tela volta a aparecer.
    // Usamos isso para atualizar a lista caso você tenha adicionado uma refeição nova.
    override fun onResume() {
        super.onResume()
        loadMeals()
    }

    private fun loadMeals() {
        // Carrega a lista de refeições salvas
        val meals = MealManager.getMeals(this)

        // Configura o Adapter
        adapter = WeeklyPlanAdapter(meals) { meal, isChecked ->
            // Quando marcar/desmarcar o checkbox, salva o novo estado
            MealManager.updateMealStatus(this, meal.id, isChecked)
        }

        recyclerPlan.adapter = adapter
    }
}
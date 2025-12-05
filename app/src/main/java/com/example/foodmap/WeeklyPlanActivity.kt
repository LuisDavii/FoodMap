package com.example.foodmap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmap.models.RefeicaoResponse
import com.example.foodmap.models.StatusRequest
import com.example.foodmap.network.ApiResponse
// AQUI ESTÁ A CORREÇÃO IMPORTANTE:
import com.example.foodmap.network.FoodScannerActivity
import com.example.foodmap.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class WeeklyPlanActivity : AppCompatActivity() {

    // --- Variáveis da UI ---
    private lateinit var recyclerPlan: RecyclerView
    private lateinit var btnLogout: ImageView
    private lateinit var adapter: WeeklyPlanAdapter
    private lateinit var btnAddMeal: Button
    private lateinit var btnEditMeta: ImageView

    // Variáveis das Estatísticas
    private lateinit var tvMetaValue: TextView
    private lateinit var pbDaily: ProgressBar
    private lateinit var pbWeekly: ProgressBar
    private lateinit var tvProgressWeekly: TextView
    private lateinit var tvTotalMeals: TextView

    // --- Dados ---
    private var dailyGoal = 2000
    private var currentMealList: List<RefeicaoResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_plan)

        initComponents()
        setupRecyclerView()
        loadSavedGoal()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        carregarDadosCompletos()
    }

    private fun initComponents() {
        recyclerPlan = findViewById(R.id.recyclerPlan)
        btnAddMeal = findViewById(R.id.btnAddMeal)

        btnEditMeta = findViewById(R.id.btnEditMeta)
        tvMetaValue = findViewById(R.id.tvMetaValue)
        pbDaily = findViewById(R.id.progressBarDaily)
        pbWeekly = findViewById(R.id.progressBarWeekly)
        tvProgressWeekly = findViewById(R.id.tvProgressWeekly)
        tvTotalMeals = findViewById(R.id.tvTotalMeals)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupRecyclerView() {
        recyclerPlan.layoutManager = LinearLayoutManager(this)
    }

    private fun loadSavedGoal() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        dailyGoal = sharedPref.getInt("daily_goal", 2000)
        atualizarTextoMeta()
    }

    private fun atualizarTextoMeta() {
        tvMetaValue.text = "$dailyGoal kcal"
    }

    private fun setupClickListeners() {
        // 1. Botão Adicionar Refeição -> Abre o SCANNER
        btnAddMeal.setOnClickListener {
            val intent = Intent(this, FoodScannerActivity::class.java)
            startActivity(intent)
        }

        // 2. Botão Editar Meta (Lápis)
        btnEditMeta.setOnClickListener {
            mostrarDialogEditarMeta()
        }

        btnLogout.setOnClickListener {
            fazerLogout()
        }
    }

    private fun fazerLogout() {
        // 1. Limpa o SharedPreferences (apaga o "is_logged_in" e dados do usuário)
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear() // Apaga tudo
        editor.apply()

        // 2. Volta para a tela de Login
        val intent = Intent(this, Login::class.java)
        // Essas flags limpam a pilha de telas, impedindo que o usuário volte
        // para o plano semanal apertando o botão "Voltar" do celular.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // --- CARREGAMENTO DE DADOS (LISTA + ESTATÍSTICAS) ---
    private fun carregarDadosCompletos() {
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
                    currentMealList = listaDoServidor

                    configurarLista(listaDoServidor)
                    calcularEstatisticas(listaDoServidor)

                } else {
                    Toast.makeText(this@WeeklyPlanActivity, "Erro ao carregar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<RefeicaoResponse>>, t: Throwable) {
                Toast.makeText(this@WeeklyPlanActivity, "Falha na conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun configurarLista(listaRefeicoes: List<RefeicaoResponse>) {
        val listaMeal = listaRefeicoes.map { ref ->
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
    }

    // --- LÓGICA DE ESTATÍSTICAS ---
    private fun calcularEstatisticas(todasRefeicoes: List<RefeicaoResponse>) {
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

        val progressoSemanal = if (metaSemanal > 0) (caloriasIngeridasSemana * 100) / metaSemanal else 0
        pbWeekly.progress = progressoSemanal.coerceAtMost(100)

        tvProgressWeekly.text = "$progressoSemanal%"
        tvTotalMeals.text = todasRefeicoes.size.toString()
    }

    // --- DIALOG DE EDITAR META ---
    private fun mostrarDialogEditarMeta() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Definir Meta Diária")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "Ex: 2000"
        input.setText(dailyGoal.toString())
        builder.setView(input)

        builder.setPositiveButton("Salvar") { dialog, _ ->
            val textoDigitado = input.text.toString()
            if (textoDigitado.isNotEmpty()) {
                val novaMeta = textoDigitado.toIntOrNull()
                if (novaMeta != null && novaMeta > 0) {
                    salvarNovaMeta(novaMeta)
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun salvarNovaMeta(novaMeta: Int) {
        dailyGoal = novaMeta
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putInt("daily_goal", dailyGoal).apply()

        atualizarTextoMeta()
        calcularEstatisticas(currentMealList)
        Toast.makeText(this, "Meta atualizada!", Toast.LENGTH_SHORT).show()
    }

    private fun atualizarStatusNoServidor(mealId: Long, isChecked: Boolean) {
        val request = StatusRequest(concluido = isChecked)

        RetrofitClient.instance.atualizarStatus(mealId, request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    carregarDadosCompletos()
                } else {
                    Toast.makeText(this@WeeklyPlanActivity, "Erro ao atualizar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@WeeklyPlanActivity, "Falha de conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
package com.example.foodmap.network

import android.content.Intent // <-- MUDANÇA 1: IMPORT ADICIONADO
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.foodmap.HistoryActivity // <-- MUDANÇA 2: IMPORT ADICIONADO
import com.example.foodmap.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Data class para representar os dados nutricionais de um alimento.
 * Simula a resposta que viria de um serviço de Visão de IA.
 */
data class FoodAnalysis(
    val name: String,
    val caloriesKcal: Int,
    val proteinGrams: Double,
    val fiberGrams: Double
)

class FoodScannerActivity : AppCompatActivity() {

    // Componentes da UI
    private lateinit var textViewLoggedInUser: TextView
    private lateinit var buttonHistory: Button // <-- MUDANÇA 3: NOME DA VARIÁVEL CORRIGIDO
    private lateinit var buttonScan: Button
    private lateinit var cameraPreview: View // Placeholder para a superfície da câmera
    private lateinit var cardResult: CardView
    private lateinit var textViewFoodName: TextView
    private lateinit var textViewCalories: TextView
    private lateinit var textViewProtein: TextView

    // Variáveis de estado simuladas
    private val loggedInUserName = "João"
    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        // 1. Inicializa as Views
        initializeViews()

        // 2. Configura a UI inicial (e placeholders de câmera)
        setupInitialUI()

        // 3. Configura os Listeners de Clique
        setupClickListeners()
    }

    private fun initializeViews() {
        // Inicializa elementos do Header
        textViewLoggedInUser = findViewById(R.id.textViewLoggedInUser)
        buttonHistory = findViewById(R.id.buttonHistory) // <-- MUDANÇA 4: NOME DA VARIÁVEL CORRIGIDO

        // Inicializa elementos do Scanner
        cameraPreview = findViewById(R.id.cameraPreview)
        buttonScan = findViewById(R.id.buttonScan)

        // Inicializa elementos do Card de Resultado
        cardResult = findViewById(R.id.cardResult)
        textViewFoodName = findViewById(R.id.textViewFoodName)
        textViewCalories = findViewById(R.id.textViewCalories)
        textViewProtein = findViewById(R.id.textViewProtein)
    }

    private fun setupInitialUI() {
        // Define o nome do usuário logado
        textViewLoggedInUser.text = "Olá, $loggedInUserName!"

        // Garante que o cartão de resultado esteja inicialmente oculto
        cardResult.visibility = View.GONE
    }

    private fun setupClickListeners() {
        // Lógica para o botão VER HISTÓRICO
        buttonHistory.setOnClickListener { // <-- MUDANÇA 5: NOME DA VARIÁVEL CORRIGIDO
            // Cria a intenção de ir para a tela de histórico
            val intent = Intent(this, HistoryActivity::class.java)
            // Inicia a nova tela
            startActivity(intent)
        }

        // Lógica para o botão ANALISAR ALIMENTO
        buttonScan.setOnClickListener {
            if (!isScanning) {
                scanFood()
            } else {
                Toast.makeText(this, "Aguarde, a análise anterior está em andamento.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // <-- MUDANÇA 6: FUNÇÃO ANTIGA E NÃO UTILIZADA 'handleLogout' FOI REMOVIDA

    /**
     * Inicia a simulação de análise de alimentos.
     * Usa Coroutines para executar o trabalho em segundo plano.
     */
    private fun scanFood() {
        isScanning = true
        buttonScan.isEnabled = false
        buttonScan.text = "ANALISANDO..."
        cardResult.visibility = View.GONE
        Toast.makeText(this, "Análise de imagem iniciada...", Toast.LENGTH_LONG).show()

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                delay(3000)
                return@withContext generateFoodAnalysis()
            }
            updateUIWithAnalysis(result)
            isScanning = false
            buttonScan.isEnabled = true
            buttonScan.text = "ANALISAR ALIMENTO"
            Toast.makeText(this@FoodScannerActivity, "Análise concluída!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Simula o retorno de um modelo de Visão de IA, retornando dados nutricionais.
     */
    private fun generateFoodAnalysis(): FoodAnalysis {
        val foodName = "Brócolis Cozido"
        val calories = 34
        val protein = 2.8
        val fiber = 2.6

        return FoodAnalysis(foodName, calories, protein, fiber)
    }

    /**
     * Atualiza os campos de texto do CardView com os resultados da análise.
     */
    private fun updateUIWithAnalysis(analysis: FoodAnalysis) {
        textViewFoodName.text = "Alimento: ${analysis.name}"
        textViewCalories.text = "Calorias (100g): ${analysis.caloriesKcal} kcal"
        textViewProtein.text = "Proteína: ${analysis.proteinGrams}g | Fibra: ${analysis.fiberGrams}g"
        cardResult.visibility = View.VISIBLE
    }
}
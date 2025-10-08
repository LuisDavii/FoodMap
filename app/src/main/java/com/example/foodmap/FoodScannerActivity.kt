package com.example.foodmap.network
// ATENÇÃO: Se o seu pacote raiz for diferente (ex: com.example.meuapp),
// você deve ajustar o 'import R' abaixo para corresponder ao seu pacote raiz.

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
// ESTE É O IMPORT QUE RESOLVE O ERRO 'UNRESOLVED REFERENCE R'
import com.example.foodmap.R // *** AJUSTE ESTE IMPORT PARA O SEU PACOTE RAIZ ***
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
    private lateinit var buttonLogout: Button
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
        // O ID do layout (activity_food_scanner)
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
        buttonLogout = findViewById(R.id.buttonHistory)

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

        // --- PLACEHOLDER DA CÂMERA ---
        // Em um projeto real, aqui você inicializaria o CameraX ou uma SurfaceView
        // e faria a solicitação de permissões de câmera.
    }

    private fun setupClickListeners() {
        // Lógica para o botão SAIR
        buttonLogout.setOnClickListener {
            handleLogout()
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

    /**
     * Simula a lógica de logout.
     */
    private fun handleLogout() {
        Toast.makeText(this, "Usuário $loggedInUserName desconectado.", Toast.LENGTH_SHORT).show()
        // Adicione aqui a navegação para a tela de Login
    }

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

        // Usa lifecycleScope para iniciar uma coroutine vinculada ao ciclo de vida da Activity
        lifecycleScope.launch {
            // 1. Simulação do Processamento (em thread de IO/Background)
            val result = withContext(Dispatchers.IO) {
                // Simula um atraso de rede/processamento de 3 segundos
                delay(3000)

                // Retorna um resultado de análise simulado
                return@withContext generateFoodAnalysis()
            }

            // 2. Atualização da UI (de volta na thread Principal)
            updateUIWithAnalysis(result)

            // 3. Finaliza o estado de scanning
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
        // Define o texto nos TextViews
        textViewFoodName.text = "Alimento: ${analysis.name}"
        textViewCalories.text = "Calorias (100g): ${analysis.caloriesKcal} kcal"
        textViewProtein.text = "Proteína: ${analysis.proteinGrams}g | Fibra: ${analysis.fiberGrams}g"

        // Torna o cartão visível
        cardResult.visibility = View.VISIBLE
    }
}

package com.example.foodmap.network

// Imports de UI e Navegação

// Imports do CameraX

// Imports do TFLite

// Imports de IO e Concorrência

// Imports do Projeto
// <-- MUDANÇA: Importa o ScanResult que será usado para salvar
// <-- MUDANÇA: Importa o FoodDatabaseEntry que será usado para ler o JSON


// MUDANÇA: Imports para ler JSON (Gson)
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.foodmap.FoodDatabaseEntry
import com.example.foodmap.HistoryActivity
import com.example.foodmap.HistoryManager
import com.example.foodmap.R
import com.example.foodmap.ScanResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FoodScannerActivity : AppCompatActivity() {

    // --- Componentes da UI (sem alteração) ---
    private lateinit var textViewLoggedInUser: TextView
    private lateinit var buttonHistory: Button
    private lateinit var buttonScan: Button
    private lateinit var cameraPreview: PreviewView
    private lateinit var cardResult: CardView
    private lateinit var textViewFoodName: TextView
    private lateinit var textViewCalories: TextView
    private lateinit var textViewProtein: TextView
    private lateinit var buttonSaveScan: Button

    // --- Lógica de Estado ---
    private val loggedInUserName = "João" // (Isto pode ser atualizado para buscar o nome salvo no Login)

    // <-- CORREÇÃO 1: O tipo da variável foi alterado para ScanResult
    // Esta variável guarda o ÚLTIMO resultado exibido na tela, pronto para ser salvo.
    private var currentScanResult: ScanResult? = null

    // --- Propriedades da Câmera e TFLite (sem alteração) ---
    private lateinit var interpreter: Interpreter
    private lateinit var labels: List<String>
    private lateinit var cameraExecutor: ExecutorService
    private var modelInputWidth: Int = 0
    private var modelInputHeight: Int = 0
    @Volatile
    private var currentPredictedFood: String? = null

    // --- MUDANÇA: Nossa nova base de dados em memória ---
    private lateinit var foodDatabase: Map<String, FoodAnalysis>

    // ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        // MUDANÇA: 1. Carrega a nossa base de dados JSON
        loadFoodDatabase()

        // 2. Inicializa as Views da UI
        initializeViews()

        // 3. Configura a UI inicial (Nome de usuário, etc.)
        setupInitialUI()

        // 4. Configura os Listeners de Clique (Botões)
        setupClickListeners()

        // 5. Configura a Câmera e o Modelo TFLite
        cameraExecutor = Executors.newSingleThreadExecutor()
        if (isCameraPermissionGranted()) {
            setupModelAndLabels()
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Desliga o executor da câmera para evitar memory leaks
        cameraExecutor.shutdown()
    }

    private fun initializeViews() {
        // Header
        textViewLoggedInUser = findViewById(R.id.textViewLoggedInUser)
        buttonHistory = findViewById(R.id.buttonHistory)

        // Câmera e Scan
        cameraPreview = findViewById(R.id.cameraPreview)
        buttonScan = findViewById(R.id.buttonScan)

        // Card de Resultado
        cardResult = findViewById(R.id.cardResult)
        textViewFoodName = findViewById(R.id.textViewFoodName)
        textViewCalories = findViewById(R.id.textViewCalories)
        textViewProtein = findViewById(R.id.textViewProtein)
        buttonSaveScan = findViewById(R.id.buttonSaveScan)
    }

    private fun setupInitialUI() {
        // Tenta buscar o nome do usuário salvo nas SharedPreferences do Login
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userName = sharedPref.getString("user_name", loggedInUserName) // Usa "João" como padrão

        textViewLoggedInUser.text = "Olá, $userName!"
        cardResult.visibility = View.GONE
    }

    private fun setupClickListeners() {
        // Botão VER HISTÓRICO
        buttonHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        // Botão ANALISAR ALIMENTO (MUDANÇA DE LÓGICA)
        buttonScan.setOnClickListener {
            val foodName = currentPredictedFood

            if (foodName.isNullOrEmpty()) {
                Toast.makeText(this, "Aponte a câmera para um alimento.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // MUDANÇA: Agora usa a nossa nova função que consulta a base de dados
            val analysis = generateFoodAnalysis(foodName)

            updateUIWithAnalysis(analysis)
        }

        // NOVO: Listener para o botão SALVAR SCAN
        buttonSaveScan.setOnClickListener {
            currentScanResult?.let { resultToSave ->
                // Usa o HistoryManager para salvar o item
                HistoryManager.saveScan(this, resultToSave)
                Toast.makeText(this, "Salvo no histórico!", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "Nenhum resultado para salvar.", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Atualiza o CardView com os resultados da análise.
     */
    private fun updateUIWithAnalysis(analysis: FoodAnalysis) {
        // Atualiza os textos
        textViewFoodName.text = "Alimento: ${analysis.name}"
        textViewCalories.text = "Calorias (100g): ${analysis.caloriesKcal} kcal"
        textViewProtein.text = "Proteína: ${analysis.proteinGrams}g | Fibra: ${analysis.fiberGrams}g"

        // Exibe o card
        cardResult.visibility = View.VISIBLE

        // Cria o objeto de dados que pode ser salvo
        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // <-- CORREÇÃO 2: Criamos um ScanResult (que o HistoryManager espera)
        currentScanResult = ScanResult(
            foodName = analysis.name,
            calories = analysis.caloriesKcal,
            protein = analysis.proteinGrams,
            fiber = analysis.fiberGrams,
            timestamp = timestamp
        )
    }

    // --- MUDANÇAS PRINCIPAIS AQUI ---

    /**
     * MUDANÇA: Esta função agora lê o nosso 'food_database.json'
     * e o carrega para um Map em memória para acesso rápido.
     */
    private fun loadFoodDatabase() {
        try {
            // 1. Abre o ficheiro JSON da pasta 'assets'
            val jsonString = assets.open("food_database.json")
                .bufferedReader()
                .use { it.readText() }

            // 2. Define o "tipo" que o Gson deve esperar (uma Lista de FoodDatabaseEntry)
            val listType = object : TypeToken<List<FoodDatabaseEntry>>() {}.type

            // 3. Converte o JSON (string) para uma Lista de objetos
            val entries: List<FoodDatabaseEntry> = Gson().fromJson(jsonString, listType)

            // 4. Converte a Lista num Map para acesso instantâneo
            foodDatabase = entries.associateBy(
                { it.key }, // Chave do Map
                {
                    // Valor do Map
                    FoodAnalysis(
                        it.name,
                        it.caloriesKcal,
                        it.proteinGrams,
                        it.fiberGrams
                    )
                }
            )

            Log.d("FoodScannerActivity", "Base de dados JSON carregada com ${foodDatabase.size} itens.")

        } catch (e: IOException) {
            Log.e("FoodScannerActivity", "Erro ao ler food_database.json", e)
            Toast.makeText(this, "Erro ao carregar base de dados de alimentos.", Toast.LENGTH_LONG).show()
            foodDatabase = emptyMap() // Garante que a app não falhe
        }
    }

    /**
     * MUDANÇA: Esta função foi completamente substituída.
     * Em vez de um 'when' statement, agora ela consulta o nosso Map.
     */
    private fun generateFoodAnalysis(foodName: String): FoodAnalysis {
        // 1. Procura o 'foodName' (ex: "broccoli") no nosso Map.
        val data = foodDatabase[foodName.lowercase()]

        if (data != null) {
            // 2. Se encontrou, retorna os dados do JSON
            return data
        } else {
            // 3. Se não encontrou, retorna um resultado padrão (igual a antes)
            Log.w("FoodScannerActivity", "Alimento '$foodName' não encontrado na base de dados JSON.")
            // <-- CORREÇÃO 3: Removido o '0' extra. A assinatura é (String, Int, Double, Double)
            return FoodAnalysis(foodName, 0, 0.0, 0.0)
        }
    }

    // --- (Fim das Mudanças Principais) ---


    // =======================================================================
    // == LÓGICA DE CÂMERA E TFLITE (sem alteração)
    // =======================================================================

    private fun isCameraPermissionGranted() =
        ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupModelAndLabels()
                startCamera()
            } else {
                Toast.makeText(this, "Permissão da câmera negada.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private var modelInputDataType: org.tensorflow.lite.DataType? = null
    private var outputBuffer: ByteBuffer? = null
    private var outputShape: IntArray? = null

    // ---

    private fun setupModelAndLabels() {
        try {
            // <-- CORREÇÃO 1: Carrega o seu novo modelo
            val modelBuffer = loadModelFile("meu_modelo_comida.tflite")
            interpreter = Interpreter(modelBuffer)

            // <-- CORREÇÃO 2: Carrega o seu novo ficheiro de labels
            labels = FileUtil.loadLabels(this, "labels_comida.txt")

            // --- Lógica de Input (Entrada) ---
            val inputTensor = interpreter.getInputTensor(0)
            val inputShape = inputTensor.shape()
            modelInputHeight = inputShape[1] // ex: 224
            modelInputWidth = inputShape[2]  // ex: 224

            // <-- CORREÇÃO 3: Guarda o TIPO de dados (ex: FLOAT32)
            modelInputDataType = inputTensor.dataType()

            // --- Lógica de Output (Saída) ---
            val outputTensor = interpreter.getOutputTensor(0)

            // <-- CORREÇÃO 4: Guarda o formato e tamanho da saída
            outputShape = outputTensor.shape() // ex: [1, 55]
            val outputType = outputTensor.dataType() // ex: FLOAT32

            // Aloca o buffer de saída com o tamanho CORRETO
            // (ex: 55 labels * 4 bytes/float = 220 bytes)
            val outputSizeInBytes = outputShape!![1] * outputType.byteSize()
            outputBuffer = ByteBuffer.allocateDirect(outputSizeInBytes)
            outputBuffer!!.order(java.nio.ByteOrder.nativeOrder()) // Importante

        } catch (e: Exception) {
            Log.e("FoodScannerActivity", "Erro ao carregar modelo ou labels.", e)
            Toast.makeText(this, "Erro ao carregar modelo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(cameraPreview.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        val bitmap = imageProxy.toBitmap()
                        if (bitmap != null) {

                            // --- INÍCIO DA CORREÇÃO ---

                            // MUDANÇA 1: Corrigir a ENTRADA (Input)
                            // Temos de criar o TensorImage com o TIPO de dados correto
                            // que lemos do modelo (ex: FLOAT32).
                            val tensorImage = TensorImage(modelInputDataType!!)
                            tensorImage.load(bitmap)

                            // O processador de imagem (resize) continua igual
                            val imageProcessor = ImageProcessor.Builder()
                                .add(ResizeOp(modelInputHeight, modelInputWidth, ResizeOp.ResizeMethod.BILINEAR))
                                // NOTA: Se o teu modelo FLOAT32 espera valores normalizados (ex: 0.0 a 1.0),
                                // terias de adicionar aqui: .add(NormalizeOp(0f, 255f))
                                // Por agora, vamos manter simples.
                                .build()
                            val processedImage = imageProcessor.process(tensorImage)

                            // MUDANÇA 2: Corrigir a SAÍDA (Output)
                            // O código antigo estava a criar um novo 'outputBuffer' local e incorreto.
                            // Vamos usar o 'outputBuffer' DA CLASSE, que foi inicializado
                            // corretamente em 'setupModelAndLabels'.

                            // 1. Prepara o buffer de saída da classe para ser escrito
                            outputBuffer!!.rewind()

                            // 2. Executa o modelo
                            //    Input: processedImage.buffer (agora com o tamanho correto)
                            //    Output: outputBuffer (o da classe, com o tamanho correto)
                            interpreter.run(processedImage.buffer, outputBuffer!!)

                            // 3. Prepara o buffer de saída da classe para ser lido
                            outputBuffer!!.rewind()

                            // MUDANÇA 3: Ler os resultados como FLOAT
                            // O código antigo lia os resultados como UINT8 (com 'and 0xFF'),
                            // o que está incorreto para um modelo FLOAT32.

                            // Assumindo que a saída do modelo é FLOAT32
                            val scores = outputBuffer!!.asFloatBuffer()

                            var maxScore = -Float.MAX_VALUE
                            var maxScoreIndex = -1

                            // Loop para encontrar a pontuação mais alta
                            for (i in 0 until labels.size) {
                                val score = scores.get(i)
                                if (score > maxScore) {
                                    maxScore = score
                                    maxScoreIndex = i
                                }
                            }
                            // --- FIM DA CORREÇÃO ---

                            if (maxScoreIndex != -1) {
                                currentPredictedFood = labels[maxScoreIndex]
                            }
                        }
                        imageProxy.close()
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("FoodScannerActivity", "Falha ao vincular a câmera", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}

/**
 * Data class para representar os dados nutricionais de um alimento.
 * (Esta classe já estava no seu ficheiro, mantida aqui por consistência)
 */
data class FoodAnalysis(
    val name: String,
    val caloriesKcal: Int,
    val proteinGrams: Double,
    val fiberGrams: Double
)
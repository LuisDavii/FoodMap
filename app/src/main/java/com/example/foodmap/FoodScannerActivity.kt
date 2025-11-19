package com.example.foodmap.network

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
import com.example.foodmap.AddMealActivity
import com.example.foodmap.FoodDatabaseEntry
import com.example.foodmap.R
import com.example.foodmap.ScanResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
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
    private val CONFIDENCE_THRESHOLD = 0.70f
    private lateinit var textViewLoggedInUser: TextView

    private lateinit var buttonScan: Button
    private lateinit var cameraPreview: PreviewView
    private lateinit var cardResult: CardView
    private lateinit var textViewFoodName: TextView
    private lateinit var textViewCalories: TextView
    private lateinit var textViewProtein: TextView
    private lateinit var buttonSaveScan: Button

    private val loggedInUserName = "Testee"
    private var currentScanResult: ScanResult? = null

    private lateinit var interpreter: Interpreter
    private lateinit var labels: List<String>
    private lateinit var cameraExecutor: ExecutorService
    private var modelInputWidth: Int = 0
    private var modelInputHeight: Int = 0
    @Volatile
    private var currentPredictedFood: String? = null

    private lateinit var foodDatabase: Map<String, FoodAnalysis>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        loadFoodDatabase()

        initializeViews()

        setupClickListeners()

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
        cameraExecutor.shutdown()
    }

    private fun initializeViews() {

        cameraPreview = findViewById(R.id.cameraPreview)
        buttonScan = findViewById(R.id.buttonScan)

        cardResult = findViewById(R.id.cardResult)
        textViewFoodName = findViewById(R.id.textViewFoodName)
        textViewCalories = findViewById(R.id.textViewCalories)
        textViewProtein = findViewById(R.id.textViewProtein)
        buttonSaveScan = findViewById(R.id.buttonSaveScan)
    }


    private fun setupClickListeners() {


        buttonScan.setOnClickListener {
            val foodName = currentPredictedFood

            if (foodName.isNullOrEmpty()) {
                Toast.makeText(this, "Aponte a câmera para um alimento.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val analysis = generateFoodAnalysis(foodName)

            updateUIWithAnalysis(analysis)
        }


        buttonSaveScan.setOnClickListener {
            currentScanResult?.let { resultToSave ->

                val intent = Intent(this, AddMealActivity::class.java)

                intent.putExtra("FOOD_NAME", resultToSave.foodName)
                intent.putExtra("FOOD_CALORIES", resultToSave.calories)

                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "Nenhum alimento identificado para adicionar.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    /**
     * Atualiza o CardView com os resultados da análise.
     */
    private fun updateUIWithAnalysis(analysis: FoodAnalysis) {

        textViewFoodName.text = "Alimento: ${analysis.name}"
        textViewCalories.text = "Calorias (100g): ${analysis.caloriesKcal} kcal"
        textViewProtein.text = "Proteína: ${analysis.proteinGrams}g | Fibra: ${analysis.fiberGrams}g"

        cardResult.visibility = View.VISIBLE

        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        currentScanResult = ScanResult(
            foodName = analysis.name,
            calories = analysis.caloriesKcal,
            protein = analysis.proteinGrams,
            fiber = analysis.fiberGrams,
            timestamp = timestamp
        )
    }

    /**
     * MUDANÇA: Esta função agora lê o nosso 'food_database.json'
     * e o carrega para um Map em memória para acesso rápido.
     */
    private fun loadFoodDatabase() {
        try {
            val jsonString = assets.open("food_database.json")
                .bufferedReader()
                .use { it.readText() }

            val listType = object : TypeToken<List<FoodDatabaseEntry>>() {}.type

            val entries: List<FoodDatabaseEntry> = Gson().fromJson(jsonString, listType)

            foodDatabase = entries.associateBy(
                { it.key },
                {
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
            foodDatabase = emptyMap()
        }
    }

    /**
     * MUDANÇA: Esta função foi completamente substituída.
     * Em vez de um 'when' statement, agora ela consulta o nosso Map.
     */
    private fun generateFoodAnalysis(foodName: String): FoodAnalysis {
        val data = foodDatabase[foodName.lowercase()]

        if (data != null) {
            return data
        } else {
            Log.w("FoodScannerActivity", "Alimento '$foodName' não encontrado na base de dados JSON.")
            return FoodAnalysis(foodName, 0, 0.0, 0.0)
        }
    }

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

    private fun setupModelAndLabels() {
        try {

            val modelBuffer = loadModelFile("meu_modelo_comida.tflite")
            interpreter = Interpreter(modelBuffer)


            labels = FileUtil.loadLabels(this, "labels_comida.txt")


            val inputTensor = interpreter.getInputTensor(0)
            val inputShape = inputTensor.shape()
            modelInputHeight = inputShape[1]
            modelInputWidth = inputShape[2]


            modelInputDataType = inputTensor.dataType()


            val outputTensor = interpreter.getOutputTensor(0)


            outputShape = outputTensor.shape()
            val outputType = outputTensor.dataType()

            val outputSizeInBytes = outputShape!![1] * outputType.byteSize()
            outputBuffer = ByteBuffer.allocateDirect(outputSizeInBytes)
            outputBuffer!!.order(java.nio.ByteOrder.nativeOrder())

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

                            val tensorImage = TensorImage(modelInputDataType!!)
                            tensorImage.load(bitmap)
                            val imageProcessor = ImageProcessor.Builder()
                                .add(ResizeOp(modelInputHeight, modelInputWidth, ResizeOp.ResizeMethod.BILINEAR))
                                .add(NormalizeOp(0.0f, 255.0f))
                                .build()
                            val processedImage = imageProcessor.process(tensorImage)


                            outputBuffer!!.rewind()

                            interpreter.run(processedImage.buffer, outputBuffer!!)

                            outputBuffer!!.rewind()

                            val scores = outputBuffer!!.asFloatBuffer()

                            var maxScore = -Float.MAX_VALUE
                            var maxScoreIndex = -1

                            for (i in 0 until labels.size) {
                                val score = scores.get(i)
                                if (score > maxScore) {
                                    maxScore = score
                                    maxScoreIndex = i
                                }
                            }

                            if (maxScoreIndex != -1) {
                                if (maxScore >= CONFIDENCE_THRESHOLD) {
                                    currentPredictedFood = labels[maxScoreIndex]

                                    Log.d("FoodScanner", "Detectado: ${labels[maxScoreIndex]} com ${(maxScore * 100).toInt()}%")
                                } else {
                                    currentPredictedFood = null

                                    Log.d("FoodScanner", "Rejeitado: ${labels[maxScoreIndex]} com apenas ${(maxScore * 100).toInt()}%")
                                }
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
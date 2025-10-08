package com.example.foodmap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraPreview: PreviewView
    private lateinit var textPrediction: TextView
    private lateinit var interpreter: Interpreter
    private lateinit var labels: List<String>

    private var modelInputWidth: Int = 0
    private var modelInputHeight: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cameraPreview = findViewById(R.id.cameraPreview)
        textPrediction = findViewById(R.id.textPrediction)

        if (isCameraPermissionGranted()) {
            setupModelAndLabels()
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun isCameraPermissionGranted() =
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupModelAndLabels()
                startCamera()
            } else {
                Toast.makeText(this, "Permissão da câmera negada.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupModelAndLabels() {
        try {
            // Carrega o modelo .tflite da pasta assets
            val modelBuffer = loadModelFile("mobilenet_v1_1.0_224_quant.tflite")
            interpreter = Interpreter(modelBuffer)

            // Carrega os rótulos do arquivo labels.txt
            labels = FileUtil.loadLabels(this, "labels.txt")

            // Obtém as dimensões de entrada que o modelo espera
            val inputTensor = interpreter.getInputTensor(0)
            val inputShape = inputTensor.shape()
            modelInputHeight = inputShape[1]
            modelInputWidth = inputShape[2]

        } catch (e: Exception) {
            Log.e("CameraActivity", "Erro ao carregar modelo ou labels.", e)
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
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        val bitmap = imageProxy.toBitmap()

                        if (bitmap != null) {
                            // Prepara a imagem para o modelo
                            val tensorImage = TensorImage.fromBitmap(bitmap)
                            val imageProcessor = ImageProcessor.Builder()
                                .add(ResizeOp(modelInputHeight, modelInputWidth, ResizeOp.ResizeMethod.BILINEAR))
                                .build()
                            val processedImage = imageProcessor.process(tensorImage)

                            // Prepara o buffer de saída
                            val outputBuffer = ByteBuffer.allocateDirect(labels.size)
                            outputBuffer.rewind()

                            // Executa a inferência
                            interpreter.run(processedImage.buffer, outputBuffer)

                            // Processa o resultado
                            outputBuffer.rewind()
                            val scores = outputBuffer.asReadOnlyBuffer()
                            val maxScoreIndex = (0 until labels.size).maxByOrNull { scores.get(it).toInt() and 0xFF } ?: -1

                            // Atualiza a UI
                            runOnUiThread {
                                if (maxScoreIndex != -1) {
                                    val predictedLabel = labels[maxScoreIndex]
                                    val confidence = (scores.get(maxScoreIndex).toInt() and 0xFF) / 255.0f * 100
                                    textPrediction.text = "$predictedLabel (${"%.1f".format(confidence)}%)"
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
                Log.e("CameraActivity", "Falha ao vincular a câmera", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}
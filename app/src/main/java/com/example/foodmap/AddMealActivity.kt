package com.example.foodmap

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodmap.models.RefeicaoRequest
import com.example.foodmap.network.ApiResponse
import com.example.foodmap.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddMealActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meal)

        val spinnerDay = findViewById<Spinner>(R.id.spinnerDay)
        val spinnerType = findViewById<Spinner>(R.id.spinnerType)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etCalories = findViewById<EditText>(R.id.etCalories)
        val btnSaveMeal = findViewById<Button>(R.id.btnSaveMeal)

        // Configurar os Spinners com opções simples
        val days = arrayOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")
        val types = arrayOf("Café da Manhã", "Almoço", "Jantar", "Lanche")

        spinnerDay.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, days)
        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        // Receber dados da FoodScannerActivity
        val scannedName = intent.getStringExtra("FOOD_NAME")
        val scannedCalories = intent.getIntExtra("FOOD_CALORIES", 0)

        if (!scannedName.isNullOrEmpty()) {
            etDescription.setText(scannedName)
            if (scannedCalories > 0) {
                etCalories.setText(scannedCalories.toString())
            }
            Toast.makeText(this, "Dados do Scanner carregados!", Toast.LENGTH_SHORT).show()
        }

        btnSaveMeal.setOnClickListener {
            val desc = etDescription.text.toString()
            val calStr = etCalories.text.toString()

            if (desc.isNotEmpty() && calStr.isNotEmpty()) {

                // 1. Recuperar o ID do usuário logado (salvo no Login)
                val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                val userId = sharedPref.getInt("user_id", -1) // -1 se não achar

                if (userId == -1) {
                    Toast.makeText(this, "Erro: Usuário não identificado. Faça login novamente.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                // 2. Montar o objeto para envio
                val novaRefeicao = RefeicaoRequest(
                    dia_semana = spinnerDay.selectedItem.toString(),
                    tipo_refeicao = spinnerType.selectedItem.toString(),
                    descricao = desc,
                    calorias = calStr.toInt(),
                    usuario_id = userId,
                    concluido = false
                )

                // 3. Desabilitar botão para evitar cliques duplos
                btnSaveMeal.isEnabled = false
                btnSaveMeal.text = "Salvando..."

                // 4. Enviar para o Backend
                RetrofitClient.instance.salvarRefeicao(novaRefeicao).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        btnSaveMeal.isEnabled = true
                        btnSaveMeal.text = "Salvar Refeição"

                        if (response.isSuccessful) {
                            Toast.makeText(this@AddMealActivity, "Refeição salva no plano!", Toast.LENGTH_SHORT).show()
                            finish() // Fecha a tela e volta
                        } else {
                            Toast.makeText(this@AddMealActivity, "Erro ao salvar: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        btnSaveMeal.isEnabled = true
                        btnSaveMeal.text = "Salvar Refeição"
                        Toast.makeText(this@AddMealActivity, "Falha na conexão: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })

            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
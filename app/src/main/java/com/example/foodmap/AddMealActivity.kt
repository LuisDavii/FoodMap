package com.example.foodmap

import android.os.Bundle
import android.view.View
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

        // MUDANÇA: Agora buscamos como View genérica ou ImageView, pois no XML é uma seta
        val btnBack = findViewById<View>(R.id.btnBack)

        // Configuração dos Spinners (usando nosso layout customizado)
        val days = arrayOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")
        val adapterDays = ArrayAdapter(this, R.layout.item_spinner, days)
        spinnerDay.adapter = adapterDays

        val types = arrayOf("Café da Manhã", "Almoço", "Jantar", "Lanche")
        val adapterTypes = ArrayAdapter(this, R.layout.item_spinner, types)
        spinnerType.adapter = adapterTypes

        // Receber dados do Scanner
        val scannedName = intent.getStringExtra("FOOD_NAME")
        val scannedCalories = intent.getIntExtra("FOOD_CALORIES", 0)

        if (!scannedName.isNullOrEmpty()) {
            etDescription.setText(scannedName)
            if (scannedCalories > 0) {
                etCalories.setText(scannedCalories.toString())
            }
            Toast.makeText(this, "Alimento carregado!", Toast.LENGTH_SHORT).show()
        }

        // Lógica do Botão Voltar (Seta no topo)
        btnBack.setOnClickListener {
            finish()
        }

        // Lógica do Botão Salvar
        btnSaveMeal.setOnClickListener {
            val desc = etDescription.text.toString().trim()
            val calStr = etCalories.text.toString().trim()

            if (desc.isNotEmpty() && calStr.isNotEmpty()) {
                val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                val userId = sharedPref.getInt("user_id", -1)

                if (userId == -1) {
                    Toast.makeText(this, "Erro: Faça login novamente.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val novaRefeicao = RefeicaoRequest(
                    dia_semana = spinnerDay.selectedItem.toString(),
                    tipo_refeicao = spinnerType.selectedItem.toString(),
                    descricao = desc,
                    calorias = calStr.toInt(),
                    usuario_id = userId,
                    concluido = false
                )

                btnSaveMeal.isEnabled = false
                btnSaveMeal.text = "Salvando..."

                RetrofitClient.instance.salvarRefeicao(novaRefeicao).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        btnSaveMeal.isEnabled = true
                        btnSaveMeal.text = "Salvar Refeição"

                        if (response.isSuccessful) {
                            Toast.makeText(this@AddMealActivity, "Refeição adicionada!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@AddMealActivity, "Erro ao salvar", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        btnSaveMeal.isEnabled = true
                        btnSaveMeal.text = "Salvar Refeição"
                        Toast.makeText(this@AddMealActivity, "Erro de conexão", Toast.LENGTH_SHORT).show()
                    }
                })

            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
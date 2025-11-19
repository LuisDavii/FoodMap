package com.example.foodmap

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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

        btnSaveMeal.setOnClickListener {
            val desc = etDescription.text.toString()
            val calStr = etCalories.text.toString()

            if (desc.isNotEmpty() && calStr.isNotEmpty()) {
                val meal = Meal(
                    dayOfWeek = spinnerDay.selectedItem.toString(),
                    type = spinnerType.selectedItem.toString(),
                    description = desc,
                    calories = calStr.toInt()
                )

                // Salva usando o nosso Manager
                MealManager.saveMeal(this, meal)

                Toast.makeText(this, "Refeição salva!", Toast.LENGTH_SHORT).show()
                finish() // Volta para a tela anterior
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
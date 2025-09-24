package com.example.foodmap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Encontra os elementos da tela de login pelos IDs
        val editTextLogin = findViewById<EditText>(R.id.editTextLogin)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val buttonCadastro = findViewById<Button>(R.id.buttonCadastro) // Encontre o novo botão

        // Define a ação para o clique do botão "Entrar"
        buttonLogin.setOnClickListener {
            // Lógica de validação de login
            val login = editTextLogin.text.toString()
            val password = editTextPassword.text.toString()

            if (login == "admin" && password == "123") {
                Toast.makeText(this, R.string.login_sucesso, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.login_falha, Toast.LENGTH_SHORT).show()
            }
        }

        // Define a ação para o clique do botão "Cadastrar"
        buttonCadastro.setOnClickListener {
            // Cria um Intent para ir para a tela de cadastro
            val intent = Intent(this, cadastro::class.java)
            startActivity(intent)
        }
    }
}
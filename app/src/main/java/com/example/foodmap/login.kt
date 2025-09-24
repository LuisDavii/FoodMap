package com.example.foodmap

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // AQUI ESTÁ A MUDANÇA

        // Encontra os elementos da tela de login pelos IDs
        val editTextLogin = findViewById<EditText>(R.id.editTextLogin)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)

        // Define a ação para o clique do botão "Entrar"
        buttonLogin.setOnClickListener {
            // Pega o texto dos campos de login e senha
            val login = editTextLogin.text.toString()
            val password = editTextPassword.text.toString()

            // Lógica de validação simples
            if (login == "admin" && password == "123") {
                // Se o login for bem-sucedido
                Toast.makeText(this, R.string.login_sucesso, Toast.LENGTH_SHORT).show()
            } else {
                // Se o login falhar
                Toast.makeText(this, R.string.login_falha, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
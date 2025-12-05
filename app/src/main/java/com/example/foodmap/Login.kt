package com.example.foodmap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodmap.network.LoginRequest
import com.example.foodmap.network.LoginResponse
import com.example.foodmap.network.RetrofitClient
import com.example.foodmap.network.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    private lateinit var editTextLogin: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    // CORREÇÃO: Mudamos de Button para TextView para evitar o erro de Cast
    private lateinit var buttonCadastrar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initComponents()
        setupClickListeners()
    }

    private fun initComponents() {
        editTextLogin = findViewById(R.id.editTextLogin)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        // CORREÇÃO: O findViewById agora encontra um TextView sem dar erro
        buttonCadastrar = findViewById(R.id.button2)
    }

    private fun setupClickListeners() {
        // Botão Entrar
        buttonLogin.setOnClickListener {
            if (validarCamposLogin()) {
                fazerLogin()
            }
        }

        // Link Cadastrar (Agora é um TextView clicável)
        buttonCadastrar.setOnClickListener {
            val intent = Intent(this, Cadastro::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validarCamposLogin(): Boolean {
        val username = editTextLogin.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (username.isEmpty()) {
            editTextLogin.error = "Username é obrigatório"
            return false
        }

        if (password.isEmpty()) {
            editTextPassword.error = "Senha é obrigatória"
            return false
        }

        if (password.length < 6) {
            editTextPassword.error = "Senha deve ter pelo menos 6 caracteres"
            return false
        }

        return true
    }

    // --- LÓGICA DE LOGIN ---

    private fun fazerLogin() {
        val username = editTextLogin.text.toString().trim()
        val password = editTextPassword.text.toString()

        val loginRequest = LoginRequest(
            username = username,
            password = password
        )

        buttonLogin.isEnabled = false
        buttonLogin.text = "Entrando..."

        RetrofitClient.instance.loginUsuario(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                buttonLogin.isEnabled = true
                buttonLogin.text = "Entrar"

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val usuario = loginResponse?.usuario

                    if (usuario != null) {
                        salvarDadosUsuario(usuario)
                        irParaTelaPrincipal()
                    } else {
                        Toast.makeText(this@Login, "Erro: Dados do usuário vazios.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        404 -> "Usuário não encontrado"
                        401 -> "Senha incorreta"
                        400 -> "Dados inválidos"
                        else -> "Erro ${response.code()}: ${response.message()}"
                    }
                    Toast.makeText(this@Login, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                buttonLogin.isEnabled = true
                buttonLogin.text = "Entrar"
                Toast.makeText(this@Login, "Falha na conexão: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun salvarDadosUsuario(usuario: Usuario) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("user_id", usuario.id)
            putString("user_name", usuario.name)
            putString("user_username", usuario.username)
            putString("user_email", usuario.email)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    private fun irParaTelaPrincipal() {
        val intent = Intent(this, WeeklyPlanActivity::class.java)
        startActivity(intent)
        finish()
    }
}
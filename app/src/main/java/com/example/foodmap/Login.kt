package com.example.foodmap

// <-- MUDANÇA 1: ADICIONADO O IMPORT PARA A TELA DO SCANNER
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodmap.network.FoodScannerActivity
import com.example.foodmap.network.LoginRequest
import com.example.foodmap.network.LoginResponse
import com.example.foodmap.network.RetrofitClient
import com.example.foodmap.network.UsuarioResponse

class Login : AppCompatActivity() {

    private lateinit var editTextLogin: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonCadastrar: Button

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
        buttonCadastrar = findViewById(R.id.button2)
    }

    private fun setupClickListeners() {
        // Botão Entrar
        buttonLogin.setOnClickListener {
            if (validarCamposLogin()) {
                fazerLogin()
            }
        }

        // Botão Cadastrar
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

    private fun fazerLogin() {
        val username = editTextLogin.text.toString().trim()
        val password = editTextPassword.text.toString()

        val loginRequest = LoginRequest(
            username = username,
            password = password
        )

        // Mostrar loading
        buttonLogin.isEnabled = false
        buttonLogin.text = "Entrando..."

        RetrofitClient.instance.loginUsuario(loginRequest).enqueue(object : retrofit2.Callback<LoginResponse> {
            override fun onResponse(call: retrofit2.Call<LoginResponse>, response: retrofit2.Response<LoginResponse>) {
                buttonLogin.isEnabled = true
                buttonLogin.text = "Entrar"

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    Toast.makeText(this@Login, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()

                    // ✅ Aqui você pode salvar os dados do usuário e redirecionar
                    val usuario = loginResponse?.usuario
                    if (usuario != null) {
                        // Salvar dados do usuário (SharedPreferences, etc.)
                        salvarDadosUsuario(usuario)

                        // Redirecionar para a tela correta
                        redirecionarParaScanner() // Renomeei a função para mais clareza
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

            override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                buttonLogin.isEnabled = true
                buttonLogin.text = "Entrar"
                Toast.makeText(this@Login, "Falha na conexão: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun salvarDadosUsuario(usuario: UsuarioResponse) {
        // ✅ Aqui você pode salvar os dados do usuário logado
        // Exemplo usando SharedPreferences:
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("user_id", usuario.id)
            putString("user_name", usuario.name)
            putString("user_username", usuario.username)
            putString("user_email", usuario.email)
            putBoolean("is_logged_in", true)
            apply()
        }

        println("✅ Usuário logado: ${usuario.name} (${usuario.username})")
    }

    private fun redirecionarParaScanner() { // Função renomeada
        // ✅ Redirecionar para a tela principal
        // <-- MUDANÇA 2: ALTERADO O DESTINO DA NAVEGAÇÃO
        val intent = Intent(this, FoodScannerActivity::class.java)
        startActivity(intent)
        finish() // Fecha a tela de login
    }
}
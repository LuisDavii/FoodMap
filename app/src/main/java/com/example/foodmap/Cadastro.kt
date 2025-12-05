package com.example.foodmap

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView // Importante: Importar TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodmap.network.ApiResponse
import com.example.foodmap.network.RetrofitClient
import com.example.foodmap.network.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Cadastro : AppCompatActivity() {

    private lateinit var editNome: EditText
    private lateinit var editUserName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editSenha: EditText
    private lateinit var editConfirmarSenha: EditText
    private lateinit var btnCriarConta: Button

    // CORREÇÃO: Mudamos de Button para TextView
    private lateinit var btnJaPossuiConta: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initComponents()
        setupClickListeners()
    }

    private fun initComponents() {
        editNome = findViewById(R.id.editNome)
        editUserName = findViewById(R.id.editUserName)
        editEmail = findViewById(R.id.editEmail)
        editSenha = findViewById(R.id.editSenha)
        editConfirmarSenha = findViewById(R.id.editConfirmarSenha)
        btnCriarConta = findViewById(R.id.btnCriarConta)
        // CORREÇÃO: findViewById agora busca o TextView corretamente
        btnJaPossuiConta = findViewById(R.id.btnJaPossuiConta)
    }

    private fun setupClickListeners() {
        btnCriarConta.setOnClickListener {
            if (validarCampos()) {
                cadastrarUsuario()
            }
        }
        // Continua clicável mesmo sendo TextView
        btnJaPossuiConta.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun validarCampos(): Boolean {
        val nome = editNome.text.toString().trim()
        val userName = editUserName.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val senha = editSenha.text.toString()
        val confirmarSenha = editConfirmarSenha.text.toString()

        if (nome.isEmpty()) {
            editNome.error = "Nome é obrigatório"
            return false
        }

        if (userName.isEmpty()) {
            editUserName.error = "Username é obrigatório"
            return false
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.error = "Email inválido"
            return false
        }

        if (senha.length < 6) {
            editSenha.error = "Senha deve ter pelo menos 6 caracteres"
            return false
        }

        if (senha != confirmarSenha) {
            editConfirmarSenha.error = "As senhas não coincidem"
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun cadastrarUsuario() {
        val usuario = Usuario(
            name = editNome.text.toString().trim(),
            username = editUserName.text.toString().trim(),
            email = editEmail.text.toString().trim(),
            password = editSenha.text.toString()
        )

        btnCriarConta.isEnabled = false
        btnCriarConta.text = "Cadastrando..."

        RetrofitClient.instance.cadastrarUsuario(usuario).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                btnCriarConta.isEnabled = true
                btnCriarConta.text = "Criar Conta"

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    Toast.makeText(
                        this@Cadastro,
                        apiResponse?.message ?: "Usuário cadastrado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Dados inválidos. Verifique as informações."
                        409 -> "Usuário ou email já cadastrado."
                        else -> "Erro ${response.code()}: ${response.message()}"
                    }
                    Toast.makeText(this@Cadastro, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                btnCriarConta.isEnabled = true
                btnCriarConta.text = "Criar Conta"

                Toast.makeText(
                    this@Cadastro,
                    "Falha na conexão: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}
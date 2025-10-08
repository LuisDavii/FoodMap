package com.example.foodmap.network

import com.example.foodmap.models.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/usuarios/")
    fun cadastrarUsuario(@Body usuario: Usuario): Call<ApiResponse>

    // ✅ Novo endpoint de login
    @POST("api/login/")
    fun loginUsuario(@Body loginRequest: LoginRequest): Call<LoginResponse>
}

// ✅ Request para login
data class LoginRequest(
    val username: String,
    val password: String
)

// ✅ Response para login
data class LoginResponse(
    val message: String,
    val usuario: UsuarioResponse? = null
)

// ✅ Dados do usuário retornados no login
data class UsuarioResponse(
    val id: Int,
    val username: String,
    val name: String,
    val email: String
)

// ✅ Response para cadastro (já existente)
data class ApiResponse(
    val message: String,
    val id: Int? = null,
    val username: String? = null
)
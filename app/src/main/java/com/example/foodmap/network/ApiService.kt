package com.example.foodmap.network

import com.example.foodmap.models.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/usuarios/")
    fun cadastrarUsuario(@Body usuario: Usuario): Call<ApiResponse>
}

// Classe para tratar a resposta da API
data class ApiResponse(
    val message: String,
    val id: Int? = null,
    val userName: String? = null
)
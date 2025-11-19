package com.example.foodmap.models

import com.google.gson.annotations.SerializedName
import com.example.foodmap.network.Usuario
// Request de Login
data class LoginRequest(
    @SerializedName("userName") // ou "username", conforme sua API
    val username: String,

    @SerializedName("password")
    val password: String
)

// Response de Login
data class LoginResponse(
    val success: Boolean?,
    val message: String?,

    @SerializedName("usuario", alternate = ["user", "data"])
    val usuario: Usuario?
)

// Response Genérico (usado no cadastro)
data class ApiResponse(
    val message: String,
    val id: Int? = null,
    val username: String? = null
)
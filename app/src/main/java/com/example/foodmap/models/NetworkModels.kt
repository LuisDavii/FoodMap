package com.example.foodmap.models

import com.example.foodmap.network.Usuario
import com.google.gson.annotations.SerializedName

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

data class RefeicaoRequest(
    val dia_semana: String,
    val tipo_refeicao: String,
    val descricao: String,
    val calorias: Int,
    val usuario_id: Int, // Importante para vincular ao usuário
    val concluido: Boolean = false // Padrão como não concluído
)

data class RefeicaoResponse(
    val id: Int,
    val dia_semana: String,
    val tipo_refeicao: String,
    val descricao: String,
    val calorias: Int,
    val concluido: Boolean
)

data class StatusRequest(
    val concluido: Boolean
)

data class ApiResponse(
    val message: String,
    val id: Int? = null,
    val username: String? = null
)
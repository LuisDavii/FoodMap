package com.example.foodmap.network

import com.google.gson.annotations.SerializedName

data class Usuario(
    // O Login.kt precisa do ID para salvar as preferências
    @SerializedName("id")
    val id: Int = 0,

    // O Login.kt usa 'name' para mostrar "Bem-vindo, [Nome]"
    @SerializedName("name")
    val name: String,

    // O Login.kt usa 'username' (tudo minúsculo)
    @SerializedName("username")
    val username: String,

    // O Login.kt usa 'email'
    @SerializedName("email")
    val email: String,

    // A password é opcional na resposta, mas deixamos aqui caso a API devolva
    @SerializedName("password")
    val password: String? = null
)
package com.example.foodmap.network

import com.google.gson.annotations.SerializedName

data class Usuario(

    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String? = null
)
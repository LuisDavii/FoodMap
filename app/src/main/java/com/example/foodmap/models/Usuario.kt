package com.example.foodmap.models

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("userName")
    val userName: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)
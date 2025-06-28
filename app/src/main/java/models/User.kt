package com.example.libraryapp.models

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int = 0,
    val username: String,
    val password: String?,
    val email: String,
    val phone: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
package com.example.libraryapp.models

import com.google.gson.annotations.SerializedName

data class Book(
    val id: Int = 0,
    val title: String,
    val author: String,
    @SerializedName("publication_year") val publication_year: Int,
    val isbn: String,
    val available: Boolean = true,
    @SerializedName("image_url") val image_url: String? = null,
    val description: String? = null
)
package com.example.libraryapp.models

import com.google.gson.annotations.SerializedName

data class Loan(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("book_id") val bookId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("issue_date") val issueDate: String,
    @SerializedName("return_date") val returnDate: String? = null
)
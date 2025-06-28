package com.example.libraryapp.api

import com.example.libraryapp.models.Book
import com.example.libraryapp.models.Loan
import com.example.libraryapp.models.User
import com.example.libraryapp.models.Token
import retrofit2.http.*

interface ApiService {
    @GET("books")
    suspend fun getBooks(): List<Book>

    @GET("books/{id}")
    suspend fun getBookById(@Path("id") bookId: Int): Book

    @POST("books")
    suspend fun addBook(@Body book: Book): Book

    @PUT("books/{id}")
    suspend fun updateBook(@Path("id") bookId: Int, @Body book: Book): Book

    @DELETE("books/{id}")
    suspend fun deleteBook(@Path("id") bookId: Int): Unit

    @GET("users")
    suspend fun getUsers(): List<User>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Int): User

    @POST("users")
    suspend fun addUser(@Body user: User): User

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") userId: Int, @Body user: User): User

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") userId: Int): Unit

    @GET("loans")
    suspend fun getLoans(): List<Loan>

    @POST("loans")
    suspend fun addLoan(@Body loan: Loan): Loan

    @PUT("loans/{id}")
    suspend fun updateLoan(@Path("id") loanId: Int, @Body loan: Loan): Loan

    @DELETE("loans/{loan_id}")
    suspend fun returnBook(@Path("loan_id") loanId: Int): Map<String, String>

    @POST("login")
    suspend fun login(@Body user: User): Token

    @POST("register")
    suspend fun register(@Body user: User): Token
}
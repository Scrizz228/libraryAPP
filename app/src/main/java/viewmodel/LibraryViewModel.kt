package com.example.libraryapp.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.api.ApiService
import com.example.libraryapp.api.RetrofitClient
import com.example.libraryapp.models.Book
import com.example.libraryapp.models.Loan
import com.example.libraryapp.models.User
import com.example.libraryapp.models.Token
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import retrofit2.HttpException

class LibraryViewModel(private val context: Context) : ViewModel() {
    private val apiService: ApiService = RetrofitClient.instance
    private val prefs: SharedPreferences = context.getSharedPreferences("library_prefs", Context.MODE_PRIVATE)

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _loans = MutableStateFlow<List<Loan>>(emptyList())
    val loans: StateFlow<List<Loan>> = _loans.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    init {
        loadCachedData()
        viewModelScope.launch {
            try {
                refreshData()
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error initializing data: ${e.message}")
                _errorState.value = "Ошибка загрузки данных: ${e.message}"
            }
        }
    }

    private fun loadCachedData() {
        viewModelScope.launch {
            _books.value = prefs.getString("cached_books", null)?.let { Gson().fromJson(it, object : TypeToken<List<Book>>(){}.type) } ?: emptyList()
            _users.value = prefs.getString("cached_users", null)?.let { Gson().fromJson(it, object : TypeToken<List<User>>(){}.type) } ?: emptyList()
            _loans.value = prefs.getString("cached_loans", null)?.let { Gson().fromJson(it, object : TypeToken<List<Loan>>(){}.type) } ?: emptyList()
            Log.d("LibraryViewModel", "Loaded cached users: ${_users.value.size}")
            _currentUser.value = prefs.getString("current_user", null)?.let { Gson().fromJson(it, User::class.java) }
        }
    }

    private fun saveCachedData() {
        prefs.edit().apply {
            putString("cached_books", Gson().toJson(_books.value))
            putString("cached_users", Gson().toJson(_users.value))
            putString("cached_loans", Gson().toJson(_loans.value))
            putString("current_user", Gson().toJson(_currentUser.value))
            apply()
        }
    }

    fun clearError() {
        _errorState.value = null
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            try {
                val newUser = apiService.addUser(user)
                _users.value = _users.value + newUser
                saveCachedData()
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error adding user: ${e.message}")
                _errorState.value = "Ошибка добавления пользователя: ${e.message}"
                Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun addBook(book: Book) {
        viewModelScope.launch {
            try {
                val newBook = apiService.addBook(book)
                _books.value = _books.value + newBook
                saveCachedData()
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error adding book: ${e.message}")
                _errorState.value = "Ошибка добавления книги: ${e.message}"
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                getBooks()
                getUsers()
                getLoans()
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error refreshing data: ${e.message}")
                _errorState.value = "Ошибка обновления данных: ${e.message}"
            }
        }
    }

    fun getBooks() {
        viewModelScope.launch {
            Log.d("LibraryViewModel", "Starting to fetch books from ${RetrofitClient.BASE_URL}books")
            try {
                val bookList = apiService.getBooks()
                Log.d("LibraryViewModel", "Fetched books from API: $bookList")
                if (bookList.isEmpty()) {
                    Log.w("LibraryViewModel", "API returned empty list, using test data")
                    _books.value = listOf(
                        Book(
                            id = 1,
                            title = "The Great Gatsby",
                            author = "F. Scott Fitzgerald",
                            publication_year = 1925,
                            isbn = "978-0743273565",
                            available = true,
                            image_url = "https://images.unsplash.com/photo-1544947950-fa07a98d237f",
                            description = "Роман о американской мечте и трагедии."
                        ),
                        Book(
                            id = 2,
                            title = "1984",
                            author = "George Orwell",
                            publication_year = 1949,
                            isbn = "978-0451524935",
                            available = true,
                            image_url = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c",
                            description = "Антиутопический роман о тоталитаризме."
                        )
                    )
                } else {
                    _books.value = bookList
                    bookList.forEach { book ->
                        Log.d("LibraryViewModel", "Book ${book.title}: image_url = ${book.image_url}")
                    }
                }
                saveCachedData()
            } catch (e: HttpException) {
                Log.e("LibraryViewModel", "HTTP Error fetching books: ${e.code()} - ${e.message()}")
                _errorState.value = "Ошибка загрузки книг: HTTP ${e.code()} - ${e.message()}"
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error fetching books: ${e.message}")
                _errorState.value = "Ошибка загрузки книг: ${e.message}"
                _books.value = prefs.getString("cached_books", null)?.let { Gson().fromJson(it, object : TypeToken<List<Book>>(){}.type) } ?: emptyList()
                if (_books.value.isEmpty()) {
                    Log.w("LibraryViewModel", "Cache is empty, using test data")
                    _books.value = listOf(
                        Book(
                            id = 1,
                            title = "The Great Gatsby",
                            author = "F. Scott Fitzgerald",
                            publication_year = 1925,
                            isbn = "978-0743273565",
                            available = true,
                            image_url = "https://images.unsplash.com/photo-1544947950-fa07a98d237f",
                            description = "Роман о американской мечте и трагедии."
                        ),
                        Book(
                            id = 2,
                            title = "1984",
                            author = "George Orwell",
                            publication_year = 1949,
                            isbn = "978-0451524935",
                            available = true,
                            image_url = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c",
                            description = "Антиутопический роман о тоталитаризме."
                        )
                    )
                }
                saveCachedData()
            }
        }
    }

    fun getUsers() {
        viewModelScope.launch {
            Log.d("LibraryViewModel", "Starting to fetch users")
            try {
                val userList = apiService.getUsers()
                Log.d("LibraryViewModel", "Fetched users from API: $userList")
                if (userList.isEmpty()) {
                    Log.w("LibraryViewModel", "API returned empty list, using test data")
                    _users.value = listOf(
                        User(
                            id = 1,
                            username = "john_doe",
                            password = "password123",
                            email = "john@example.com",
                            phone = "123-456-7890"
                        ),
                        User(
                            id = 2,
                            username = "jane_smith",
                            password = "password456",
                            email = "jane@example.com",
                            phone = "987-654-3210"
                        )
                    )
                } else {
                    _users.value = userList
                    userList.forEach { user ->
                        Log.d("LibraryViewModel", "User ${user.username}: email = ${user.email}")
                    }
                }
                saveCachedData()
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error fetching users: ${e.message}")
                _errorState.value = "Ошибка загрузки пользователей: ${e.message}"
                _users.value = prefs.getString("cached_users", null)?.let { Gson().fromJson(it, object : TypeToken<List<User>>(){}.type) } ?: emptyList()
                Log.d("LibraryViewModel", "Fallback users from cache: ${_users.value.size}")
                if (_users.value.isEmpty()) {
                    Log.w("LibraryViewModel", "Cache is empty, using test data")
                    _users.value = listOf(
                        User(
                            id = 1,
                            username = "john_doe",
                            password = "password123",
                            email = "john@example.com",
                            phone = "123-456-7890"
                        ),
                        User(
                            id = 2,
                            username = "jane_smith",
                            password = "password456",
                            email = "jane@example.com",
                            phone = "987-654-3210"
                        )
                    )
                }
                saveCachedData()
            }
        }
    }

    fun getLoans() {
        viewModelScope.launch {
            try {
                val loanList = apiService.getLoans()
                _loans.value = loanList
                saveCachedData()
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error fetching loans: ${e.message}")
                _errorState.value = "Ошибка загрузки выдач: ${e.message}"
            }
        }
    }

    fun issueBook(loan: Loan) {
        viewModelScope.launch {
            try {
                val book = _books.value.find { it.id == loan.bookId }
                if (book == null || !book.available) {
                    _errorState.value = "Нет в наличии"
                    Toast.makeText(context, "Нет в наличии", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val currentDate = LocalDate.now()
                val newLoan = Loan(
                    bookId = loan.bookId,
                    userId = loan.userId,
                    issueDate = currentDate.toString(),
                    returnDate = null
                )
                val response = apiService.addLoan(newLoan)
                _loans.value = _loans.value + response

                val updatedBooks = _books.value.map {
                    if (it.id == loan.bookId) it.copy(available = false) else it
                }
                _books.value = updatedBooks
                saveCachedData()
                Toast.makeText(context, "Книга успешно выдана", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Ошибка выдачи книги: ${e.message}")
                _errorState.value = "Ошибка выдачи книги: ${e.message}"
                Toast.makeText(context, "Ошибка выдачи книги: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun returnBook(loanId: Int) {
        viewModelScope.launch {
            try {
                val loanToUpdate = _loans.value.find { it.id == loanId }
                if (loanToUpdate != null) {
                    val response = apiService.returnBook(loanId)
                    Log.d("LibraryViewModel", "Return book response: $response")
                    val updatedLoans = _loans.value.map {
                        if (it.id == loanId) it.copy(returnDate = LocalDate.now().toString()) else it
                    }
                    _loans.value = updatedLoans

                    val loanBookId = loanToUpdate.bookId
                    val updatedBooks = _books.value.map {
                        if (it.id == loanBookId) it.copy(available = true) else it
                    }
                    _books.value = updatedBooks
                }
                getLoans()
                Toast.makeText(context, "Книга успешно возвращена", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error returning book: ${e.message}")
                _errorState.value = "Ошибка возврата книги: ${e.message}"
                Toast.makeText(context, "Ошибка возврата книги: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun register(user: User) {
        viewModelScope.launch {
            try {
                val response = apiService.register(user)
                prefs.edit().putString("auth_token", response.token).apply()
                Toast.makeText(context, "Регистрация успешна", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Registration failed with error: ${e.message}")
                _errorState.value = "Ошибка регистрации: ${e.message}"
                Toast.makeText(context, "Ошибка регистрации: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val user = User(username = username, password = password, email = "", phone = null)
                val token = apiService.login(user)
                prefs.edit().putString("auth_token", token.token).apply()
                val userList = apiService.getUsers()
                val loggedInUser = userList.find { it.username == username }
                if (loggedInUser != null) {
                    _currentUser.value = loggedInUser
                    saveCachedData()
                    _loginState.value = LoginState.Success
                    Log.d("LibraryViewModel", "Logged in user: ${loggedInUser.username}")
                } else {
                    throw Exception("Пользователь не найден после логина")
                }
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Ошибка при входе в систему: ${e.message}")
                val errorMessage = if (e.message?.contains("Invalid credentials") == true) {
                    "Неверные учетные данные"
                } else {
                    "Ошибка входа: ${e.message}"
                }
                _loginState.value = LoginState.Error(errorMessage)
                _errorState.value = errorMessage
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun updateUserEmail(newEmail: String) {
        viewModelScope.launch {
            try {
                val current = _currentUser.value ?: throw Exception("Пользователь не вошёл в систему")
                if (current.id == null) throw Exception("User ID is null")
                val updatedUser = current.copy(email = newEmail)
                Log.d("LibraryViewModel", "Request URL: ${RetrofitClient.BASE_URL}users/${current.id}")
                Log.d("LibraryViewModel", "Updating user with: $updatedUser")
                try {
                    apiService.updateUser(current.id, updatedUser)
                } catch (e: HttpException) {
                    Log.e("LibraryViewModel", "Ошибка: ${e.code()} - ${e.message()}")
                    throw e
                }
                _currentUser.value = updatedUser
                _users.value = _users.value.map { if (it.id == current.id) updatedUser else it }
                saveCachedData()
                Toast.makeText(context, "Email успешно обновлён", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error updating email: ${e.message}")
                _errorState.value = "Ошибка обновления email: ${e.message}"
                Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun logout() {
        prefs.edit().apply {
            remove("auth_token")
            remove("current_user")
            apply()
        }
        _currentUser.value = null
        _loginState.value = LoginState.Idle
        Log.d("LibraryViewModel", "User logged out")
    }

    fun isAuthenticated(): Boolean {
        return prefs.getString("auth_token", null) != null && _currentUser.value != null
    }

    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return sdf.format(Date())
    }
}
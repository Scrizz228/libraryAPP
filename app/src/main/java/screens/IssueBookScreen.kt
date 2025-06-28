package com.example.libraryapp.screens

import android.widget.Toast // Добавлен импорт для Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.libraryapp.models.Book
import com.example.libraryapp.models.Loan
import com.example.libraryapp.models.User
import com.example.libraryapp.viewmodel.LibraryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueBookScreen(navController: NavController, viewModel: LibraryViewModel, bookId: Int, userId: Int) {
    val books by viewModel.books.collectAsState()
    val users by viewModel.users.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var issueDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var returnDate by remember { mutableStateOf(LocalDate.now().plusDays(14).format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var expandedUser by remember { mutableStateOf(false) }
    var expandedBook by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(key1 = "fetch_data_issue_book") {
        viewModel.getBooks()
        viewModel.getUsers()
    }

    // Отображаем ошибки, если они есть
    LaunchedEffect(errorState) {
        errorState?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issue a Book") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedUser,
                        onExpandedChange = { expandedUser = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedUser?.username ?: "",
                            onValueChange = {},
                            label = { Text("Select User") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUser) }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedUser,
                            onDismissRequest = { expandedUser = false }
                        ) {
                            users.forEach { user ->
                                DropdownMenuItem(
                                    text = { Text(user.username) },
                                    onClick = {
                                        selectedUser = user
                                        expandedUser = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedBook,
                        onExpandedChange = { expandedBook = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedBook?.title ?: "",
                            onValueChange = {},
                            label = { Text("Select Book") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBook) }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedBook,
                            onDismissRequest = { expandedBook = false }
                        ) {
                            books.filter { it.available }.forEach { book ->
                                DropdownMenuItem(
                                    text = { Text(book.title) },
                                    onClick = {
                                        selectedBook = book
                                        expandedBook = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = issueDate,
                        onValueChange = { if (isValidDate(it)) issueDate = it },
                        label = { Text("Issue Date") },
                        leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = returnDate,
                        onValueChange = {
                            if (isValidDate(it)) {
                                val parsedReturnDate = LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                                val parsedIssueDate = LocalDate.parse(issueDate, DateTimeFormatter.ISO_LOCAL_DATE)
                                if (parsedReturnDate.isAfter(parsedIssueDate)) {
                                    returnDate = it
                                } else {
                                    Toast.makeText(context, "Return date must be after issue date", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        label = { Text("Return Date") },
                        leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (selectedUser != null && selectedBook != null && isValidDate(issueDate) && isValidDate(returnDate)) {
                                isLoading = true
                                val loan = Loan(
                                    bookId = selectedBook!!.id,
                                    userId = selectedUser!!.id,
                                    issueDate = issueDate,
                                    returnDate = returnDate
                                )
                                viewModel.issueBook(loan)
                                isLoading = false
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = !isLoading
                    ) {
                        Text("Issue Book")
                    }
                }
            }
        }
    }
}

private fun isValidDate(dateStr: String): Boolean {
    return try {
        LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        true
    } catch (e: Exception) {
        false
    }
}
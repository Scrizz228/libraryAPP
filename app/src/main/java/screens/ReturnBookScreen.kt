package com.example.libraryapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.libraryapp.models.Book
import com.example.libraryapp.models.Loan
import com.example.libraryapp.viewmodel.LibraryViewModel
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnBookScreen(navController: NavController, viewModel: LibraryViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val loans by viewModel.loans.collectAsState()
    var selectedLoan by remember { mutableStateOf<Loan?>(null) }

    // Фильтруем активные займы текущего пользователя (где return_date отсутствует)
    val userLoans = loans.filter { it.userId == currentUser?.id && it.returnDate == null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Возврат книги") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentUser == null) {
                Text("Пожалуйста, войдите в систему, чтобы вернуть книгу", style = MaterialTheme.typography.bodyLarge)
            } else if (userLoans.isEmpty()) {
                Text("Нет взятых книг для ${currentUser?.username}", style = MaterialTheme.typography.bodyLarge)
            } else {
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedLoan?.let { "${viewModel.books.value.find { book -> book.id == it.bookId }?.title ?: "Unknown"}" } ?: "",
                        onValueChange = {},
                        label = { Text("Выберите книгу для возврата") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        userLoans.forEach { loan ->
                            val book = viewModel.books.value.find { it.id == loan.bookId }
                            DropdownMenuItem(
                                text = { Text("${book?.title ?: "Unknown"} by ${book?.author ?: "Unknown"}") },
                                onClick = {
                                    selectedLoan = loan
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        selectedLoan?.let { loan ->
                            viewModel.returnBook(loan.id)
                            navController.popBackStack()
                            Toast.makeText(navController.context, "Книга успешно возвращена", Toast.LENGTH_SHORT).show()
                        } ?: Toast.makeText(navController.context, "Пожалуйста, выберите книгу для возврата", Toast.LENGTH_SHORT).show()
                    },
                    enabled = selectedLoan != null,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Вернуть книгу")
                }
            }
        }
    }
}
package com.example.libraryapp.screens

import android.widget.Toast // Добавлен импорт для Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.libraryapp.models.Book
import com.example.libraryapp.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksListScreen(navController: NavController, viewModel: LibraryViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val books by viewModel.books.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Используем ключ для LaunchedEffect, чтобы избежать повторных вызовов
    LaunchedEffect(key1 = "fetch_books") {
        scope.launch {
            try {
                viewModel.getBooks()
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
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
                title = { Text("Список книг") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                label = { Text("Поиск книг...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        // Фильтрация уже выполняется через filteredBooks
                    }
                )
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filteredBooks = books.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.author.contains(searchQuery, ignoreCase = true)
                    }
                    if (filteredBooks.isEmpty()) {
                        item {
                            Text(
                                text = if (searchQuery.isEmpty()) "Нет доступных книг" else "Книги не найдены",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(filteredBooks) { book ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("book_detail/${book.id}") },
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        text = "by ${book.author}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Год: ${book.publication_year}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (!book.available) {
                                        Text(
                                            text = "Недоступно",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
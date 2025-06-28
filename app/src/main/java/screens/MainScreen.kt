package com.example.libraryapp.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.libraryapp.models.Book
import com.example.libraryapp.models.Loan
import com.example.libraryapp.viewmodel.LibraryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: LibraryViewModel) {
    var username by remember { mutableStateOf("") }
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    val books by viewModel.books.collectAsState()
    val users by viewModel.users.collectAsState()
    val loans by viewModel.loans.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return sdf.format(Date())
    }

    LaunchedEffect(key1 = "fetch_data_main_screen") {
        scope.launch {
            try {
                viewModel.getBooks()
                viewModel.getUsers()
                viewModel.getLoans()
                isLoading = false
                Log.d("MainScreen", "Books loaded: ${books.size}") // Отладка
            } catch (e: Exception) {
                isLoading = false
                Log.e("MainScreen", "Ошибка при загрузке данных", e)
            }
        }
    }

    LaunchedEffect(errorState) {
        errorState?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* Оставляем пустым */ },
                navigationIcon = { /* Убрана стрелка */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 0.dp) // Убран вертикальный отступ
                .verticalScroll(scrollState)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 0.dp, end = 0.dp), // Убраны все вертикальные отступы
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) { // Уменьшен внутренний padding до 8.dp
                        Text(
                            text = "Панель Библиотеки",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp)) // Уменьшен Spacer до 8.dp

                        // Popular Books Section
                        Text(
                            text = "Популярные книги",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp)) // Уменьшен Spacer до 4.dp

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(books.take(5)) { book ->
                                Card(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(150.dp)
                                        .padding(vertical = 2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { navController.navigate("book_detail/${book.id}") },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Book Cover Image
                                        if (!book.image_url.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = book.image_url,
                                                contentDescription = book.title,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(60.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(60.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Filled.LocalLibrary,
                                                    contentDescription = "No cover",
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }

                                        // Book Details
                                        Column {
                                            Text(
                                                text = book.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "by ${book.author}",
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp)) // Уменьшен Spacer до 8.dp

                        // Statistics Section
                        Text(
                            text = "Статистика",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp)) // Уменьшен Spacer до 4.dp
                        Text(
                            text = "Количество взятых книг: ${loans.size}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp)) // Уменьшен Spacer до 8.dp

                        // Issue Book Section
                        Text(
                            text = "Аренда книг",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp)) // Уменьшен Spacer до 4.dp
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Имя пользователя") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        )
                        Spacer(modifier = Modifier.height(4.dp)) // Уменьшен Spacer до 4.dp
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedBook?.title ?: "",
                                onValueChange = {},
                                label = { Text("Выбрать книгу") },
                                leadingIcon = { Icon(Icons.Filled.LocalLibrary, contentDescription = null) },
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
                                books.forEach { book ->
                                    DropdownMenuItem(
                                        text = { Text("${book.title} by ${book.author}") },
                                        onClick = {
                                            selectedBook = book
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp)) // Уменьшен Spacer до 8.dp
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { navController.navigate("return_book") },
                                modifier = Modifier
                            ) {
                                Text("Возврат книги")
                            }
                            Button(
                                onClick = {
                                    val user = users.find { it.username == username }
                                    if (user == null) {
                                        Toast.makeText(context, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                                    } else if (selectedBook == null) {
                                        Toast.makeText(context, "Пожалуйста, выберите книгу", Toast.LENGTH_SHORT).show()
                                    } else if (!selectedBook!!.available) {
                                        Toast.makeText(context, "Нет в наличии", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val loan = Loan(
                                            bookId = selectedBook!!.id,
                                            userId = user.id,
                                            issueDate = getCurrentDateString(),
                                            returnDate = getCurrentDateString()
                                        )
                                        viewModel.issueBook(loan)
                                        Toast.makeText(context, "Книга успешна выдана", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = username.isNotBlank() && selectedBook != null && !isLoading,
                                modifier = Modifier
                            ) {
                                Text("Взять книгу")
                            }
                        }
                    }
                }
            }
        }
    }
}
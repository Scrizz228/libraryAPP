package com.example.libraryapp.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.example.libraryapp.models.Book
import com.example.libraryapp.viewmodel.LibraryViewModel
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    navController: NavController,
    viewModel: LibraryViewModel,
    bookId: Int
) {
    var isLoading by remember { mutableStateOf(true) }
    val books by viewModel.books.collectAsState()
    val loans by viewModel.loans.collectAsState()

    // Поиск книги по ID
    val book by remember(books) { derivedStateOf { books.find { it.id == bookId } } }
    val isBookTaken = remember(loans) {
        loans.any { it.bookId == bookId && it.returnDate == null }
    }

    LaunchedEffect(Unit) {
        if (books.isEmpty()) {
            Log.d("BookDetailScreen", "Books list is empty, fetching books...")
            viewModel.getBooks()
        } else {
            Log.d("BookDetailScreen", "Books list: $books")
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали книги") },
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
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            book == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Книга не найдена")
                }
            }
            else -> {
                book?.let { currentBook ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        val imageUrl = currentBook.image_url
                        Log.d("BookDetailScreen", "Image URL for book ${currentBook.title}: $imageUrl")

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUrl.isNullOrBlank()) {
                                Text(
                                    text = "No URL",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            } else {
                                // Используем AsyncImagePainter для управления состояниями
                                val painter = rememberAsyncImagePainter(model = imageUrl)
                                val painterState by painter.state.collectAsState()

                                // Отображаем изображение
                                Image(
                                    painter = painter,
                                    contentDescription = "Book cover for ${currentBook.title}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Управляем состояниями вручную
                                when (painterState) {
                                    is AsyncImagePainter.State.Loading -> {
                                        Log.d("BookDetailScreen", "Image loading: $imageUrl")
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                    is AsyncImagePainter.State.Error -> {
                                        Log.e("BookDetailScreen", "Image load error for $imageUrl: ${(painterState as AsyncImagePainter.State.Error).result.throwable.message}")
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalLibrary,
                                                contentDescription = "No cover available",
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                                            )
                                        }
                                    }
                                    is AsyncImagePainter.State.Success -> {
                                        Log.d("ImageLoad", "Success: $imageUrl")
                                    }
                                    else -> {
                                        Log.d("BookDetailScreen", "Image state: $painterState")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = currentBook.title,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "by ${currentBook.author}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentBook.description ?: "No description available.",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 10,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Год публикации: ${currentBook.publication_year}")
                        Text("ISBN: ${currentBook.isbn}")
                        Text("Доступность: ${if (isBookTaken) "Нет" else if (currentBook.available) "Да" else "Нет"}")
                    }
                }
            }
        }
    }
}
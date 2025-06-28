package com.example.libraryapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.libraryapp.models.Book
import com.example.libraryapp.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookScreen(navController: NavController, viewModel: LibraryViewModel) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var publicationYear by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var available by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add/Edit Book") },
                actions = {
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
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") }
            )
            TextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Author") }
            )
            TextField(
                value = publicationYear,
                onValueChange = { publicationYear = it },
                label = { Text("Publication Year") }
            )
            TextField(
                value = isbn,
                onValueChange = { isbn = it },
                label = { Text("ISBN") }
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Available")
                Switch(
                    checked = available,
                    onCheckedChange = { available = it }
                )
            }

            Button(onClick = {
                val book = Book(
                    title = title,
                    author = author,
                    publication_year = publicationYear.toIntOrNull() ?: 0,
                    isbn = isbn,
                    id = 0
                )
                if (title.isNotBlank() && author.isNotBlank() && isbn.isNotBlank() && publicationYear.toIntOrNull() != null) {
                    viewModel.addBook(book)
                    navController.popBackStack()
                } else {
                    println("Invalid book data: title=$title, author=$author, isbn=$isbn, publicationYear=$publicationYear")
                }
            }) {
                Text("Save")
            }
        }
    }
}
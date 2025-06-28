package com.example.libraryapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.libraryapp.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(navController: NavController, viewModel: LibraryViewModel, userId: Int) {
    // Получаем список пользователей из StateFlow, подписываемся на обновления
    val users by viewModel.users.collectAsState(initial = emptyList())

    // Находим нужного пользователя по userId
    val user = users.find { it.id == userId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Details") },
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
                .padding(paddingValues)
        ) {
            user?.let {
                Text(text = it.username, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Email: ${it.email}")
            } ?: Text("User not found")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("users") }) {
                Text("Back")
            }
        }
    }
}
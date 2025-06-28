package com.example.libraryapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.libraryapp.viewmodel.LibraryViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: LibraryViewModel) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Слушаем состояние авторизации
    LaunchedEffect(Unit) {
        viewModel.loginState.collectLatest { state ->
            isLoading = false // Сбрасываем флаг загрузки перед обработкой
            when (state) {
                is LibraryViewModel.LoginState.Success -> {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                is LibraryViewModel.LoginState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                is LibraryViewModel.LoginState.Idle -> {
                    // Ничего не делаем
                }
                is LibraryViewModel.LoginState.Loading -> {
                    isLoading = true // Устанавливаем флаг загрузки
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
        Text(
            text = "Авторизация",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Поле ввода имени пользователя
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Имя пользователя") },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(bottom = 8.dp),
            enabled = !isLoading
        )

        // Поле ввода пароля
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(bottom = 16.dp),
            enabled = !isLoading
        )

        // Кнопка входа или индикатор загрузки
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        } else {
            Button(
                onClick = {
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        viewModel.login(username, password)
                    } else {
                        Toast.makeText(context, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(50.dp)
            ) {
                Text("Вход", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Кнопка регистрации
        TextButton(
            onClick = {
                if (!isLoading) {
                    navController.navigate("register")
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Нет аккаунта? Зарегистрируйтесь", color = MaterialTheme.colorScheme.primary)
        }
    }
}
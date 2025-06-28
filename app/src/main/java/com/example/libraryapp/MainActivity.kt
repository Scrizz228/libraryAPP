package com.example.libraryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.libraryapp.screens.*
import com.example.libraryapp.ui.theme.LibraryAPPTheme
import com.example.libraryapp.viewmodel.LibraryViewModel
import androidx.compose.material3.ExperimentalMaterial3Api // Добавлен импорт

@OptIn(ExperimentalMaterial3Api::class) // Добавлена аннотация для экспериментального API
class MainActivity : ComponentActivity() {
    private val viewModel by lazy { LibraryViewModel(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var darkTheme by remember { mutableStateOf(false) }

            LibraryAPPTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute in listOf("main", "books_list", "users_list", "settings")
                val showTopBar = currentRoute?.startsWith("issue_book") == true ||
                        currentRoute in listOf("book_detail", "user_detail", "add_edit_book", "add_edit_user", "loans", "profile", "return_book", "search", "stats")

                Scaffold(
                    topBar = {
                        if (showTopBar) {
                            TopAppBar(
                                title = {
                                    when (currentRoute) {
                                        "return_book" -> Text("Return or Issue a Book")
                                        "issue_book/{bookId}/{userId}" -> Text("Issue a Book")
                                        "books_list" -> Text("Book List")
                                        "users_list" -> Text("Users List")
                                        "settings" -> Text("Settings")
                                        else -> Text(currentRoute?.replaceFirstChar { it.uppercase() } ?: "")
                                    }
                                },
                                navigationIcon = {
                                    if (currentRoute != "main" && currentRoute != null) {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                                        }
                                    }
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController, currentRoute)
                        }
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "login",
                            enterTransition = {
                                fadeIn(animationSpec = tween(300)) +
                                        slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(300))
                            },
                            exitTransition = {
                                fadeOut(animationSpec = tween(300)) +
                                        slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(300))
                            },
                            popEnterTransition = {
                                fadeIn(animationSpec = tween(300)) +
                                        slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(300))
                            },
                            popExitTransition = {
                                fadeOut(animationSpec = tween(300)) +
                                        slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(300))
                            }
                        ) {
                            composable("login") { LoginScreen(navController, viewModel) }
                            composable("register") { RegisterScreen(navController, viewModel) }
                            composable("books_list") { BooksListScreen(navController, viewModel) }
                            composable(
                                route = "book_detail/{bookId}",
                                arguments = listOf(navArgument("bookId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val bookId = backStackEntry.arguments?.getInt("bookId") ?: 0
                                BookDetailScreen(navController, viewModel, bookId) // Активируем BookDetailScreen
                            }
                            composable("main") { MainScreen(navController, viewModel) }
                            composable(
                                route = "issue_book/{bookId}/{userId}",
                                arguments = listOf(
                                    navArgument("bookId") { type = NavType.IntType },
                                    navArgument("userId") { type = NavType.IntType }
                                )
                            ) { backStackEntry ->
                                val bookId = backStackEntry.arguments?.getInt("bookId") ?: 0
                                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                                IssueBookScreen(navController, viewModel, bookId, userId)
                            }
                            composable("add_edit_book") {
                                // AddEditBookScreen(navController, viewModel)
                                Text("AddEditBookScreen Placeholder")
                            }
                            composable("add_edit_user") {
                                // AddEditUserScreen(navController, viewModel)
                                Text("AddEditBookScreen Placeholder")
                            }
                            composable("loans") {
                                // LoansListScreen(navController, viewModel)
                                Text("LoansListScreen Placeholder")
                            }
                            composable("profile") {
                                // ProfileScreen(navController, viewModel)
                                Text("ProfileScreen Placeholder")
                            }
                            composable(
                                "return_book?bookId={bookId}&userId={userId}",
                                arguments = listOf(
                                    navArgument("bookId") { type = NavType.IntType; defaultValue = 0 },
                                    navArgument("userId") { type = NavType.IntType; defaultValue = 0 }
                                )
                            ) { backStackEntry ->
                                val bookId = backStackEntry.arguments?.getInt("bookId") ?: 0
                                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                                ReturnBookScreen(navController, viewModel)
                            }
                            composable("search") {
                                // SearchScreen(navController, viewModel)
                                Text("SearchScreen Placeholder")
                            }
                            composable("settings") {
                                SettingsScreen(navController, viewModel, darkTheme, onThemeChange = { darkTheme = it })
                            }
                            composable("stats") {
                                // StatsScreen(navController, viewModel)
                                Text("StatsScreen Placeholder")
                            }
                            composable(
                                route = "user_detail/{userId}",
                                arguments = listOf(navArgument("userId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                                // UserDetailScreen(navController, viewModel, userId)
                                Text("UserDetailScreen Placeholder: User ID $userId")
                            }
                            composable("users_list") { UsersListScreen(navController, viewModel) } // Используем реальный экран
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Главная") },
            selected = currentRoute == "main",
            onClick = {
                navController.navigate("main") {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.LocalLibrary, contentDescription = "Books") },
            label = { Text("Книги") },
            selected = currentRoute == "books_list",
            onClick = {
                navController.navigate("books_list") {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Users") },
            label = { Text("Профиль") },
            selected = currentRoute == "users_list",
            onClick = {
                navController.navigate("users_list") {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Настройки") },
            selected = currentRoute == "settings",
            onClick = {
                navController.navigate("settings") {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
    }
}
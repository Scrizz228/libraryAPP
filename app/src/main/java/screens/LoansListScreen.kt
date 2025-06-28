package com.example.libraryapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.libraryapp.models.Loan
import com.example.libraryapp.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansListScreen(navController: NavController, viewModel: LibraryViewModel) {
    val loans by viewModel.loans.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getLoans()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loans List") },
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
            Text("Loans List", style = MaterialTheme.typography.headlineMedium)

            if (loans.isEmpty()) {
                Text("No loans available", modifier = Modifier.padding(top = 16.dp))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(loans) { loan ->
                        LoanItem(loan = loan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Back")
            }
        }
    }
}


@Composable
fun LoanItem(loan: Loan) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Loan ID: ${loan.id}")
            Text("User ID: ${loan.userId}")
            Text("Book ID: ${loan.bookId}")
            Text("Issue Date: ${loan.issueDate}")
            Text("Return Date: ${loan.returnDate ?: "Not returned"}")
        }
    }
}
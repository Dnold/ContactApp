package com.example.contactapp.screens // Assuming this is your package

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width // For spacing between text and button icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope // <<< IMPORT THIS
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController // Use NavHostController consistently
import com.example.contactapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch // <<< IMPORT THIS

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    navController: NavHostController // Changed NavController to NavHostController for consistency
) {
    val darkMode by viewModel.darkMode.collectAsState()
    // val navController: NavController // This line was redundant and incorrectly typed, removed it.
    // The parameter 'navController: NavHostController' is used.

    val scope = rememberCoroutineScope() // <<< GET THE SCOPE HERE

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Einstellungen", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = darkMode,
                    onCheckedChange = { viewModel.toggleDarkMode() } // toggleDarkMode is likely not suspend
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // Added more space

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Alle Kontakte löschen", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    // Launch a coroutine to call the suspend function
                    scope.launch { // <<< LAUNCH COROUTINE HERE
                        viewModel.clearUsers() // Assuming it's named clearAllUsers in ViewModel
                    }
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Alle löschen")
                    Spacer(modifier = Modifier.width(4.dp)) // Optional: space between icon and text
                    Text("Löschen")
                }
            }
            // You can add more settings rows here
        }
    }
}
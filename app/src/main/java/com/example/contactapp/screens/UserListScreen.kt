package com.example.contactapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add // For the main FAB icon
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person // For "Add Manually"

// import androidx.compose.material.icons.filled.Place // Replaced 'Place' with QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.contactapp.NavDestinations // Keep if NavDestinations is used directly here
import com.example.contactapp.model.User
import com.example.contactapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // ExperimentalLayoutApi might not be needed
@Composable
fun UserListScreen(
    viewModel: MainViewModel,
    onUserClick: (User) -> Unit,
    onSettingsClick: () -> Unit,
    onQrClick: () -> Unit, // This might be redundant now, see comments
    onAddUserManuallyClick: () -> Unit,
    navController: NavController, // Keep if needed for other direct navigation from this screen
    onScanQrClick: () -> Unit // This is used by the FAB menu
) {
    val users by viewModel.users.collectAsState()
    var query by remember { mutableStateOf("") }
    var sortAsc by remember { mutableStateOf(true) }
    var selectedGender by remember { mutableStateOf("") }
    var minAgeInput by remember { mutableStateOf("") }
    var maxAgeInput by remember { mutableStateOf("") }

    var showFabMenu by remember { mutableStateOf(false) }

    val minAge = minAgeInput.toIntOrNull() ?: Int.MIN_VALUE
    val maxAge = maxAgeInput.toIntOrNull() ?: Int.MAX_VALUE

    val filteredUsers = users
        .filter { user ->
            val nameMatch = "${user.firstName} ${user.lastName}".contains(query, ignoreCase = true)
            val genderMatch = selectedGender.isEmpty() || user.gender.equals(selectedGender, ignoreCase = true)
            val age = user.age ?: 0
            val ageMatch = age in minAge..maxAge
            nameMatch && genderMatch && ageMatch
        }
        // Corrected sorting logic:
        .let { userList ->
            if (sortAsc) {
                userList.sortedBy { it.lastName }
            } else {
                userList.sortedByDescending { it.lastName }
            }
        }

    LaunchedEffect(Unit) {
        // viewModel.fetchUsersIfNeeded() // Uncomment if you have this logic
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Grid") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            Box { // Box to anchor DropdownMenu to the FAB
                FloatingActionButton(onClick = {
                    showFabMenu = !showFabMenu // Toggle menu visibility
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Open add options menu")
                }

                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Scan QR to Add User") }, // Clarified text
                        onClick = {
                            onScanQrClick() // Use the specific lambda for scanning action
                            showFabMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Add, contentDescription = "Scan QR Code to add user")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Manually") },
                        onClick = {
                            onAddUserManuallyClick()
                            showFabMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Add User Manually")
                        }
                    )
                }
            }
        }
    ) { paddingValues -> // Renamed padding to paddingValues to avoid clash if used as parameter
        Column(Modifier.padding(paddingValues)) {
            // Filter Section
            Column(Modifier.padding(16.dp)) { // Increased padding for filter section
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search by Name") }, // More descriptive label
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = minAgeInput,
                        onValueChange = { minAgeInput = it.filter { char -> char.isDigit() } }, // Allow only digits
                        label = { Text("Min Age") },
                        modifier = Modifier.weight(1f),
                        isError = minAgeInput.isNotEmpty() && minAgeInput.toIntOrNull() == null,
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = maxAgeInput,
                        onValueChange = { maxAgeInput = it.filter { char -> char.isDigit() } }, // Allow only digits
                        label = { Text("Max Age") },
                        modifier = Modifier.weight(1f),
                        isError = maxAgeInput.isNotEmpty() && maxAgeInput.toIntOrNull() == null,
                        singleLine = true
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("Filter by Gender:", style = MaterialTheme.typography.labelMedium) // Added title for gender filter
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Spacing between filter chips
                ) {
                    GenderFilterOption("", "All", selectedGender) { selectedGender = it }
                    GenderFilterOption("male", "Male", selectedGender) { selectedGender = it }
                    GenderFilterOption("female", "Female", selectedGender) { selectedGender = it }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Sort by Last Name: ", style = MaterialTheme.typography.labelMedium) // More descriptive
                    IconButton(onClick = { sortAsc = !sortAsc }) {
                        Icon(
                            imageVector = if (sortAsc) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (sortAsc) "Sort Ascending" else "Sort Descending"
                        )
                    }
                }
            }

            // User Grid
            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (users.isEmpty()) "No users available. Add some!" else "No users match your current filters.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = paddingValues.calculateBottomPadding() + 8.dp)
                ) {
                    items(filteredUsers, key = { user -> user.uuid }) { user -> // Added key for performance
                        Card(
                            modifier = Modifier
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .fillMaxWidth()
                                .clickable { onUserClick(user) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Added slight elevation
                        ) {
                            Column(Modifier.padding(12.dp)) { // Increased padding inside card
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = "Photo of ${user.firstName} ${user.lastName}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .align(Alignment.CenterHorizontally), // Center image if it doesn't fill
                                    // Consider adding placeholder and error drawables to AsyncImage
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "${user.firstName} ${user.lastName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1 // Prevent very long names from breaking layout
                                )
                                Text(
                                    text = user.phone.ifBlank { "No phone" }, // Handle blank phone
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = (user.age?.let { "$it years, " } ?: "") + user.gender.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, // Capitalize gender
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

@OptIn(ExperimentalMaterial3Api::class) // For FilterChip
@Composable
fun GenderFilterOption(value: String, label: String, selectedValue: String, onSelect: (String) -> Unit) {
    FilterChip(
        selected = selectedValue == value,
        onClick = { onSelect(if (selectedValue == value) "" else value) }, // Allow deselecting by clicking again
        label = { Text(label) },
        // leadingIcon = if (selected == value) { { Icon(Icons.Filled.Done, contentDescription = null) } } else null // Optional: add a check icon
    )
}
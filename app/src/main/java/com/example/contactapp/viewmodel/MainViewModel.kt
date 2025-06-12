package com.example.contactapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.contactapp.model.User // Ensure this import is correct
import com.example.contactapp.repository.UserRepository // Ensure this import is correct
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserRepository) : ViewModel() {

    // --- DARK MODE STATE ---
    private val _darkMode = MutableStateFlow<Boolean>(false) // Default to false
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    // --- USER LIST STATE ---
    // Exposed directly from the repository's Flow
    val users: StateFlow<List<User>> = repository.users
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // --- SELECTED USER STATE ---
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow() // Publicly exposed

    // --- ERROR MESSAGE STATE (Example) ---
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        Log.d("MainViewModel", "ViewModel initialized")
        // Optionally load dark mode preference from DataStore/SharedPreferences here
        // viewModelScope.launch { _darkMode.value = settingsRepository.getDarkMode() }
        fetchUsers() // Initial fetch of users
    }

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "Fetching users...")
                repository.fetchAndStoreUsers() // Assumes this refreshes the 'users' Flow from repo
                Log.d("MainViewModel", "Users fetch attempt complete.")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching users", e)
                _errorMessage.value = "Failed to fetch users: ${e.message}"
            }
        }
    }

    fun loadUserById(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "Loading user by ID: $userId")
                // Assuming repository has a suspend function to get a user by ID
                val user = repository.getUserById(userId)
                _selectedUser.value = user
                if (user == null) {
                    Log.w("MainViewModel", "User with ID $userId not found in repository.")
                } else {
                    Log.d("MainViewModel", "User $userId loaded: ${user.firstName}")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading user by ID $userId", e)
                _errorMessage.value = "Failed to load user details: ${e.message}"
                _selectedUser.value = null // Clear selection on error
            }
        }
    }

    fun clearSelectedUser() {
        Log.d("MainViewModel", "Clearing selected user.")
        _selectedUser.value = null
    }

    fun toggleDarkMode() {
        _darkMode.value = !_darkMode.value
        Log.d("MainViewModel", "Dark mode toggled to: ${_darkMode.value}")
        // Optionally save this preference to DataStore/SharedPreferences
        // viewModelScope.launch { settingsRepository.saveDarkMode(_darkMode.value) }
    }

    fun addOrUpdateUser(user: User) {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "Adding/Updating user: ${user.uuid}")
                repository.addOrUpdateUser(user)
                // Optionally, trigger a refresh of the user list or selected user if needed
                // fetchUsers() // If adding affects the main list display immediately
                Log.d("MainViewModel", "User ${user.uuid} added/updated successfully.")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error adding/updating user ${user.uuid}", e)
                _errorMessage.value = "Failed to save user: ${e.message}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    suspend fun clearUsers(){
        repository.clearUsers()
    }
}
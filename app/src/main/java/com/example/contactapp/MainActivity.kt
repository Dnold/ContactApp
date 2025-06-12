package com.example.contactapp


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.contactapp.data.AppDatabase
import com.example.contactapp.model.User // Ensure this is imported for onUserClick lambda
import com.example.contactapp.model.toJsonString
import com.example.contactapp.qrcodes.QRCodeView
import com.example.contactapp.qrcodes.ScanUserQrScreen
import com.example.contactapp.repository.UserRepository
import com.example.contactapp.screens.SettingsScreen

import com.example.contactapp.screens.UserDetailScreen
import com.example.contactapp.screens.UserListScreen
import com.example.contactapp.ui.theme.ContactAppTheme
import com.example.contactapp.viewmodel.MainViewModel

object NavDestinations {
    const val USER_LIST = "user_list"
    const val USER_DETAIL_ROUTE = "user_detail"
    const val USER_DETAIL_ARG_ID = "userId"
    const val USER_DETAIL_PATH = "$USER_DETAIL_ROUTE/{$USER_DETAIL_ARG_ID}"
    const val SETTINGS = "settings"
    const val QR_CODE_DISPLAY = "qr_code_display"
    const val ADD_USER_MANUALLY = "add_user_manually"
    const val SCAN_USER_QR = "scan_user_qr"
}

class MainViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(applicationContext)
        val repo = UserRepository(db.userDao())
        viewModel = ViewModelProvider(this, MainViewModelFactory(repo))[MainViewModel::class.java]

        setContent {
            // This assumes viewModel.darkMode is a StateFlow<Boolean>
            val useDarkTheme by viewModel.darkMode.collectAsState()

            ContactAppTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MyApp(viewModel: MainViewModel) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = NavDestinations.USER_LIST) {
        composable(NavDestinations.USER_LIST) {
            UserListScreen(
                viewModel = viewModel,
                onUserClick = { user: User ->
                    navController.navigate("${NavDestinations.USER_DETAIL_ROUTE}/${user.uuid}")
                },
                onSettingsClick = {
                    navController.navigate(NavDestinations.SETTINGS)
                },
                onScanQrClick = { // This is used by UserListScreen's FAB menu
                    navController.navigate(NavDestinations.SCAN_USER_QR)
                },
                onAddUserManuallyClick = { // This is used by UserListScreen's FAB menu
                    Log.d("MyApp", "Add User Manually / Fetch Users clicked from FAB Menu")
                    // If you have an "Add User Manually" screen, navigate to it:
                    // navController.navigate(NavDestinations.ADD_USER_MANUALLY)
                    // Or if it's just to trigger a general fetch:
                    viewModel.fetchUsers()
                },
                // --- FIXES ---
                onQrClick = {
                    // This lambda is passed to UserListScreen.
                    // Based on your current UserListScreen, it seems the "Scan QR to Add User"
                    // DropdownMenuItem correctly uses onScanQrClick.
                    // So, what is this onQrClick for?
                    // If it's for a different QR action (e.g., displaying a user's QR),
                    // you'd navigate accordingly.
                    // If it's redundant because onScanQrClick covers the main QR scanning,
                    // you could remove it from UserListScreen's parameters.
                    // For now, let's make it safe:
                    Log.d("MyApp", "onQrClick called - no specific action defined in MainActivity for this.")
                    // navController.navigate(NavDestinations.QR_CODE_DISPLAY) // Example if it was to display a QR
                },
                navController = navController // <<<< PASS THE ACTUAL navController from MyApp
            )
        }
        composable(
            route = NavDestinations.USER_DETAIL_PATH,
            arguments = listOf(navArgument(NavDestinations.USER_DETAIL_ARG_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(NavDestinations.USER_DETAIL_ARG_ID)

            LaunchedEffect(userId) {
                if (userId != null) {
                    Log.d("MyApp", "Loading user by ID: $userId")
                    viewModel.loadUserById(userId)
                } else {
                    Log.d("MyApp", "User ID is null, clearing selected user.")
                    viewModel.clearSelectedUser()
                }
            }

            // This assumes viewModel.selectedUser is a StateFlow<User?>
            val user by viewModel.selectedUser.collectAsState()

            DisposableEffect(Unit) {
                onDispose {
                    Log.d("MyApp", "Disposing UserDetail, clearing selected user.")
                    viewModel.clearSelectedUser()
                }
            }

            if (user != null) {
                UserDetailScreen(user = user!!, onNavigateBack = { // user is smart-cast
                    navController.popBackStack()
                })
            } else if (userId != null) {
                // Loading state while user is null but userId is present (after LaunchedEffect starts)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text("Loading user data...", modifier = Modifier.padding(top = 8.dp))
                    }
                }
            } else {
                // Should ideally not happen if navigation is set up correctly,
                // but as a fallback or if userId was truly never provided.
                Text("User ID not available.", modifier = Modifier.padding(16.dp))
            }
        }
        composable(NavDestinations.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                navController = navController // Pass navController if SettingsScreen needs it for navigation
            )
        }
        composable(NavDestinations.QR_CODE_DISPLAY) {
            // This assumes viewModel.selectedUser is a StateFlow<User?>
            // And User data class has toJsonString()
            val currentUser by viewModel.selectedUser.collectAsState()
            val qrData = currentUser?.toJsonString() ?: "{\"error\":\"No user data for QR\"}" // Fallback JSON

            Log.d("MyApp", "Displaying QR Code for data: ${qrData.take(100)}...")
            QRCodeView(
                data = qrData,
                modifier = Modifier.padding(16.dp).fillMaxSize() // Example modifier
            )
        }
        composable(NavDestinations.SCAN_USER_QR) {
            ScanUserQrScreen(
                viewModel = viewModel,
                onQrCodeScannedAndProcessed = {
                    Log.d("MyApp", "QR Scanned and Processed, navigating back.")
                    navController.popBackStack()
                }
            )
        }
        // Example for ADD_USER_MANUALLY if you create that screen
        // composable(NavDestinations.ADD_USER_MANUALLY) {
        //     AddUserManuallyScreen(viewModel = viewModel, onUserAdded = { navController.popBackStack() })
        // }
    }
}
package com.example.contactapp.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape // For circular image
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // For clipping shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // For Coil placeholder/error
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource // For painterResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import coil.compose.AsyncImage // Import Coil's AsyncImage
import coil.request.ImageRequest
import com.example.contactapp.R // Assuming you'll have a placeholder drawable
import com.example.contactapp.model.User
import com.example.contactapp.model.toJsonString
import com.example.contactapp.util.generateQrCodeBitmap // Ensure this is correctly imported or located

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun formatDisplayDate(dateString: String?): String {
    return dateString?.substringBefore("T") ?: "N/A" // Basic formatting
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    user: User,
    onNavigateBack: () -> Unit
) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingQr by remember { mutableStateOf(true) }
    val qrCodeSize: Dp = 220.dp
    val qrCodeSizePx = with(LocalDensity.current) { qrCodeSize.roundToPx() }

    LaunchedEffect(user) {
        isLoadingQr = true
        val userDataJson = user.toJsonString()
        if (userDataJson.isNotBlank()) {
            withContext(Dispatchers.IO) {
                val bitmap = generateQrCodeBitmap(
                    content = userDataJson,
                    width = qrCodeSizePx,
                    height = qrCodeSizePx
                )
                withContext(Dispatchers.Main) {
                    qrBitmap = bitmap
                    isLoadingQr = false
                }
            }
        } else {
            isLoadingQr = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${user.firstName} ${user.lastName}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- User Profile Image ---
            user.photoUrl?.let { imageUrl -> // Use the correct photoUrl field from your User model
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true) // Enable crossfade animation

                        .build(),
                    contentDescription = "Photo of ${user.firstName}",
                    modifier = Modifier
                        .size(150.dp) // Adjust size as needed
                        .padding(vertical = 16.dp)
                        .clip(CircleShape), // Make the image circular
                    contentScale = ContentScale.Crop // Crop to fit the circle
                )
            }
            if (user.photoUrl == null) { // Add space if no photo
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Details Sections ---
            DetailSectionCard("Contact Information") {
                user.email?.let { DetailItem("Email:", it) }
                user.phone?.let { DetailItem("Phone:", it) }
            }

            DetailSectionCard("Location") {
                user.street?.let { DetailItem("Street:", it) }
                user.city?.let { DetailItem("City:", it) }
                user.state?.let { DetailItem("State:", it) }
                user.country?.let { DetailItem("Country:", it) }
            }

            DetailSectionCard("Personal Details") {
                user.birthDate?.let { DetailItem("Birthdate:", formatDisplayDate(it)) }
                user.gender?.let { DetailItem("Gender:", it.replaceFirstChar { char -> char.uppercase() }) }
                user.nationality?.let { DetailItem("Nationality:", it) }
            }

            // --- QR Code Section Card ---
            DetailSectionCard("Share Contact via QR") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Scan this code to add ${user.firstName} to contacts.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    if (isLoadingQr) {
                        CircularProgressIndicator(modifier = Modifier.size(qrCodeSize))
                    } else if (qrBitmap != null) {
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "Contact QR Code for ${user.firstName}",
                                modifier = Modifier.size(qrCodeSize),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        Text(
                            "Could not generate QR code.",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = qrCodeSize / 2)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DetailSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}
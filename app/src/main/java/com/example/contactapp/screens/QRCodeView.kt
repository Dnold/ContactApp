package com.example.contactapp.qrcodes

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.contactapp.model.User // For parsing
import com.example.contactapp.util.generateQrCodeBitmap
import com.example.contactapp.viewmodel.MainViewModel
// import generateQrCodeBitmap // Assuming this is in the same package or a top-level function in the same module
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.text.format

//---------------------------------------------------------------------//
// Composable to DISPLAY a QR Code
// TODO: Ensure 'generateQrCodeBitmap' is correctly implemented and accessible.
// If it's a utility function, make sure its implementation is sound.
//---------------------------------------------------------------------//
@Composable
fun QRCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val density = LocalDensity.current
    val sizePx = remember(size, density) { with(density) { size.roundToPx() } }

    LaunchedEffect(data, sizePx) {
        isLoading = true
        if (data.isNotBlank()) {
            // Assuming generateQrCodeBitmap is a suspend function or can be wrapped
            // For simplicity, if it's not suspend, ensure it's not blocking UI too long.
            // Using withContext(Dispatchers.IO) is good.
            val bitmapResult = withContext(Dispatchers.IO) {
                try {
                    // This is a placeholder if you don't have the actual function
                    // Replace with your actual QR generation logic
                    // For example, using a library like ZXing
                    Log.d("QRCodeView", "Attempting to generate QR for data: ${data.take(50)}")
                    // Example placeholder - replace with actual implementation
                    // createPlaceholderBitmap(sizePx, sizePx) // Replace this line
                    generateQrCodeBitmap( // Ensure this function exists and works
                        content = data,
                        width = sizePx,
                        height = sizePx
                    )
                } catch (e: Exception) {
                    Log.e("QRCodeView", "Error generating QR bitmap", e)
                    null
                }
            }
            qrBitmap = bitmapResult // Already on Main thread due to LaunchedEffect scope
            isLoading = false
        } else {
            Log.d("QRCodeView", "Data for QR is blank, clearing bitmap.")
            qrBitmap = null
            isLoading = false
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(size / 2))
        } else if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap!!.asImageBitmap(),
                contentDescription = "QR Code for data: ${data.take(30)}...",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text("No data for QR code or error in generation.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// Example placeholder for generateQrCodeBitmap if you need one to make QRCodeView compile
// You should replace this with your actual QR code generation logic (e.g., using ZXing library)
/*
private fun generateQrCodeBitmap(content: String, width: Int, height: Int): Bitmap? {
    // This is a dummy implementation. Replace with a real QR code generator.
    Log.w("QRCodeView_generate", "generateQrCodeBitmap called with content: $content. Using placeholder.")
    if (content.isBlank() || width <= 0 || height <= 0) return null
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 20f
        textAlign = android.graphics.Paint.Align.CENTER
    }
    canvas.drawColor(android.graphics.Color.WHITE) // Background
    canvas.drawText("QR:$content".take(20), width / 2f, height / 2f, paint)
    return bitmap
}
*/


//---------------------------------------------------------------------//
// JSON Parsing Logic for User from QR Code
//---------------------------------------------------------------------//
fun parseUserFromJsonString(jsonString: String): User? {
    Log.d("UserParsing", "Attempting to parse JSON string: ${jsonString.take(100)}...")
    return try {
        val rootJson = JSONObject(jsonString)
        // val appIdentifier = rootJson.optString("appIdentifier") // Optional: uncomment if you add this to your QR data
        // if (appIdentifier != "com.example.contactapp.user") { // Optional
        //     Log.w("UserParsing", "QR code has non-matching app identifier: $appIdentifier")
        //     return null
        // }
        // Assuming your User data is directly under a "userData" object in the JSON
        // If not, adjust accordingly. If User fields are at the root, use rootJson directly.
        val userData = rootJson.optJSONObject("userData") ?: rootJson // Fallback to root if "userData" doesn't exist

        fun JSONObject.optNullableString(key: String): String? {
            return if (this.has(key) && !this.isNull(key)) {
                this.getString(key).takeIf { it.isNotBlank() && it.lowercase() != "null" }
            } else {
                null
            }
        }
        User(
            uuid = userData.getString("uuid"), // Ensure this key exists
            firstName = userData.getString("firstName"), // Ensure this key exists
            lastName = userData.getString("lastName"), // Ensure this key exists
            birthDate = userData.optNullableString("birthDate") ?: "",
            phone = userData.optNullableString("phone") ?: "",
            photoUrl = userData.optNullableString("photoUrl") ?: "",
            email = userData.optNullableString("email") ?: "",
            gender = userData.optNullableString("gender") ?: "",
            age = userData.optInt("age", 0), // Default to 0 if "age" is missing or not an int
            nationality = userData.optNullableString("nationality") ?: "",
            street = userData.optNullableString("street") ?: "",
            city = userData.optNullableString("city") ?: "",
            state = userData.optNullableString("state") ?: "",
            country = userData.optNullableString("country") ?: ""
        )
    } catch (e: Exception) {
        Log.e("UserParsing", "Error parsing user JSON from QR: ${e.message}", e)
        null
    }
}


//---------------------------------------------------------------------//
// QR Code SCANNING Functionality
//---------------------------------------------------------------------//

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandleCameraPermission(
    onPermissionGranted: @Composable () -> Unit,
    onPermissionDenied: @Composable (showRationale: Boolean, requestPermission: () -> Unit) -> Unit,
    onPermissionPermanentlyDenied: @Composable () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Effect to launch permission request when the composable enters composition
    // if permission is not granted and rationale is not needed (i.e., first time or denied previously).
    LaunchedEffect(cameraPermissionState.status) { // Re-evaluate if status changes
        if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale) {
            Log.d("HandleCameraPermission", "Permission not granted and no rationale needed. Requesting.")
            cameraPermissionState.launchPermissionRequest()
        }
    }

    when (cameraPermissionState.status) {
        PermissionStatus.Granted -> {
            Log.d("HandleCameraPermission", "Permission Granted.")
            onPermissionGranted()
        }
        is PermissionStatus.Denied -> {
            if (cameraPermissionState.status.shouldShowRationale) {
                Log.d("HandleCameraPermission", "Permission Denied. Rationale should be shown.")
                onPermissionDenied(true) { cameraPermissionState.launchPermissionRequest() }
            } else {
                // If rationale is false, it means permission was denied, and the user
                // chose "Don't ask again", or it's the first time and the system dialog is showing/showed.
                // The LaunchedEffect above handles the initial request.
                // If it's permanently denied, this branch will be hit after user interaction.
                Log.d("HandleCameraPermission", "Permission Denied. No rationale (could be permanently denied or first request flow).")
                // We provide a way for ScanUserQrScreen to show a "permanently denied" message.
                onPermissionPermanentlyDenied()
            }
        }
    }
}


@Composable
fun QRCodeScannerView(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasScanned by remember { mutableStateOf(false) }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        Log.d("QRCodeScannerView", "AnalysisExecutor created.")
        onDispose {
            Log.d("QRCodeScannerView", "AnalysisExecutor shutting down.")
            analysisExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            Log.d("QRCodeScannerView", "AndroidView Factory called.")
            val previewView = PreviewView(ctx).apply {
                // Optional: Adjust PreviewView scale type if needed for better preview
                // scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            cameraProviderFuture.addListener({
                Log.d("QRCodeScannerView", "CameraProviderFuture listener invoked.")
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    Log.d("QRCodeScannerView", "CameraProvider obtained: $cameraProvider")

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                            Log.d("QRCodeScannerView", "Preview SurfaceProvider set.")
                        }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                    Log.d("QRCodeScannerView", "CameraSelector built for LENS_FACING_BACK.")

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        // Optional: Consider setting a target resolution for analysis
                        // .setTargetResolution(android.util.Size(1280, 720))
                        .build()
                        .also {
                            Log.d("QRCodeScannerView", "ImageAnalysis built. Setting analyzer...")
                            it.setAnalyzer(analysisExecutor, QRCodeAnalyzer(
                                onQrCodeScanned = { result ->
                                    // This callback from QRCodeAnalyzer might not be on the main thread
                                    // depending on ML Kit's implementation.
                                    // For state updates in Compose, ensure they happen on the main thread.
                                    // However, `onQrCodeScanned` here is passed to `ScanUserQrScreen`
                                    // which then uses it to update its state, which is fine.
                                    Log.i("QRCodeScannerView_Callback", "Raw QR Result from Analyzer: ${result.take(50)}")
                                    if (!hasScanned) { // Prevent multiple rapid scans if analyzer is quick
                                        hasScanned = true // Set immediately
                                        onQrCodeScanned(result) // Propagate to ScanUserQrScreen
                                    }
                                },
                                context = ctx
                            ))
                            Log.d("QRCodeScannerView", "ImageAnalysis Analyzer set.")
                        }

                    Log.d("QRCodeScannerView", "Unbinding all use cases before binding new ones.")
                    cameraProvider.unbindAll()

                    Log.d("QRCodeScannerView", "Attempting to bind Preview and ImageAnalysis to lifecycle.")
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    Log.d("QRCodeScannerView", "Use cases bound to lifecycle successfully.")

                } catch (exc: Exception) {
                    Log.e("QRCodeScannerView", "Use case binding or setup failed", exc)
                    // Consider showing an error message to the user here or propagating the error
                }
            }, ContextCompat.getMainExecutor(ctx)) // Ensure listener runs on the main thread
            previewView
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalGetImage::class)
private class QRCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit,
    @Suppress("unused") private val context: Context // Keep if might be used later
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    private val scanner: BarcodeScanner = BarcodeScanning.getClient(options)
    private var frameCounter = 0L // Use Long for potentially many frames

    init {
        Log.d("QRCodeAnalyzer", "Analyzer Initialized. Scanner: $scanner")
    }

    override fun analyze(imageProxy: ImageProxy) {
        frameCounter++
        val currentFrame = frameCounter // For logging consistency within this call

        if (currentFrame % 30 == 1L) { // Log roughly once per second (adjust if needed)
            Log.d("QRCodeAnalyzer", "analyze() called. Frame: $currentFrame. Rotation: ${imageProxy.imageInfo.rotationDegrees}. Format: ${imageProxy.format}")
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            if (currentFrame == 1L) { // Log details of the first frame
                Log.d("QRCodeAnalyzer", "First frame InputImage created. Width: ${image.width}, Height: ${image.height}, Format: ${image.format}")
            }

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val firstBarcode = barcodes.first() // We only care about the first QR found
                        firstBarcode.rawValue?.let { qrContent ->
                            Log.i("QRCodeAnalyzer", "SUCCESS: Barcodes detected: ${barcodes.size}. First QR Content: ${qrContent.take(100)}")
                            onQrCodeScanned(qrContent) // <<<< THE KEY CALLBACK!
                        } ?: Log.w("QRCodeAnalyzer", "Barcode detected but rawValue is null.")
                    } else {
                        if (currentFrame % 90 == 1L) { // Log less frequently if no barcodes found
                            Log.v("QRCodeAnalyzer", "No barcodes detected in frame $currentFrame.")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QRCodeAnalyzer", "FAILURE: Barcode scanning ML Kit processing error in frame $currentFrame.", e)
                }
                .addOnCompleteListener {
                    // Crucial: Close the ImageProxy to allow new frames to be delivered.
                    imageProxy.close()
                }
        } else {
            if (currentFrame % 60 == 1L) { // Log if mediaImage is consistently null
                Log.w("QRCodeAnalyzer", "mediaImage was null in frame $currentFrame. Closing proxy.")
            }
            imageProxy.close() // Still need to close it.
        }
    }
}

@Composable
fun ScanUserQrScreen(
    viewModel: MainViewModel,
    onQrCodeScannedAndProcessed: () -> Unit
) {
    var scannedQrContent by remember { mutableStateOf<String?>(null) }
    var showProcessingMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var cameraPermissionDeniedRationale by remember { mutableStateOf(false) }
    var cameraPermissionPermanentlyDenied by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope() // For launching coroutine from ViewModel

    fun processQrDataAndUpdateViewModel(
        qrData: String,
        vm: MainViewModel,
        onSuccessfulProcessing: () -> Unit,
        onErrorDuringProcessing: (String) -> Unit
    ) {
        Log.i("ScanUserQrScreen", "Processing QR Data: ${qrData.take(100)}")
        val user = parseUserFromJsonString(qrData)

        if (user != null) {
            Log.d("ScanUserQrScreen", "Successfully parsed User: ${user.firstName} ${user.lastName}, UUID: ${user.uuid}")
            // Assuming addOrUpdateUser is a suspend function or handles its own scope
            scope.launch { // Use coroutine scope to call suspend function or background task
                try {
                    vm.addOrUpdateUser(user) // Ensure this is implemented in your ViewModel
                    Log.d("ScanUserQrScreen", "User processed by ViewModel.")
                    withContext(Dispatchers.Main) { // Ensure UI updates on main thread
                        onSuccessfulProcessing()
                    }
                } catch (e: Exception) {
                    Log.e("ScanUserQrScreen", "Error calling addOrUpdateUser in ViewModel", e)
                    withContext(Dispatchers.Main) {
                        onErrorDuringProcessing("Failed to save user data: ${e.localizedMessage}")
                    }
                }
            }
        } else {
            Log.w("ScanUserQrScreen", "Failed to parse user from QR data.")
            onErrorDuringProcessing("Invalid QR code data. Please ensure it's a valid user QR code from this app.")
        }
    }

    // This LaunchedEffect is triggered when scannedQrContent changes and we are ready to process.
    // It handles the actual processing of the QR data.
    LaunchedEffect(scannedQrContent, showProcessingMessage) {
        if (showProcessingMessage && scannedQrContent != null) {
            val dataToProcess = scannedQrContent!! // We know it's not null here
            Log.d("ScanUserQrScreen", "LaunchedEffect triggered for processing: ${dataToProcess.take(50)}")
            errorMessage = null // Clear previous errors

            processQrDataAndUpdateViewModel(
                qrData = dataToProcess,
                vm = viewModel,
                onSuccessfulProcessing = {
                    Log.i("ScanUserQrScreen", "Processing successful, invoking onQrCodeScannedAndProcessed.")
                    onQrCodeScannedAndProcessed() // Navigate back or confirm
                },
                onErrorDuringProcessing = { err ->
                    Log.e("ScanUserQrScreen", "Error during QR data processing: $err")
                    errorMessage = err
                    showProcessingMessage = false // Stop showing "Processing..."
                    scannedQrContent = null     // Allow rescanning
                }
            )
        }
    }


    // Main UI structure for the screen
    when {
        showProcessingMessage -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Processing QR Code...")
                }
            }
        }
        errorMessage != null -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Error", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage!!, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    errorMessage = null
                    scannedQrContent = null // Reset to allow camera view again
                    showProcessingMessage = false
                    cameraPermissionDeniedRationale = false // Reset permission states as well
                    cameraPermissionPermanentlyDenied = false
                }) {
                    Text("Try Again")
                }
            }
        }
        cameraPermissionDeniedRationale -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera Permission Required", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("This app needs camera access to scan QR codes. Please grant the permission to continue.")
                Spacer(modifier = Modifier.height(16.dp))
                // The request will be triggered by HandleCameraPermission's onPermissionDenied's requestPermission lambda
                // No need for a button here if HandleCameraPermission is structured to re-request via its callback.
                // The Button to re-request is in HandleCameraPermission if status.shouldShowRationale is true.
            }
        }
        cameraPermissionPermanentlyDenied -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera Permission Needed", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Camera permission has been permanently denied. To use this feature, please enable it in the app settings.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    // TODO: Intent to open app settings
                    // context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
                    Log.w("ScanUserQrScreen", "Implement navigation to app settings.")
                }) {
                    Text("Open Settings")
                }
            }
        }
        else -> { // Default state: request permission or show camera
            HandleCameraPermission(
                onPermissionGranted = {
                    Log.d("ScanUserQrScreen", "Camera permission granted. Showing QRCodeScannerView.")
                    cameraPermissionDeniedRationale = false
                    cameraPermissionPermanentlyDenied = false
                    QRCodeScannerView(
                        modifier = Modifier.fillMaxSize(),
                        onQrCodeScanned = { result ->
                            // This is called from QRCodeScannerView when a QR is successfully scanned
                            Log.i("ScanUserQrScreen", "QR Code Scanned by QRCodeScannerView: ${result.take(50)}")
                            if (!showProcessingMessage && scannedQrContent == null) { // Ensure not already processing
                                scannedQrContent = result // This will trigger the LaunchedEffect
                                showProcessingMessage = true
                            }
                        }
                    )
                },
                onPermissionDenied = { showRationale, requestPermission ->
                    Log.d("ScanUserQrScreen", "Camera permission denied. Show rationale: $showRationale")
                    if (showRationale) {
                        // The UI for rationale is expected to be shown by HandleCameraPermission itself
                        // or this state can be used to display a more specific message here.
                        // For simplicity, we'll let HandleCameraPermission display its rationale UI.
                        // However, we set our state to true so ScanUserQrScreen can react if needed.
                        cameraPermissionDeniedRationale = true
                    }
                    // If not showing rationale, it might be the initial request or permanently denied.
                    // The `onPermissionPermanentlyDenied` callback handles the latter.
                },
                onPermissionPermanentlyDenied = {
                    Log.w("ScanUserQrScreen", "Camera permission permanently denied.")
                    cameraPermissionPermanentlyDenied = true
                    cameraPermissionDeniedRationale = false // Ensure rationale UI is not shown
                }
            )
        }
    }
}
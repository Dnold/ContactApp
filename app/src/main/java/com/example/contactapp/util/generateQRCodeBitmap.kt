package com.example.contactapp.util

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log // Added for logging
// import com.example.contactapp.model.User // Not directly used in this function
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
// import org.json.JSONObject // Not directly used in this function


fun generateQrCodeBitmap(
    content: String,
    width: Int = 512, // Desired width of the QR code in pixels
    height: Int = 512, // Desired height of the QR code in pixels
    errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.Q // Changed default to M for better robustness
): Bitmap? {
    if (content.isBlank()) {
        Log.w("generateQrCodeBitmap", "Content is blank, returning null.")
        return null
    }

    // Log the content that is being encoded. This is CRUCIAL for debugging.
    Log.d("generateQrCodeBitmap", "Generating QR Code with the following parameters:")
    Log.d("generateQrCodeBitmap", "Content Length: ${content.length}")
    Log.d("generateQrCodeBitmap", "Content (first 200 chars): '${content.take(200)}'") // Log a portion of the content
    if (content.length > 200) {
        Log.d("generateQrCodeBitmap", "... (content truncated in log)")
    }
    Log.d("generateQrCodeBitmap", "Requested Width: $width, Height: $height")
    Log.d("generateQrCodeBitmap", "Error Correction Level: $errorCorrectionLevel")


    val qrCodeWriter = QRCodeWriter()
    val hints = mutableMapOf<EncodeHintType, Any>()
    hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
    hints[EncodeHintType.ERROR_CORRECTION] = errorCorrectionLevel
    hints[EncodeHintType.MARGIN] = 2 // Margin around QR code, 0-4 is common (2 is a good default)

    return try {
        Log.d("generateQrCodeBitmap", "Encoding content with ZXing...")
        val bitMatrix = qrCodeWriter.encode(
            content,
            BarcodeFormat.QR_CODE,
            width,
            height,
            hints
        )
        Log.d("generateQrCodeBitmap", "BitMatrix generated. Creating Bitmap.")

        // Using RGB_565 is generally fine for B&W QR codes and saves memory.
        // If you suspect any color issues (though unlikely for QR), you could test with ARGB_8888.
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        Log.i("generateQrCodeBitmap", "Bitmap successfully generated for content (length: ${content.length}).")
        bitmap
    } catch (e: Exception) {
        // Log the error using Android's Log class for better Logcat integration
        Log.e("generateQrCodeBitmap", "Error generating QR code bitmap: ${e.message}", e)
        // e.printStackTrace() // This prints to System.err, Log.e is generally preferred in Android.
        null
    }
}
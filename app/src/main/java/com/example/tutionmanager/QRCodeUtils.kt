package com.example.tutionmanager

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

object QRCodeUtils {
    fun generateQRCode(content: String): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 400, 400)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

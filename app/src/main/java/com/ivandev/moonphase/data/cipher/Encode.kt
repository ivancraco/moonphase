package com.ivandev.moonphase.data.cipher

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

object Encode {
    fun encodeImage(bitmap: Bitmap): String {
        val previewWidth = 200
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}
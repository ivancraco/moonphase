package com.ivandev.moonphase.data.cipher

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayInputStream

object Decode {
    fun decodeImage(image: String): Bitmap {
        val stream =
            ByteArrayInputStream(Base64.decode(image.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
        return BitmapFactory.decodeStream(stream)
    }
}
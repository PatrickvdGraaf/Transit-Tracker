package com.crepetete.transittracker.models.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

/**
 *
 * Created by Patrick van de Graaf on 5/18/2018.
 *
 */
class DatabaseBitmapUtility {
    companion object {
        // Convert from bitmap to byte array.
        fun getBytes(bitmap: Bitmap): ByteArray? {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
            return stream.toByteArray()
        }

        // Convert from byte array to bitmap.
        fun getImage(image: ByteArray): Bitmap? {
            return BitmapFactory.decodeByteArray(image, 0, image.size)
        }
    }
}
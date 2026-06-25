package com.bghitech.momenta.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun compressForUpload(input: File): File {
        val maxWidth = 1440
        val quality = 82

        val bitmap = decodeSampledBitmap(input, maxWidth)
        val rotated = fixOrientation(bitmap, input)

        val output = File(context.cacheDir, "compressed_${input.name}")
        FileOutputStream(output).use { out ->
            rotated.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }

        return output
    }

    private fun decodeSampledBitmap(file: File, maxWidth: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.path, options)

        val scaleFactor = (options.outWidth / maxWidth).coerceAtLeast(1)

        val sampleOptions = BitmapFactory.Options().apply {
            inSampleSize = scaleFactor
        }
        return BitmapFactory.decodeFile(file.path, sampleOptions)
    }

    private fun fixOrientation(bitmap: Bitmap, file: File): Bitmap {
        try {
            val exif = ExifInterface(file)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        } catch (_: Exception) { }
        return bitmap
    }
}

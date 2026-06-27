package com.bghitech.momenta.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import java.io.File
import java.io.FileOutputStream

object PhotoEffectProcessor {
    fun apply(context: Context, input: File, effect: PhotoEffect): File {
        if (effect == PhotoEffect.Natural) return input

        val source = BitmapFactory.decodeFile(input.absolutePath) ?: return input
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(effect.colorMatrix())
        }
        canvas.drawBitmap(source, 0f, 0f, paint)

        val output = File(context.cacheDir, "${input.nameWithoutExtension}_${effect.name.lowercase()}.jpg")
        FileOutputStream(output).use { out ->
            result.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        source.recycle()
        result.recycle()
        return output
    }
}

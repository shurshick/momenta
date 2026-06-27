package com.bghitech.momenta.core.media

import android.graphics.ColorMatrix

enum class PhotoEffect(val title: String, val subtitle: String) {
    Natural("Оригинал", "Без обработки"),
    Warm("Тепло", "Мягкий вечерний тон"),
    Cool("Прохлада", "Чище свет и синие тени"),
    Mono("Ч/Б", "Контрастный монохром"),
    Vivid("Живой", "Больше света и цвета"),
    Contrast("Контраст", "Глубже тени и акценты");

    fun colorMatrix(): ColorMatrix = when (this) {
        Natural -> ColorMatrix()
        Warm -> ColorMatrix(
            floatArrayOf(
                1.12f, 0f, 0f, 0f, 10f,
                0f, 1.03f, 0f, 0f, 6f,
                0f, 0f, 0.90f, 0f, -4f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        Cool -> ColorMatrix(
            floatArrayOf(
                0.92f, 0f, 0f, 0f, -2f,
                0f, 1.02f, 0f, 0f, 2f,
                0f, 0f, 1.15f, 0f, 8f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        Mono -> ColorMatrix().apply { setSaturation(0f) }
        Vivid -> ColorMatrix().apply { setSaturation(1.35f) }
        Contrast -> ColorMatrix(
            floatArrayOf(
                1.18f, 0f, 0f, 0f, -12f,
                0f, 1.18f, 0f, 0f, -12f,
                0f, 0f, 1.18f, 0f, -12f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }
}

package io.kailuzhang.github.demo

import android.graphics.Color
import androidx.annotation.ColorInt

data class Item(
    val viewType: Int,
    @ColorInt val backgroundColor: Int = Color.TRANSPARENT,
    val title: String? = null,
    val height: Float = 40f
)

data class TabData(
    val title: String,
    val startViewType: Int,
    val endViewType: Int
)
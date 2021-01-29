package io.kailuzhang.github.demo.utils

import android.content.res.Resources
import android.util.TypedValue

object UIUtils {
    fun dpToPx(dp: Float): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        ).toInt()

    fun getScreenW(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }
}
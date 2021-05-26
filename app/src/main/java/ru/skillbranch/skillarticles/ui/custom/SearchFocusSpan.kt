package ru.skillbranch.skillarticles.ui.custom

import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import androidx.core.graphics.ColorUtils

class SearchFocusSpan(private val bgColor: Int, private val fgColor: Int) : SearchSpan(bgColor, fgColor) {
    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.bgColor = bgColor
        textPaint.color = fgColor
    }
}
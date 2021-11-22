package ru.skillbranch.skillarticles.ui.custom.markdown

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.annotation.VisibleForTesting
import androidx.core.graphics.ColorUtils
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.dpToPx

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
class SearchBgHelper(
    context: Context,
    private val focusListener: (top: Int, bottom: Int) -> Unit,
    mockDrawable: Drawable?,
    mockDrawableLeft: Drawable?,
    mockDrawableMiddle: Drawable?,
    mockDrawableRight: Drawable?
) {

    constructor(
        context: Context,
        focusListener: (top: Int, bottom: Int) -> Unit
    ) : this(
        context,
        focusListener,
        null,
        null,
        null,
        null
    )

    private val padding: Int = context.dpToIntPx(4)
    private val borderWidth: Int = context.dpToIntPx(1)
    private val radius: Float = context.dpToPx(8)

    private val secondaryColor: Int = context.attrValue(R.attr.colorSecondary)
    private val alphaColor: Int = ColorUtils.setAlphaComponent(secondaryColor, 160)

    private val drawableLeft: Drawable = mockDrawableLeft ?: GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadii = floatArrayOf(
            radius, radius, // top left
            0f, 0f, // top right
            0f, 0f, // bottom right
            radius, radius // bottom left
        )
        color = ColorStateList.valueOf(alphaColor)
        setStroke(borderWidth, secondaryColor)
    }

    private val drawableMiddle: Drawable = mockDrawableMiddle ?: GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        color = ColorStateList.valueOf(alphaColor)
        setStroke(borderWidth, secondaryColor)
    }

    private val drawableRight: Drawable = mockDrawableRight ?: GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadii = floatArrayOf(
            0f, 0f,
            radius, radius,
            radius, radius,
            0f, 0f
        )
        color = ColorStateList.valueOf(alphaColor)
        setStroke(borderWidth, secondaryColor)
    }
}
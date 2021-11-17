package ru.skillbranch.skillarticles.ui.custom.markdown

import android.content.Context
import android.graphics.Rect
import android.text.Spannable
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatTextView
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx

class MarkdownTextView constructor(
    context: Context,
    fontSize: Float,
    private val isSizedDepend: Boolean = true
) : AppCompatTextView(context, null, 0), IMarkdownView {

    override var fontSize: Float = fontSize
        set(value) {
            textSize = value
            field = value
        }

    override val spannableContent: Spannable
        get() = text as Spannable

    private val color = context.attrValue(R.attr.colorOnBackground)
    private val focusRect = Rect()
    private val searchPadding = context.dpToIntPx(56)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var searchBgHelper = SearchBgHelper(context) { top, bottom ->
        focusRect.set(0, top - searchPadding, width, bottom + searchPadding)
        requestRectangleOnScreen(focusRect, false)
    }
}
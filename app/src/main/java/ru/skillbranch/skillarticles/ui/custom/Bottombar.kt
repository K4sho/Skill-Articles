package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import com.google.android.material.shape.MaterialShapeDrawable
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.LayoutBottombarBinding
import ru.skillbranch.skillarticles.ui.custom.behaviors.BottombarBehavior
import kotlin.math.hypot

class Bottombar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    var isSearchMode = false

    //Можно совместить объявление с инициализацией
    val binding: LayoutBottombarBinding =
        LayoutBottombarBinding.inflate(LayoutInflater.from(context), this)

    init {
        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elevation
        background = materialBg
    }

    fun setSearchState(isSearch: Boolean) {
        if (isSearchMode == isSearch || !isAttachedToWindow) return
        isSearchMode = isSearch
        if (isSearchMode) animatedShowSearch()
        else animateHideSearch()
    }

    fun setSearchInfo(searchCount: Int = 0, position: Int = 0) {
        with(binding) {
            btnResultDown.isEnabled = searchCount > 0
            btnResultUp.isEnabled = searchCount > 0

            //Хардкод строк обычно не приветствуется, особенно там где есть доступ к контексту.
            // Лучше положить эти строки в ресурсы и получать их при создании вьюхи,
            // сохраняя в приватное свойство.
            tvSearchResult.text = if (searchCount == 0) "Not found" else "${position.inc()} of $searchCount"

            //если в статье слово встретится один раз, то стрелка вниз не отключится,
            // так как сработает первое условие в when и остальные проверки будут пропущены.
            when(position){
                0 -> btnResultUp.isEnabled = false
                searchCount.dec() -> btnResultDown.isEnabled = false
            }
        }
    }

    private fun animateHideSearch() {
        binding.bottomGroup.isVisible = true
        val endRadius = hypot(width.toDouble(), height / 2.toDouble())
        val va = ViewAnimationUtils.createCircularReveal(
            binding.reveal,
            width,
            height / 2,
            0f,
            endRadius.toFloat()
        )
        va.doOnEnd {
            binding.reveal.isVisible = false
        }
        va.start()
    }

    private fun animatedShowSearch() {
        binding.reveal.isVisible = true
        val endRadius = hypot(width.toDouble(), height / 2.toDouble())
        val va = ViewAnimationUtils.createCircularReveal(
            binding.reveal,
            width,
            height / 2,
            0f,
            endRadius.toFloat()
        )
        va.doOnEnd {
            binding.bottomGroup.isVisible = false
        }
        va.start()
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return BottombarBehavior()
    }

    override fun onSaveInstanceState(): Parcelable {
        val saveState = SavedState(super.onSaveInstanceState())
        saveState.ssSearchMode = isSearchMode
        return saveState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) {
            isSearchMode = state.ssSearchMode
            binding.reveal.isVisible = isSearchMode
            binding.bottomGroup.isVisible = !isSearchMode
        }
    }

    private class SavedState : BaseSavedState, Parcelable {
        var ssSearchMode: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            ssSearchMode = parcel.readByte() != 0.toByte()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeByte(if (ssSearchMode) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(p0: Parcel): SavedState =
                SavedState(p0)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}
package ru.skillbranch.skillarticles.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.ui.delegates.AttrValue
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.*

class RootActivity : AppCompatActivity(), IArticleView {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var viewModelFactory: ViewModelProvider.Factory = ViewModelFactory("0")
    private val viewModel: ArticleViewModel by viewModels { viewModelFactory }

    private val vb: ActivityRootBinding by viewBinding(ActivityRootBinding::inflate)

    private val vbBottombar
    get() = vb.bottombar.binding

    private val vbSubmenu
    get() = vb.submenu.binding

    private lateinit var searchView: SearchView

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val bgColor by AttrValue(R.attr.colorSecondary)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val fgColor by AttrValue(R.attr.colorOnSecondary)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar()
        setupSubmenu()
        setupBottombar()

        viewModel.observeState(this, ::renderUi)
        viewModel.observeSubState(this, ArticleState::toBottombarData, ::renderBottombar)
        viewModel.observeSubState(this, ArticleState::toSubmenuData, ::renderSubmenu)
        viewModel.observeNotifications(this) {
            renderNotification(it)
        }
    }

    override fun renderUi(state: ArticleState) {
        vbBottombar.btnSettings.isChecked = state.isShowMenu
        if (state.isShowMenu) vb.submenu.open() else vb.submenu.close()
        vbBottombar.btnLike.isChecked = state.isLike
        vbBottombar.btnBookmark.isChecked = state.isBookmark
        vbSubmenu.switchMode.isChecked = state.isDarkMode
        delegate.localNightMode =
                if (state.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        if (state.isBigText) {
            vb.tvTextContent.textSize = 18f
            vbSubmenu.btnTextUp.isChecked = true
            vbSubmenu.btnTextDown.isChecked = false
        } else {
            vb.tvTextContent.textSize = 14f
            vbSubmenu.btnTextUp.isChecked = true
            vbSubmenu.btnTextDown.isChecked = false
        }

        vb.tvTextContent.text = if (state.isLoadingContent) "loading" else state.content.first() as String

        vb.toolbar.title = state.title ?: "Skill Articles"
        vb.toolbar.subtitle = state.category ?: "loading..."
        if (state.categoryIcon != null) vb.toolbar.logo = getDrawable(state.categoryIcon as Int)
    }

    /**
     * Метод для отрисовки уведомлений для пользователя с помощью Snackbar
     */
    private fun renderNotification(notify: Notify) {
        // Привязываем вывод снэкбара к боттомбару
        val snackbar = Snackbar.make(vb.coordinatorContainer, notify.message, Snackbar.LENGTH_LONG).setAnchorView(vb.bottombar)
        when(notify) {
            is Notify.ActionMessage -> {
                with(snackbar) {
                    setActionTextColor(getColor(R.color.color_accent_dark))
                    setAction(notify.actionLabel) { notify.actionHandler.invoke() }
                }
            }
            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) { notify.errHandler?.invoke() }
                }
            }
            is Notify.TextMessage -> {
                //
            }
        }
        snackbar.show()
    }

    override fun setupBottombar() {
        vbBottombar.btnLike.setOnClickListener { viewModel.handleLike() }
        vbBottombar.btnBookmark.setOnClickListener { viewModel.handleBookmark() }
        vbBottombar.btnShare.setOnClickListener { viewModel.handleShare() }
        vbBottombar.btnSettings.setOnClickListener { viewModel.handleToggleMenu() }
    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        TODO("Not yet implemented")
    }

    override fun renderSearchPosition(searchPosition: Int) {
        TODO("Not yet implemented")
    }

    override fun clearSearchResult() {
        TODO("Not yet implemented")
    }

    override fun showSearchBar(resultsCount: Int, searchPosition: Int) {
        TODO("Not yet implemented")
    }

    override fun hideSearchBar() {
        TODO("Not yet implemented")
    }

    override fun setupSubmenu() {
        vbSubmenu.switchMode.setOnClickListener { viewModel.handleNightMode() }
        vbSubmenu.btnTextDown.setOnClickListener { viewModel.handleDownText() }
        vbSubmenu.btnTextUp.setOnClickListener { viewModel.handleUpText() }
    }

    override fun setupToolbar() {
        setSupportActionBar(vb.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val logo = if (vb.toolbar.childCount > 2) vb.toolbar.getChildAt(2) as ImageView else null
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP
        val lp = logo?.layoutParams as? Toolbar.LayoutParams
        lp?.let {
            it.width = this.dpToIntPx(40)
            it.height = this.dpToIntPx(40)
            it.marginEnd = this.dpToIntPx(16)
            logo.layoutParams = it
        }
    }

    override fun renderBottombar(data: BottombarData) {
        TODO("Not yet implemented")
    }

    override fun renderSubmenu(data: SubmenuData) {
        TODO("Not yet implemented")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu?.findItem(R.id.action_search)
        val searchView = menuItem?.actionView as? SearchView

        searchView?.setOnSearchClickListener {
            viewModel.handleSearchMode(true)
        }

        searchView?.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                searchView.clearFocus()
                viewModel.handleSearch(p0)
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (!p0.isNullOrBlank()) {
                    viewModel.handleSearch(p0)
                }
                return false
            }

        })

        searchView?.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.handleSearchMode(true)
            }
        }

        if (viewModel.currentState.isSearch) {
            menuItem?.expandActionView()
            searchView?.onActionViewExpanded()
            searchView?.setQuery(viewModel.currentState.searchQuery, true)
        }

        return super.onCreateOptionsMenu(menu)
    }
}

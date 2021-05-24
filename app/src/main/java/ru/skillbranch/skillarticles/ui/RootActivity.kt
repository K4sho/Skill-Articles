package ru.skillbranch.skillarticles.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.ViewModelFactory
import ru.skillbranch.skillarticles.extensions.dpToIntPx

class RootActivity : AppCompatActivity(), IArticleView {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var viewModelFactory: ViewModelProvider.Factory = ViewModelFactory("0")
    private val viewModel: ArticleViewModel by viewModels { viewModelFactory }

    private lateinit var vb: ActivityRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityRootBinding.inflate(layoutInflater)
        setContentView(vb.root)
        setupToolbar()
        setupSubmenu()
        setupBottomBar()

        viewModel.observeState(this) {
            renderUi(it)
        }
        viewModel.observeNotifications(this) {
            renderNotification(it)
        }
    }

    override fun renderUi(state: ArticleState) {
        btn_settings.isChecked = state.isShowMenu
        if (state.isShowMenu) submenu.open() else submenu.close()
        btn_like.isChecked = state.isLike
        btn_bookmark.isChecked = state.isBookmark
        switch_mode.isChecked = state.isDarkMode
        delegate.localNightMode =
                if (state.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        if (state.isBigText) {
            tv_text_content.textSize = 18f
            btn_text_up.isChecked = true
            btn_text_down.isChecked = false
        } else {
            tv_text_content.textSize = 14f
            btn_text_up.isChecked = true
            btn_text_down.isChecked = false
        }

        tv_text_content.text = if (state.isLoadingContent) "loading" else state.content.first() as String

        toolbar.title = state.title ?: "Skill Articles"
        toolbar.subtitle = state.category ?: "loading..."
        if (state.categoryIcon != null) toolbar.logo = getDrawable(state.categoryIcon as Int)
    }

    /**
     * Метод для отрисовки уведомлений для пользователя с помощью Snackbar
     */
    private fun renderNotification(notify: Notify) {
        // Привязываем вывод снэкбара к боттомбару
        val snackbar = Snackbar.make(coordinator_container, notify.message, Snackbar.LENGTH_LONG).setAnchorView(bottombar)
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

    override fun setupBottomBar() {
        btn_like.setOnClickListener { viewModel.handleLike() }
        btn_bookmark.setOnClickListener { viewModel.handleBookmark() }
        btn_share.setOnClickListener { viewModel.handleShare() }
        btn_settings.setOnClickListener { viewModel.handleToggleMenu() }
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
        switch_mode.setOnClickListener { viewModel.handleNightMode() }
        btn_text_down.setOnClickListener { viewModel.handleDownText() }
        btn_text_up.setOnClickListener { viewModel.handleUpText() }
    }

    override fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val logo = if (toolbar.childCount > 2) toolbar.getChildAt(2) as ImageView else null
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP
        val lp = logo?.layoutParams as? Toolbar.LayoutParams
        lp?.let {
            it.width = this.dpToIntPx(40)
            it.height = this.dpToIntPx(40)
            it.marginEnd = this.dpToIntPx(16)
            logo.layoutParams = it
        }
    }

    override fun renderBottomBar(data: BottombarData) {
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

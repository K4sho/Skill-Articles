package ru.skillbranch.skillarticles.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.getSpans
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.custom.markdown.MarkdownBuilder
import ru.skillbranch.skillarticles.ui.custom.spans.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.custom.spans.SearchSpan
import ru.skillbranch.skillarticles.ui.delegates.AttrValue
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.*

class RootActivity : AppCompatActivity(), IArticleView {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var viewModelFactory: ViewModelProvider.Factory = ViewModelFactory(this, "0")
    private val viewModel: ArticleViewModel by viewModels { viewModelFactory }

    private val vb: ActivityRootBinding by viewBinding(ActivityRootBinding::inflate)

    private val vbBottombar
        get() = vb.bottombar

    private val vbSubmenu
        get() = vb.submenu

    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSubmenu()
        setupBottombar()
        setupToolbar()
        setupCopyListener()

        viewModel.observeState(this, ::renderUi)
        viewModel.observeSubState(this, ArticleState::toBottombarData, ::renderBotombar)
        viewModel.observeSubState(this, ArticleState::toSubmenuData, ::renderSubmenu)
        viewModel.observeNotifications(this) {
            renderNotification(it)
        }
    }

    override fun setupCopyListener() {
        vb.tvTextContent.setCopyListener { copy ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied code", copy)
            clipboard.setPrimaryClip(clip)
            viewModel.handleCopyCode()
        }
    }

    override fun renderUi(state: ArticleState) {
        delegate.localNightMode =
            if (state.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        with(vb.tvTextContent) {
            textSize = if (state.isBigText) 18f else 14f
            isLoading = state.content.isEmpty()
            setContent(state.content)
        }

        // bind toolbar
        with(vb.toolbar) {
            title = state.title ?: "loading"
            subtitle = state.category ?: "loading"
            if (state.categoryIcon != null)
                logo = ContextCompat.getDrawable(this@RootActivity, state.categoryIcon as Int)
        }

        if (state.isLoadingContent) return

        if (state.isSearch) {
            renderSearchResult(state.searchResults)
            renderSearchPosition(state.searchPosition, state.searchResults)
        } else clearSearchResult()
    }

    /**
     * Метод для отрисовки уведомлений для пользователя с помощью Snackbar
     */
    private fun renderNotification(notify: Notify) {
        // Привязываем вывод снэкбара к боттомбару
        val snackbar = Snackbar.make(vb.coordinatorContainer, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(vb.bottombar)
        when (notify) {
            is Notify.ActionMessage -> {
                val (_, label, handler) = notify

                with(snackbar) {
                    setActionTextColor(getColor(R.color.color_accent_dark))
                    setAction(label) { handler.invoke() }
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
        with(vbBottombar) {
            btnLike.setOnClickListener { viewModel.handleLike() }
            btnBookmark.setOnClickListener { viewModel.handleBookmark() }
            btnShare.setOnClickListener { viewModel.handleShare() }
            btnSettings.setOnClickListener { viewModel.handleToggleMenu() }

            btnResultUp.setOnClickListener {
                //Что будет, если сбросить фокус вьюхи, у которой нет фокуса?
                // Может быть ничего страшного и не произойдет, но я бы на всякий случай добавил
                // проверку на наличие фокуса, дабы избжать лишних действий во вью.
                if (searchView.hasFocus()) searchView.clearFocus()
                viewModel.handleUpResult()
            }

            btnResultDown.setOnClickListener {
                if (searchView.hasFocus()) searchView.clearFocus()
                viewModel.handleDownResult()
            }

            btnSearchClose.setOnClickListener {
                viewModel.handleSearchMode(false)
                invalidateOptionsMenu()
            }
        }
    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        vb.tvTextContent.renderSearchResult(searchResult)
    }

    override fun renderSearchPosition(searchPosition: Int, searchResult: List<Pair<Int, Int>>) {
        vb.tvTextContent.renderSearchPosition(searchResult.getOrNull(searchPosition))
    }

    override fun clearSearchResult() {
        vb.tvTextContent.clearSearchResult()
    }

    override fun showSearchBar(resultsCount: Int, searchPosition: Int) {
        with(vb.bottombar) {
            setSearchState(true)
            setSearchInfo(resultsCount, searchPosition)
        }
        vb.scroll.setMarginOptionally(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        //Для одного вызова метода необязательно использовать блок with.
        vb.bottombar.setSearchState(false)
        vb.scroll.setMarginOptionally(bottom = dpToIntPx(0))
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
            //лучше получить размер один раз, чтобы избежать лишних вычислений.
            val logoSize = this.dpToIntPx(40)
            it.width = logoSize
            it.height = logoSize
            it.marginEnd = this.dpToIntPx(16)
            logo.layoutParams = it
        }
    }

    override fun renderBotombar(data: BottombarData) {
        with(vbBottombar) {
            btnSettings.isChecked = data.isShowMenu
            btnLike.isChecked = data.isLike
            btnBookmark.isChecked = data.isBookmark
        }

        if (data.isSearch) showSearchBar(data.resultsCount, data.searchPosition)
        else hideSearchBar()
    }

    override fun renderSubmenu(data: SubmenuData) {
        with(vbSubmenu) {
            btnTextDown.isChecked = !data.isBigText
            btnTextUp.isChecked = data.isBigText
            switchMode.isChecked = data.isDarkMode
        }
        if (data.isShowMenu) vb.submenu.open() else vb.submenu.close()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu.findItem(R.id.action_search)
        searchView = (menuItem.actionView as SearchView)
        searchView.queryHint = getString(R.string.article_search_placeholder)
        // restore SearchView
        if (viewModel.currentState.isSearch) {
            menuItem.expandActionView()
            searchView.setQuery(viewModel.currentState.searchQuery, false)
            searchView.requestFocus()
        } else {
            searchView.clearFocus()
        }

        menuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false)
                invalidateOptionsMenu()
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                viewModel.handleSearch(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()) {
                    viewModel.handleSearch(newText)
                }
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        viewModel.saveState()
        super.onSaveInstanceState(outState, outPersistentState)
    }
}

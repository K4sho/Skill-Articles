package ru.skillbranch.skillarticles.viewmodels

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.AppSettings
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format
import java.util.*

class ArticleViewModel(private val articleId: String) : BaseViewModel<ArticleState>(ArticleState()) {
    private val repository = ArticleRepository
    private var menuIsShown = false

    // В блоке инициализации подписываем на изменения
    init {
        subscribeOnDataSource(repository.getAppSettings()) {
            setting, state ->
            state.copy(
                    isDarkMode = setting.isDarkMode,
                    isBigText = setting.isBigText
            )
        }

        subscribeOnDataSource(getArticleData()) {
            article, state ->
            article ?: return@subscribeOnDataSource null
            state.copy(
                author = article.author,
                date = article.date.format(),
                title = article.title,
                category = article.category,
                categoryIcon = article.categoryIcon
            )
        }

        subscribeOnDataSource(getArticleContent()) {
            content, state ->
            content ?: return@subscribeOnDataSource null
            state.copy(
                    isLoadingContent = false,
                    content = content
            )
        }

        subscribeOnDataSource(getArticlePersonalInfo()) {
            info, state ->
            info ?: return@subscribeOnDataSource null
            state.copy(
                    isLike = info.isLike,
                    isBookmark = info.isBookmark
            )
        }
    }

    fun getArticleContent(): LiveData<List<Any>?> {
        return repository.loadArticleContent(articleId)
    }

    fun getArticleData(): LiveData<ArticleData?> {
        return repository.getArticle(articleId)
    }

    fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> {
        return repository.loadArticlePersonalInfo(articleId)
    }

    fun handleNightMode() {
        val mode = currentState.toAppSettings()
        repository.updateSettings(mode.copy(isDarkMode = !mode.isDarkMode))
    }

    fun handleUpText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    fun handleDownText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    fun handleBookmark() {
        val info = currentState.toArticlePersonalInfo()
        repository.updateArticlePersonalInfo(info.copy(isBookmark = !info.isBookmark))
        val msg = if (currentState.isBookmark) Notify.TextMessage("Add to bookmarks") else Notify.TextMessage("Remove from bookmarks")
        notify(msg)
    }

    fun handleLike() {
        // Определим лямбду, обновляющую информацию о статье
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }
        toggleLike()
        val msg = if (currentState.isLike) Notify.TextMessage("Mark is liked")
        else {
            Notify.ActionMessage(
                    "Don`t like it anymore", "No, still like it",
                    toggleLike
            )
        }
        notify(msg)
    }

    fun handleShare() {
        val msg = Notify.ErrorMessage("Share is not implemented", "OK", null)
        notify(msg)
    }

    fun handleToggleMenu() {
        updateState {
            state -> state.copy(isShowMenu = !state.isShowMenu).also { menuIsShown = !state.isShowMenu }
        }
    }

    fun handleSearchMode(isSearch: Boolean) {
        updateState {
            state -> state.copy(isSearch = isSearch)
        }
    }

    fun handleSearch(query: String?) {
        updateState {
            state -> state.copy(searchQuery = query)
        }
    }
}

/**
 * Стэйт(состояние) статьи
 */
data class ArticleState(
        val isAuth: Boolean = false, // пользователь авторизован
        val isLoadingContent: Boolean = true, // контент загружается
        val isLoadingReviews: Boolean = true, // отзывы загружается
        val isLike: Boolean = false, // отмечено как Like
        val isBookmark: Boolean = false, // в закладках
        val isShowMenu: Boolean = false, // отображается меню
        val isBigText: Boolean = false, // шрифт увеличен
        val isDarkMode: Boolean = false, // темный режим
        val isSearch: Boolean = false, // режим поиска
        val searchQuery: String? = null, // поисковый запрос
        val searchResults: List<Pair<Int, Int>> = emptyList(), // результаты поиска (стартовая и конечная позиции)
        val searchPosition: Int = 0, // текущая позиция найденного результата
        val shareLink: String? = null, // ссылка Share
        val title: String? = null, // заголовок статьи
        val category: String? = null, // категория
        val categoryIcon: Any? = null, // иконка категории
        val date: String? = null, // дата публикации
        val author: Any? = null, // автор
        val poster: String? = null,
        val content: List<Any> = emptyList(),
        val reviews: List<Any> = emptyList()
)

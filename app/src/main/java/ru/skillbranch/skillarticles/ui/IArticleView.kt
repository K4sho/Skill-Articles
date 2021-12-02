package ru.skillbranch.skillarticles.ui

import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.BottombarData
import ru.skillbranch.skillarticles.viewmodels.SubmenuData

interface IArticleView {
    /**
     * Отрисовать все вхождения поискового запроса в контент
     */
    fun renderSearchResult(searchResult: List<Pair<Int, Int>>)

    /**
     * Отрисовка и перевод фокуса на текущую позицию
     */
    fun renderSearchPosition(searchPosition: Int, searchResult: List<Pair<Int, Int>>)

    /**
     * Очистка результата поиска
     */
    fun clearSearchResult()

    /**
     * Отображение панели поиска
     */
    fun showSearchBar(resultsCount: Int, searchPosition: Int)

    /**
     * Скрытие панели поиска
     */
    fun hideSearchBar()

    /**
     * Установка listeners для сабменю и настройка внешнего вида
     */
    fun setupSubmenu()

    /**
     * Установка listeners для нижнего меню и настройка внешнего вида
     */
    fun setupBottombar()

    /**
     * Установка listeners для тулбара и настройка внешнего вида
     */
    fun setupToolbar()

    /**
     * Отрисовка данных для боттомбара на слое представления
     */
    fun renderBotombar(data: BottombarData)

    /**
     * Отрисовка сабменю на слое представления
     */
    fun renderSubmenu(data: SubmenuData)

    /**
     * Отрисовка всего UI на слое представления
     */
    fun renderUi(data: ArticleState)

    /**
     *
     */
    fun setupCopyListener()
}
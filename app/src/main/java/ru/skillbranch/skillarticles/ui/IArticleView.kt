package ru.skillbranch.skillarticles.ui

import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.viewmodels.ArticleState

interface IArticleView {
    /**
     * Отрисовать все вхождения поискового запроса в контент
     */
    fun renderSearchResult(searchResult: List<Pair<Int, Int>>)

    /**
     * Отрисовка и перевод фокуса на текущую позицию
     */
    fun renderSearchPosition(searchPosition: Int)

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
    fun setupBottomBar()

    /**
     * Установка listeners для тулбара и настройка внешнего вида
     */
    fun setupToolbar()

    /**
     * Отрисовка данных для боттомбара на слое представления
     */
    fun renderBottomBar(data: BottombarData)

    /**
     * Отрисовка сабменю на слое представления
     */
    fun renderSubmenu(data: SubmenuData)

    /**
     * Отрисовка всего UI на слое представления
     */
    fun renderUi(data: ArticleState)
}
import ru.skillbranch.skillarticles.viewmodels.articles.ArticleItem

interface IArticlesViewModel {
    /**
     * навигация к странице статьи
     **/
    fun navigateToArticle(articleItem: ArticleItem)
    /**
     * добавить/удалить из закладок статью
     **/
    fun checkBookmark(articleItem: ArticleItem, checked: Boolean)
}
package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.view.View
import ru.skillbranch.skillarticles.viewmodels.articles.ArticleItem

class ArticleItemView(context: Context) : View(context) {

    fun bind(
        item: ArticleItem,
        onClick: (ArticleItem) -> Unit,
        onToggleBookmark: (ArticleItem, Boolean) -> Unit
    ) {

    }
}
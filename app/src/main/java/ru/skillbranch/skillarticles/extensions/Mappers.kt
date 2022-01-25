package ru.skillbranch.skillarticles.extensions

import ru.skillbranch.skillarticles.data.AppSettings
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.local.User
import ru.skillbranch.skillarticles.viewmodels.ArticleState

fun ArticleState.toAppSettings(): AppSettings {
    return AppSettings(isDarkMode, isBigText)
}

fun ArticleState.toArticlePersonalInfo(): ArticlePersonalInfo {
    return ArticlePersonalInfo(isLike, isBookmark)
}

fun ArticleState.asMap(): Map<String, Any?> = mapOf(
    "isAuth" to isAuth,
    "isLoadingContent" to isLoadingContent,
    "isLoadingReviews" to isLoadingReviews,
    "isLike" to isLike,
    "isBookmark" to isBookmark,
    "isShowMenu" to isShowMenu,
    "isBigText" to isBigText,
    "isDarkMode" to isDarkMode,
    "isSearch" to isSearch,
    "searchQuery" to searchQuery,
    "searchResults" to searchResults,
    "searchPosition" to searchPosition,
    "shareLink" to shareLink,
    "title" to title,
    "category" to category,
    "categoryIcon" to categoryIcon,
    "date" to date,
    "author" to author,
    "poster" to poster,
    "content" to content,
    "reviews" to reviews,
)

fun User.asMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "avatar" to avatar,
    "rating" to rating,
    "respect" to respect,
    "about" to about
)

fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): List<List<Pair<Int, Int>>> {
    val results = List<MutableList<Pair<Int, Int>>>(bounds.size) { mutableListOf() }

    var lastResult = 0

    bounds@ for ((index, bound) in bounds.withIndex()) {
        var lastIndex = bound.first
        results@ for (result in subList(lastResult, size)) {
            val boundRange = lastIndex..bound.second

            when {
                result.first in boundRange && result.second in boundRange -> {
                    results[index].add(result.first to result.second)
                    lastResult++
                    lastIndex = result.second
                }

                result.first in boundRange && result.second !in boundRange -> {
                    if (result.first != bound.second) {
                        results[index].add(result.first to bound.second)
                    }
                    continue@bounds
                }

                result.first !in boundRange && result.second in boundRange -> {
                    if (bound.first != result.second) {
                        results[index].add(bound.first to result.second)
                    }
                    lastResult++
                    continue@results
                }
            }
        }
    }

    return results
}
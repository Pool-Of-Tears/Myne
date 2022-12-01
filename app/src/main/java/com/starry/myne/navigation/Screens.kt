package com.starry.myne.navigation

const val BOOK_DETAIL_ARG_KEY = "bookId"

sealed class Screens(val route: String) {

    object BookDetailScreen : Screens("book_detail_screen/{$BOOK_DETAIL_ARG_KEY}") {
        fun withBookId(id: String): String {
            return this.route.replace("{$BOOK_DETAIL_ARG_KEY}", id)
        }
    }
}

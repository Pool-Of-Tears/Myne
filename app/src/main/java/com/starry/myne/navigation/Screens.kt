package com.starry.myne.navigation

const val BOOK_DETAIL_ARG_KEY = "bookId"
const val CATEGORY_DETAIL_ARG_KEY = "category"

sealed class Screens(val route: String) {

    object SplashScreen : Screens("splash_screen")

    object BookDetailScreen : Screens("book_detail_screen/{$BOOK_DETAIL_ARG_KEY}") {
        fun withBookId(id: String): String {
            return this.route.replace("{$BOOK_DETAIL_ARG_KEY}", id)
        }
    }

    object CategoryDetailScreen : Screens("category_detail_screen/{$CATEGORY_DETAIL_ARG_KEY}") {
        fun withCategory(category: String): String {
            return this.route.replace("{$CATEGORY_DETAIL_ARG_KEY}", category)
        }
    }
}

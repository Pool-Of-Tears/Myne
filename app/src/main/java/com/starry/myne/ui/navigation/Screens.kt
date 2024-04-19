/**
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.starry.myne.ui.navigation

const val BOOK_ID_ARG_KEY = "bookId"
const val LIBRARY_ITEM_ID_ARG_KEY = "libraryItemId"
const val CATEGORY_DETAIL_ARG_KEY = "category"

sealed class Screens(val route: String) {

    data object BookDetailScreen : Screens("book_detail_screen/{$BOOK_ID_ARG_KEY}") {
        fun withBookId(id: String): String {
            return this.route.replace("{$BOOK_ID_ARG_KEY}", id)
        }
    }

    data object CategoryDetailScreen :
        Screens("category_detail_screen/{$CATEGORY_DETAIL_ARG_KEY}") {
        fun withCategory(category: String): String {
            return this.route.replace("{$CATEGORY_DETAIL_ARG_KEY}", category)
        }
    }

    data object ReaderDetailScreen : Screens("reader_detail_screen/{$LIBRARY_ITEM_ID_ARG_KEY}") {
        fun withLibraryItemId(id: String): String {
            return this.route.replace("{$LIBRARY_ITEM_ID_ARG_KEY}", id)
        }
    }

    data object WelcomeScreen : Screens("welcome_screen")

    data object OSLScreen : Screens("osl_screen")

    data object AboutScreen : Screens("about_screen")
}

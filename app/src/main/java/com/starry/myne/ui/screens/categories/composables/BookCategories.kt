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

package com.starry.myne.ui.screens.categories.composables

import com.starry.myne.R

/**
 * Categories for the books.
 * @param category: Category name
 * @param nameRes: Resource ID for the category name
 */
sealed class BookCategories(val category: String, val nameRes: Int) {

    companion object {
        /**
         * All categories.
         */
        val ALL = arrayOf(
            Animal,
            Children,
            Classics,
            Countries,
            Crime,
            Education,
            Fiction,
            Geography,
            History,
            Literature,
            Law,
            Music,
            Periodicals,
            Psychology,
            Philosophy,
            Religion,
            Romance,
            Science
        )

        /**
         * Get the name resource for the category.
         * @param category: Category name
         * @return Resource ID for the category name
         */
        fun getNameRes(category: String): Int {
            return when (category) {
                "animal" -> R.string.category_animal
                "children" -> R.string.category_children
                "classics" -> R.string.category_classics
                "countries" -> R.string.category_countries
                "crime" -> R.string.category_crime
                "education" -> R.string.category_education
                "fiction" -> R.string.category_fiction
                "geography" -> R.string.category_geography
                "history" -> R.string.category_history
                "literature" -> R.string.category_literature
                "law" -> R.string.category_law
                "music" -> R.string.category_music
                "periodicals" -> R.string.category_periodicals
                "psychology" -> R.string.category_psychology
                "philosophy" -> R.string.category_philosophy
                "religion" -> R.string.category_religion
                "romance" -> R.string.category_romance
                "science" -> R.string.category_science
                else -> R.string.category_animal
            }
        }
    }

    data object Animal : BookCategories("animal", R.string.category_animal)
    data object Children : BookCategories("children", R.string.category_children)
    data object Classics : BookCategories("classics", R.string.category_classics)
    data object Countries : BookCategories("countries", R.string.category_countries)
    data object Crime : BookCategories("crime", R.string.category_crime)
    data object Education : BookCategories("education", R.string.category_education)
    data object Fiction : BookCategories("fiction", R.string.category_fiction)
    data object Geography : BookCategories("geography", R.string.category_geography)
    data object History : BookCategories("history", R.string.category_history)
    data object Literature : BookCategories("literature", R.string.category_literature)
    data object Law : BookCategories("law", R.string.category_law)
    data object Music : BookCategories("music", R.string.category_music)
    data object Periodicals : BookCategories("periodicals", R.string.category_periodicals)
    data object Psychology : BookCategories("psychology", R.string.category_psychology)
    data object Philosophy : BookCategories("philosophy", R.string.category_philosophy)
    data object Religion : BookCategories("religion", R.string.category_religion)
    data object Romance : BookCategories("romance", R.string.category_romance)
    data object Science : BookCategories("science", R.string.category_science)
}
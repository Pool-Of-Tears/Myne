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

package com.starry.myne

import com.google.common.truth.Truth.assertThat
import com.starry.myne.api.models.Author
import com.starry.myne.helpers.book.BookUtils
import kotlinx.coroutines.test.runTest
import org.junit.Test


class BookUtilsTest {
    @Test
    fun `getAuthorsAsString returns expected string with one author`() = runTest {
        val author = Author(name = "Dostoyevsky, Fyodor", birthYear = 0, deathYear = 0)
        assertThat(BookUtils.getAuthorsAsString(listOf(author))).isEqualTo("Fyodor Dostoyevsky")
    }

    @Test
    fun `getAuthorsAsString returns expected string with multiple authors`() = runTest {
        val author1 = Author(name = "Dostoyevsky, Fyodor", birthYear = 0, deathYear = 0)
        val author2 = Author(name = "Orwell, George", birthYear = 0, deathYear = 0)
        assertThat(
            BookUtils.getAuthorsAsString(
                listOf(
                    author1,
                    author2
                )
            )
        ).isEqualTo("Fyodor Dostoyevsky, George Orwell")
    }

    @Test
    fun `getAuthorsAsString returns 'Unknown Author' when the list is empty`() = runTest {
        assertThat(BookUtils.getAuthorsAsString(emptyList())).isEqualTo("Unknown Author")
    }

    @Test
    fun `getLanguagesAsString returns expected string with one language`() = runTest {
        assertThat(BookUtils.getLanguagesAsString(listOf("en"))).isEqualTo("English")
    }

    @Test
    fun `getLanguagesAsString returns expected string with multiple languages`() = runTest {
        assertThat(BookUtils.getLanguagesAsString(listOf("en", "fr"))).isEqualTo("English, French")
    }

    @Test
    fun `getSubjectsAsString returns expected string with less than limit subjects`() = runTest {
        val subjects = listOf("Historical fiction", "Robinsonades")
        assertThat(
            BookUtils.getSubjectsAsString(
                subjects,
                3
            )
        ).isEqualTo("Historical fiction, Robinsonades")
    }

    @Test
    fun `getSubjectsAsString returns expected string with more than limit subjects`() = runTest {
        val subjects =
            listOf(
                "Adventure stories",
                "Russia -- Social conditions -- 1801-1917 -- Fiction",
                "Pirates",
                "Shipwrecks",
                "Survival"
            )
        assertThat(
            BookUtils.getSubjectsAsString(
                subjects,
                3
            )
        ).isEqualTo("Adventure stories, Russia, Social conditions")
    }

}
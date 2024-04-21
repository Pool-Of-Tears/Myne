package com.starry.myne

import com.google.common.truth.Truth.assertThat
import com.starry.myne.api.models.Author
import com.starry.myne.helpers.book.BookUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class BookUtilsTest {
    @Test
    fun `getAuthorsAsString returns expected string with one author`() = runTest {
        val author = Author("Dostoyevsky, Fyodor", 0, 0)
        assertThat(BookUtils.getAuthorsAsString(listOf(author))).isEqualTo("Fyodor Dostoyevsky")
    }

    @Test
    fun `getAuthorsAsString returns expected string with multiple authors`() = runTest {
        val author1 = Author("Dostoyevsky, Fyodor", 0, 0)
        val author2 = Author("Orwell, George", 0, 0)
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
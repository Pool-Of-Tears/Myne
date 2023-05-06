/*
Copyright 2022 - 2023 Stɑrry Shivɑm

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.starry.myne.ui.screens.categories.viewmodels


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.others.BookLanguage
import com.starry.myne.others.Paginator
import com.starry.myne.repo.BookRepository
import com.starry.myne.repo.models.Book
import com.starry.myne.repo.models.BookSet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategorisedBooksState(
    val isLoading: Boolean = false,
    val items: List<Book> = emptyList(),
    val error: String? = null,
    val endReached: Boolean = false,
    val page: Long = 1L
)

@HiltViewModel
class CategoryViewModel @Inject constructor(private val booksApi: BookRepository) : ViewModel() {
    companion object {
        val CATEGORIES_ARRAY =
            listOf(
                "animal",
                "children",
                "classics",
                "countries",
                "crime",
                "education",
                "fiction",
                "geography",
                "history",
                "literature",
                "law",
                "music",
                "periodicals",
                "psychology",
                "philosophy",
                "religion",
                "romance",
                "science",
            )
    }

    private lateinit var pagination: Paginator<Long, BookSet>

    var state by mutableStateOf(CategorisedBooksState())

    private val _language: MutableState<BookLanguage> =
        mutableStateOf(BookLanguage.AllBooks)
    val language: State<BookLanguage> = _language

    fun loadBookByCategory(category: String) {
        if (!this::pagination.isInitialized) {
            pagination = Paginator(
                initialPage = state.page,
                onLoadUpdated = {
                    state = state.copy(isLoading = it)
                },
                onRequest = { nextPage ->
                    try {
                        booksApi.getBooksByCategory(category, nextPage, language.value)
                    } catch (exc: Exception) {
                        Result.failure(exc)
                    }
                },
                getNextPage = {
                    state.page + 1L
                },
                onError = {
                    state = state.copy(error = it?.localizedMessage ?: "unknown-error")
                },
                onSuccess = { bookSet, newPage ->
                    /**
                     * usually bookSet.books is not nullable and API simply returns empty list
                     * when browsing books all books (i.e. without passing language parameter)
                     * however, when browsing by language it returns a response which looks like
                     * this: {"detail": "Invalid page."}. Hence the [BookSet] attributes become
                     * null in this case and can cause crashes.
                     */
                    val books = if (bookSet.books != null) {
                        bookSet.books.filter { it.formats.applicationepubzip != null }
                    } else {
                        emptyList()
                    }
                    state = state.copy(
                        items = (state.items + books),
                        page = newPage,
                        endReached = books.isEmpty()
                    )
                }
            )
        }

        loadNextItems()
    }

    fun loadNextItems() {
        viewModelScope.launch {
            pagination.loadNextItems()
        }
    }

    fun reloadItems() {
        pagination.reset()
        state = CategorisedBooksState()
        loadNextItems()
    }

    fun changeLanguage(language: BookLanguage) {
        _language.value = language
        reloadItems()
    }
}
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

package com.starry.myne.ui.screens.categories.viewmodels


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.api.BookAPI
import com.starry.myne.api.models.Book
import com.starry.myne.api.models.BookSet
import com.starry.myne.helpers.Constants
import com.starry.myne.helpers.Paginator
import com.starry.myne.helpers.PreferenceUtil
import com.starry.myne.helpers.book.BookLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
class CategoryViewModel @Inject constructor(
    private val booksApi: BookAPI,
    private val preferenceUtil: PreferenceUtil
) : ViewModel() {

    private lateinit var pagination: Paginator<Long, BookSet>

    var state by mutableStateOf(CategorisedBooksState())

    private val _language: MutableState<BookLanguage> = mutableStateOf(getPreferredLanguage())
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
                        // If response is cached it will show stuff immediately while
                        // we are navigating and navigation animation is still showing up
                        // which could cause flickering, so we add artificial delay of
                        // 400ms as it doesn't do any harm and improves the overall UX
                        delay(400)
                        booksApi.getBooksByCategory(category, nextPage, language.value)
                    } catch (exc: Exception) {
                        Result.failure(exc)
                    }
                },
                getNextPage = {
                    state.page + 1L
                },
                onError = {
                    state = state.copy(error = it?.localizedMessage ?: Constants.UNKNOWN_ERR)
                },
                onSuccess = { bookSet, newPage ->
                    val books = bookSet.books.filter { it.formats.applicationepubzip != null }
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
        preferenceUtil.putString(PreferenceUtil.PREFERRED_BOOK_LANG_STR, language.isoCode)
        reloadItems()
    }

    private fun getPreferredLanguage(): BookLanguage {
        val isoCode = preferenceUtil.getString(
            PreferenceUtil.PREFERRED_BOOK_LANG_STR,
            BookLanguage.AllBooks.isoCode
        )
        return BookLanguage.getAllLanguages().find { it.isoCode == isoCode }
            ?: BookLanguage.AllBooks
    }
}
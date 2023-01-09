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

package com.starry.myne.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.api.BooksApi
import com.starry.myne.api.models.Book
import com.starry.myne.others.PaginatorImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AllBooksState(
    val isLoading: Boolean = false,
    val items: List<Book> = emptyList(),
    val error: String? = null,
    val endReached: Boolean = false,
    val page: Long = 1L
)

data class TopBarState(
    val searchText: String = "",
    val isSearchBarVisible: Boolean = false,
    val isSearching: Boolean = false,
    val searchResults: List<Book> = emptyList()
)

sealed class UserAction {
    object SearchIconClicked : UserAction()
    object CloseIconClicked : UserAction()
    data class TextFieldInput(val text: String) : UserAction()
}

class HomeViewModel : ViewModel() {
    var allBooksState by mutableStateOf(AllBooksState())
    var topBarState by mutableStateOf(TopBarState())

    private val paginator = PaginatorImpl(
        initialPage = allBooksState.page,
        onLoadUpdated = {
            allBooksState = allBooksState.copy(isLoading = it)
        },
        onRequest = { nextPage ->
            BooksApi.getAllBooks(nextPage)
        },
        getNextPage = {
            allBooksState.page + 1L
        },
        onError = {
            allBooksState = allBooksState.copy(error = it?.localizedMessage)
        },
        onSuccess = { bookSet, newPage ->
            allBooksState = allBooksState.copy(
                items = (allBooksState.items + bookSet.books),
                page = newPage,
                endReached = bookSet.books.isEmpty()
            )
        }
    )

    init {
        loadNextItems()
    }

    fun loadNextItems() {
        viewModelScope.launch {
            paginator.loadNextItems()
        }
    }

    private var searchJob: Job? = null

    fun onAction(userAction: UserAction) {
        when (userAction) {
            UserAction.CloseIconClicked -> {
                topBarState = topBarState.copy(isSearchBarVisible = false)
            }
            UserAction.SearchIconClicked -> {
                topBarState = topBarState.copy(isSearchBarVisible = true)
            }
            is UserAction.TextFieldInput -> {
                topBarState = topBarState.copy(searchText = userAction.text)
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    if (userAction.text.isNotBlank()) {
                        topBarState = topBarState.copy(isSearching = true)
                    }
                    delay(500L)
                    searchBooks(userAction.text)
                }
            }
        }
    }

    private suspend fun searchBooks(query: String) {
        val bookSet = BooksApi.searchBooks(query)
        topBarState =
            topBarState.copy(searchResults = bookSet.getOrNull()!!.books, isSearching = false)
    }
}
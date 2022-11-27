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

data class ScreenState(
    val isLoading: Boolean = false,
    val items: List<Book> = emptyList(),
    val error: String? = null,
    val endReached: Boolean = false,
    val page: Long = 1L
)

data class TopBarState(
    val searchText: String = "",
    val isSearchBarVisible: Boolean = false,
    val isSortMenuVisible: Boolean = false,
    val isSearching: Boolean = false,
    val searchResults: List<Book> = emptyList()
)

sealed class UserAction {
    object SearchIconClicked : UserAction()
    object CloseIconClicked : UserAction()
    object SortIconClicked : UserAction()
    object SortMenuDismiss : UserAction()
    data class TextFieldInput(val text: String) : UserAction()
}

class HomeViewModel : ViewModel() {
    private val bookApi = BooksApi()
    var state by mutableStateOf(ScreenState())
    var topBarState by mutableStateOf(TopBarState())

    private val paginator = PaginatorImpl(
        initialPage = state.page,
        onLoadUpdated = {
            state = state.copy(isLoading = it)
        },
        onRequest = { nextPage ->
            bookApi.getAllBooks(nextPage)
        },
        getNextPage = {
            state.page + 1L
        },
        onError = {
            state = state.copy(error = it?.localizedMessage)
        },
        onSuccess = { bookSet, newPage ->
            state = state.copy(
                items = (state.items + bookSet.books),
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
            UserAction.SortIconClicked -> {
                topBarState = topBarState.copy(isSortMenuVisible = true)
            }
            UserAction.SortMenuDismiss -> {
                topBarState = topBarState.copy(isSortMenuVisible = false)
            }
        }
    }

    private suspend fun searchBooks(query: String) {
        val bookSet = bookApi.searchBooks(query)
        topBarState =
            topBarState.copy(searchResults = bookSet.getOrNull()!!.books, isSearching = false)
    }
}
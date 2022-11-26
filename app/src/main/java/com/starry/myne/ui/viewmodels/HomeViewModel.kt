package com.starry.myne.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.api.BooksApi
import com.starry.myne.api.models.Book
import com.starry.myne.others.PaginatorImpl
import kotlinx.coroutines.launch

data class ScreenState(
    val isLoading: Boolean = false,
    val items: List<Book> = emptyList(),
    val error: String? = null,
    val endReached: Boolean = false,
    val page: Long = 1L
)

class HomeViewModel : ViewModel() {
    private val bookApi = BooksApi()
    var state by mutableStateOf(ScreenState())

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
}
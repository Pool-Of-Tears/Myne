package com.starry.myne.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.api.BooksApi
import com.starry.myne.api.models.BookSet
import com.starry.myne.api.models.ExtraInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ScreenState(
    val isLoading: Boolean = true,
    val item: BookSet = BookSet(0, null, null, emptyList()),
    val extraInfo: ExtraInfo = ExtraInfo()
)

class BookDetailViewModel : ViewModel() {
    var state by mutableStateOf(ScreenState())

    fun getBookDetails(bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val bookItem = BooksApi.getBookById(bookId).getOrNull()!!
            val extraInfo = BooksApi.getExtraInfo(bookItem.books.first().title)
            state = if (extraInfo != null) {
                state.copy(isLoading = false, item = bookItem, extraInfo = extraInfo)
            } else {
                state.copy(isLoading = false, item = bookItem)
            }
        }
    }
}
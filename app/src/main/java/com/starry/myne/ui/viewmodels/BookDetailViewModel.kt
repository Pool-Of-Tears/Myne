package com.starry.myne.ui.viewmodels

import com.starry.myne.api.models.BookSet

data class ScreenState(
    val isLoading: Boolean = false,
    val item: BookSet = BookSet(0, null, null, emptyList())
)

class BookDetailViewModel {
}
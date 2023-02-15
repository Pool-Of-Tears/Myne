package com.starry.myne.ui.screens.reader.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.database.LibraryDao
import com.starry.myne.epub.createEpubBook
import com.starry.myne.epub.models.EpubBook
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ReaderScreenState(
    val isLoading: Boolean = true,
    val epubBook: EpubBook? = null,
)

@HiltViewModel
class ReaderViewModel @Inject constructor(private val libraryDao: LibraryDao) : ViewModel() {
    var state by mutableStateOf(ReaderScreenState())

    fun loadEpubBook(bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val libraryItem = libraryDao.getItemById(bookId.toInt())
            val epubBook = createEpubBook(libraryItem!!.filePath)
            // Added some delay to avoid choppy animation.
            delay(200L)
            state = state.copy(isLoading = false, epubBook = epubBook)
        }
    }
}
package com.starry.myne.ui.screens.reader.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.database.library.LibraryDao
import com.starry.myne.database.reader.ReaderDao
import com.starry.myne.database.reader.ReaderItem
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
    val readerItem: ReaderItem? = null
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val libraryDao: LibraryDao, private val readerDao: ReaderDao
) : ViewModel() {
    var state by mutableStateOf(ReaderScreenState())

    fun loadEpubBook(bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val libraryItem = libraryDao.getItemById(bookId.toInt())
            val readerItem = readerDao.getReaderItem(bookId.toInt())
            val epubBook = createEpubBook(libraryItem!!.filePath)
            // Added some delay to avoid choppy animation.
            delay(200L)
            state = state.copy(isLoading = false, epubBook = epubBook, readerItem = readerItem)
        }
    }

    fun updateReaderProgress(bookId: Int, chapterIndex: Int, chapterOffset: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (readerDao.getReaderItem(bookId) != null && chapterIndex != state.epubBook?.chapters!!.size - 1) {
                readerDao.update(bookId, chapterIndex, chapterOffset)
            } else if (chapterIndex == state.epubBook?.chapters!!.size - 1) {
                /*
                 if  the user has reached last chapter, delete this book
                 from reader database instead of saving it's progress
               */
                readerDao.getReaderItem(bookId)?.let { readerDao.delete(it.bookId) }
            } else {
                readerDao.insert(readerItem = ReaderItem(bookId, chapterIndex, chapterOffset))
            }
        }
    }
}
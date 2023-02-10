package com.starry.myne.ui.screens.reader.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.api.BooksApi
import com.starry.myne.database.LibraryDao
import com.starry.myne.epub.createEpubBook
import com.starry.myne.epub.models.EpubBook
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


data class EbookData(
    val coverImage: String?,
    val title: String,
    val author: String,
    val epubBook: EpubBook,
)

data class EpubDetailScreenState(
    val isLoading: Boolean = true, val ebookData: EbookData? = null, val error: String? = null
)

@HiltViewModel
class ReaderDetailViewModel @Inject constructor(private val libraryDao: LibraryDao) : ViewModel() {

    companion object Errors {
        const val FILE_NOT_FOUND = "epub_file_not_found"
    }

    var state by mutableStateOf(EpubDetailScreenState())
    fun getEbookData(bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // build EbookData.
            val libraryItem = libraryDao.getItemById(bookId.toInt())
            state = if (libraryItem!!.fileExist()) {
                val coverImage: String? = try {
                    BooksApi.getExtraInfo(libraryItem.title)?.coverImage
                } catch (exc: Exception) {
                    println(exc.localizedMessage)
                    null
                }
                state.copy(
                    isLoading = false, ebookData = EbookData(
                        coverImage,
                        libraryItem.title,
                        libraryItem.authors,
                        createEpubBook(libraryItem.filePath)
                    )
                )
            } else {
                state.copy(isLoading = false, error = FILE_NOT_FOUND)
            }
        }
    }
}
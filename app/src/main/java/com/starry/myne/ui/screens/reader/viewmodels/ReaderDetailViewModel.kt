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
import com.starry.myne.repo.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import javax.inject.Inject


data class EbookData(
    val coverImage: String?,
    val title: String,
    val author: String,
    val epubBook: EpubBook,
)

data class ReaderDetailScreenState(
    val isLoading: Boolean = true,
    val ebookData: EbookData? = null,
    val error: String? = null,
    val readerItem: ReaderItem? = null
)

@HiltViewModel
class ReaderDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val libraryDao: LibraryDao,
    private val readerDao: ReaderDao
) : ViewModel() {

    companion object Errors {
        const val FILE_NOT_FOUND = "epub_file_not_found"
    }

    var state by mutableStateOf(ReaderDetailScreenState())
    fun loadEbookData(bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // build EbookData.
            val libraryItem = libraryDao.getItemById(bookId.toInt())!!
            state = try {
                val coverImage: String? = try {
                    bookRepository.getExtraInfo(libraryItem.title)?.coverImage
                } catch (exc: Exception) {
                    null
                }
                state.copy(
                    isLoading = false, ebookData = EbookData(
                        coverImage,
                        libraryItem.title,
                        libraryItem.authors,
                        createEpubBook(libraryItem.filePath)
                    ), readerItem = readerDao.getReaderItem(bookId.toInt())
                )

            } catch (exc: FileNotFoundException) {
                state.copy(isLoading = false, error = FILE_NOT_FOUND)
            }
        }
    }
}
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


package com.starry.myne.ui.screens.reader.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.database.library.LibraryDao
import com.starry.myne.database.reader.ReaderDao
import com.starry.myne.database.reader.ReaderData
import com.starry.myne.epub.EpubParser
import com.starry.myne.epub.models.EpubBook
import com.starry.myne.repo.BookRepository
import com.starry.myne.utils.NetworkObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class EbookData(
    val coverImage: String?,
    val title: String,
    val authors: String,
    val epubBook: EpubBook,
)

data class ReaderDetailScreenState(
    val isLoading: Boolean = true,
    val ebookData: EbookData? = null,
    val error: String? = null,
)

@HiltViewModel
class ReaderDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val libraryDao: LibraryDao,
    private val readerDao: ReaderDao,
    private val epubParser: EpubParser
) : ViewModel() {

    var state by mutableStateOf(ReaderDetailScreenState())

    val readerData: Flow<ReaderData?>?
        get() = _readerData
    private var _readerData: Flow<ReaderData?>? = null

    fun loadEbookData(bookId: String, networkStatus: NetworkObserver.Status) {
        viewModelScope.launch(Dispatchers.IO) {
            // Library item is not null as this screen is only accessible from the library.
            val libraryItem = libraryDao.getItemById(bookId.toInt())!!
            // Get reader data if it exists.
            _readerData = readerDao.getReaderDataAsFlow(bookId.toInt())
            val coverImage: String? = try {
                if (networkStatus == NetworkObserver.Status.Available) bookRepository.getExtraInfo(
                    libraryItem.title
                )?.coverImage else null
            } catch (exc: Exception) {
                null
            }
            // Create EbookData object.
            val ebookData = EbookData(
                coverImage = coverImage,
                title = libraryItem.title,
                authors = libraryItem.authors,
                epubBook = epubParser.createEpubBook(libraryItem.filePath)
            )
            delay(500) // Add delay to avoid flickering.
            state = state.copy(isLoading = false, ebookData = ebookData)
        }
    }
}
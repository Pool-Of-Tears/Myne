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

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.api.BookAPI
import com.starry.myne.database.library.LibraryDao
import com.starry.myne.database.reader.ReaderDao
import com.starry.myne.database.reader.ReaderData
import com.starry.myne.epub.EpubParser
import com.starry.myne.epub.models.EpubBook
import com.starry.myne.helpers.NetworkObserver
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
    private val bookAPI: BookAPI,
    private val libraryDao: LibraryDao,
    private val readerDao: ReaderDao,
    private val epubParser: EpubParser
) : ViewModel() {

    var state by mutableStateOf(ReaderDetailScreenState())

    val readerData: Flow<ReaderData?>?
        get() = _readerData
    private var _readerData: Flow<ReaderData?>? = null

    fun loadEbookData(libraryItemId: String, networkStatus: NetworkObserver.Status) {
        viewModelScope.launch(Dispatchers.IO) {
            // Library item is not null as this screen is only accessible from the library.
            val libraryItem = libraryDao.getItemById(libraryItemId.toInt())!!
            // Get reader data if it exists.
            _readerData = readerDao.getReaderDataAsFlow(libraryItemId.toInt())
            val coverImage: String? = try {
                if (!libraryItem.isExternalBook
                    && networkStatus == NetworkObserver.Status.Available
                ) bookAPI.getExtraInfo(libraryItem.title)?.coverImage else null
            } catch (exc: Exception) {
                null
            }
            // Gutenberg for some reason don't include proper navMap in chinese books
            // in toc, so we need to parse the book based on spine, instead of toc.
            // This is a workaround for internal chinese books.
            var epubBook = epubParser.createEpubBook(libraryItem.filePath)
            if (epubBook.language == "zh" && !libraryItem.isExternalBook) {
                Log.d("ReaderDetailViewModel", "Parsing book without toc for chinese book.")
                epubBook = epubParser.createEpubBook(libraryItem.filePath, shouldUseToc = false)
            }
            // Create ebook data.
            val ebookData = EbookData(
                coverImage = coverImage,
                title = libraryItem.title,
                authors = libraryItem.authors,
                epubBook = epubBook
            )
            delay(500) // Add delay to avoid flickering.
            state = state.copy(isLoading = false, ebookData = ebookData)
        }
    }
}
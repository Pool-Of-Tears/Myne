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


package com.starry.myne.ui.screens.reader.detail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.api.BookAPI
import com.starry.myne.database.library.LibraryDao
import com.starry.myne.database.progress.ProgressDao
import com.starry.myne.database.progress.ProgressData
import com.starry.myne.epub.EpubParser
import com.starry.myne.epub.models.EpubChapter
import com.starry.myne.helpers.NetworkObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ReaderDetailScreenState(
    val isLoading: Boolean = true,
    val title: String = "",
    val authors: String = "",
    val coverImage: Any? = null,
    val chapters: List<EpubChapter> = emptyList(),
    val hasProgressSaved: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ReaderDetailViewModel @Inject constructor(
    private val bookAPI: BookAPI,
    private val libraryDao: LibraryDao,
    private val progressDao: ProgressDao,
    private val epubParser: EpubParser
) : ViewModel() {

    var state by mutableStateOf(ReaderDetailScreenState())

    var progressData: Flow<ProgressData>? = null
        private set

    fun loadEbookData(libraryItemId: String, networkStatus: NetworkObserver.Status) {
        viewModelScope.launch(Dispatchers.IO) {
            val libraryItem = libraryDao.getItemById(libraryItemId.toInt())
            // Check if library item exists.
            if (libraryItem == null) {
                state = state.copy(isLoading = false, error = "Library item not found.")
                return@launch
            }
            // Get progress data for the current book.
            progressData = progressDao.getReaderDataAsFlow(libraryItemId.toInt())
            // Fetch cover image from google books api if available.
            val coverImage: String? = try {
                if (!libraryItem.isExternalBook
                    && networkStatus == NetworkObserver.Status.Available
                ) bookAPI.getExtraInfo(libraryItem.title)?.coverImage else null
            } catch (exc: Exception) {
                Log.e("ReaderDetailViewModel", "Failed to fetch cover image.", exc)
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
            state = state.copy(
                title = libraryItem.title,
                authors = libraryItem.authors,
                coverImage = coverImage ?: epubBook.coverImage,
                chapters = epubBook.chapters,
                hasProgressSaved = progressData != null
            )
            delay(350) // Small delay for smooth transition.
            state = state.copy(isLoading = false)
        }
    }
}
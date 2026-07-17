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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.database.library.LibraryDao
import com.starry.myne.database.reader.ReaderProgress
import com.starry.myne.database.reader.ReaderDao
import com.starry.myne.epub.EpubParser
import com.starry.myne.epub.models.EpubChapter
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
    val error: String? = null,
)

@HiltViewModel
class ReaderDetailViewModel @Inject constructor(
    private val libraryDao: LibraryDao,
    private val readerDao: ReaderDao,
    private val epubParser: EpubParser
) : ViewModel() {

    var state by mutableStateOf(ReaderDetailScreenState())

    var readerProgress: Flow<ReaderProgress?>? = null
        private set

    fun loadEbookData(libraryItemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val libraryItem = libraryDao.getItemById(libraryItemId.toInt())
            // Check if library item exists.
            if (libraryItem == null) {
                state = state.copy(isLoading = false, error = "Library item not found.")
                return@launch
            }
            // Get progress data for the current book.
            readerProgress = readerDao.getReaderDataAsFlow(libraryItemId.toInt())

            // Gutenberg for some reason don't include proper navMap for chinese books
            // in toc file, so we need to parse the book based on spine, instead of toc.
            // This is special case for Chinese books.
            val isInternalChineseBook =
                !libraryItem.isImported && epubParser.peekLanguage(libraryItem.filePath) == "zh"
            val shouldUseToc = !isInternalChineseBook
            val epubBook = epubParser.createEpubBook(libraryItem.filePath, shouldUseToc)

            state = state.copy(
                title = libraryItem.title,
                authors = libraryItem.authors,
                coverImage = epubBook.coverImage,
                chapters = epubBook.chapters,
            )
            delay(350) // Small delay for smooth transition.
            state = state.copy(isLoading = false)
        }
    }
}

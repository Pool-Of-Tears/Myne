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
import com.starry.myne.epub.models.EpubChapter
import com.starry.myne.helpers.PreferenceUtil
import com.starry.myne.ui.screens.reader.others.ReaderFont
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.FileInputStream
import javax.inject.Inject


data class ReaderScreenState(
    val isLoading: Boolean = true,
    val shouldShowLoader: Boolean = false,
    val showReaderMenu: Boolean = false,
    val fontSize: Int = 18,
    val fontFamily: ReaderFont = ReaderFont.System,
    val epubBook: EpubBook? = null,
    val readerData: ReaderData? = null
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val libraryDao: LibraryDao,
    private val readerDao: ReaderDao,
    private val preferenceUtil: PreferenceUtil,
    private val epubParser: EpubParser
) : ViewModel() {

    var state by mutableStateOf(
        ReaderScreenState(
            fontFamily = getFontFamily(),
            fontSize = getFontSize()
        )
    )

    private val _chapterScrolledPercent = MutableStateFlow(0f)
    val chapterScrolledPercent: StateFlow<Float> = _chapterScrolledPercent

    private val _visibleChapterIndex = MutableStateFlow(0)
    val visibleChapterIndex: StateFlow<Int> = _visibleChapterIndex

    private val _currentChapter = MutableStateFlow(
        state.epubBook?.chapters?.getOrNull(_visibleChapterIndex.value)
    )
    val currentChapter: StateFlow<EpubChapter?> = _currentChapter

    init {
        viewModelScope.launch {
            _visibleChapterIndex.collect { index ->
                _currentChapter.value = state.epubBook?.chapters?.getOrNull(index)
            }
        }
    }

    fun loadEpubBook(libraryItemId: Int, onLoaded: (ReaderScreenState) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val libraryItem = libraryDao.getItemById(libraryItemId)
            state = state.copy(shouldShowLoader = !epubParser.isBookCached(libraryItem!!.filePath))
            val readerData = readerDao.getReaderData(libraryItemId)
            // parse and create epub book
            var epubBook = epubParser.createEpubBook(libraryItem.filePath)
            // Gutenberg for some reason don't include proper navMap in chinese books
            // in toc, so we need to parse the book based on spine, instead of toc.
            // This is a workaround for internal chinese books.
            if (epubBook.language == "zh" && !libraryItem.isExternalBook) {
                epubBook = epubParser.createEpubBook(libraryItem.filePath, shouldUseToc = false)
            }
            state = state.copy(epubBook = epubBook, readerData = readerData)
            onLoaded(state)
            // Added some delay to avoid choppy animation.
            if (state.shouldShowLoader) {
                delay(200L)
            }
            state = state.copy(isLoading = false)
        }
    }

    fun loadEpubBookExternal(fileStream: FileInputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            fileStream.use { fis ->
                // parse and create epub book
                val epubBook = epubParser.createEpubBook(fis, shouldUseToc = false)
                state = state.copy(epubBook = epubBook)
                // Added some delay to avoid choppy animation.
                delay(200L)
                state = state.copy(isLoading = false)
            }
        }
    }

    fun updateReaderProgress(libraryItemId: Int, chapterIndex: Int, chapterOffset: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val readerData = readerDao.getReaderData(libraryItemId)
            // if the user is not on last chapter, save the progress.
            if (readerData != null && chapterIndex != state.epubBook?.chapters!!.size - 1) {
                val newReaderData = readerData.copy(
                    lastChapterIndex = chapterIndex,
                    lastChapterOffset = chapterOffset,
                    lastReadTime = System.currentTimeMillis()
                )
                newReaderData.id = readerData.id
                readerDao.update(newReaderData)
            } else if (chapterIndex == state.epubBook?.chapters!!.size - 1) {
                // if the user has reached last chapter, delete this book
                // from reader database instead of saving it's progress.
                readerData?.let { readerDao.delete(it.libraryItemId) }
            } else {
                // if the user is reading this book for the first time, save the progress.
                readerDao.insert(
                    readerData = ReaderData(
                        libraryItemId,
                        chapterIndex,
                        chapterOffset
                    )
                )
            }
        }
    }

    fun setChapterScrollPercent(percent: Float) {
        _chapterScrolledPercent.value = percent
    }

    fun setVisibleChapterIndex(index: Int) {
        _visibleChapterIndex.value = index
    }

    fun toggleReaderMenu() {
        state = state.copy(showReaderMenu = !state.showReaderMenu)
    }

    fun hideReaderInfo() {
        state = state.copy(showReaderMenu = false)
    }

    fun setFontFamily(font: ReaderFont) {
        preferenceUtil.putString(PreferenceUtil.READER_FONT_STYLE_STR, font.id)
        state = state.copy(fontFamily = font)
    }

    fun getFontFamily(): ReaderFont {
        return ReaderFont.getAllFonts().find {
            it.id == preferenceUtil.getString(
                PreferenceUtil.READER_FONT_STYLE_STR,
                ReaderFont.System.id
            )
        }!!
    }

    fun setFontSize(newValue: Int) {
        preferenceUtil.putInt(PreferenceUtil.READER_FONT_SIZE_INT, newValue)
        state = state.copy(fontSize = newValue)
    }

    private fun getFontSize() = preferenceUtil.getInt(PreferenceUtil.READER_FONT_SIZE_INT, 100)

}
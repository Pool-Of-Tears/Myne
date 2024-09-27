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


package com.starry.myne.ui.screens.reader.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.database.library.LibraryDao
import com.starry.myne.database.progress.ProgressDao
import com.starry.myne.database.progress.ProgressData
import com.starry.myne.epub.EpubParser
import com.starry.myne.epub.models.EpubChapter
import com.starry.myne.epub.models.EpubImage
import com.starry.myne.helpers.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.FileInputStream
import javax.inject.Inject


data class ReaderScreenState(
    // Screen state
    val isLoading: Boolean = true,
    val shouldShowLoader: Boolean = false,
    val showReaderMenu: Boolean = false,
    val currentChapterIndex: Int = 0,
    val currentChapter: EpubChapter = EpubChapter("", "", "", ""),
    val chapterScrollPercent: Float = 0f,
    // Book data
    val title: String = "",
    val chapters: List<EpubChapter> = emptyList(),
    val images: List<EpubImage> = emptyList(),
    // Reader data
    val hasProgressSaved: Boolean = false,
    val lastChapterIndex: Int = 0,
    val lastChapterOffset: Int = 0,
    // Typography
    val fontSize: Int = 100,
    val fontFamily: ReaderFont = ReaderFont.System,
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val libraryDao: LibraryDao,
    private val progressDao: ProgressDao,
    private val preferenceUtil: PreferenceUtil,
    private val epubParser: EpubParser
) : ViewModel() {

    // Mutable state flow to update the state.
    private val _state = MutableStateFlow(ReaderScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                fontSize = preferenceUtil.getInt(PreferenceUtil.READER_FONT_SIZE_INT, 100)
            )
            _state.value = _state.value.copy(
                fontFamily = ReaderFont.getFontById(
                    preferenceUtil.getString(
                        PreferenceUtil.READER_FONT_STYLE_STR,
                        ReaderFont.System.id
                    )!!
                )
            )
            // Collect the state to update the current chapter.
            _state.collect {
                if (state.value.isLoading) return@collect
                if (state.value.chapters.isEmpty()) return@collect
                _state.value = _state.value.copy(
                    currentChapter = _state.value.chapters[_state.value.currentChapterIndex]
                )
            }
        }
    }

    fun loadEpubBook(libraryItemId: Int, onLoaded: (ReaderScreenState) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val libraryItem = libraryDao.getItemById(libraryItemId)
            _state.value = _state.value.copy(
                shouldShowLoader = !epubParser.isBookCached(libraryItem!!.filePath)
            )
            val readerData = progressDao.getReaderData(libraryItemId)
            // parse and create epub book
            var epubBook = epubParser.createEpubBook(libraryItem.filePath)
            // Gutenberg for some reason don't include proper navMap in chinese books
            // in toc, so we need to parse the book based on spine, instead of toc.
            // This is a workaround for internal chinese books.
            if (epubBook.language == "zh" && !libraryItem.isExternalBook) {
                epubBook = epubParser.createEpubBook(libraryItem.filePath, shouldUseToc = false)
            }
            _state.value = _state.value.copy(
                title = libraryItem.title,
                chapters = epubBook.chapters,
                images = epubBook.images,
                hasProgressSaved = readerData != null,
                lastChapterIndex = readerData?.lastChapterIndex ?: 0,
                lastChapterOffset = readerData?.lastChapterOffset ?: 0,
                currentChapterIndex = readerData?.lastChapterIndex ?: 0
            )
            onLoaded(state.value)
            // Added some delay to avoid choppy animation.
            if (state.value.shouldShowLoader) {
                delay(200L)
            }
            _state.value = _state.value.copy(
                isLoading = false,
                shouldShowLoader = false
            )
        }
    }

    fun loadEpubBookExternal(fileStream: FileInputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            fileStream.use { fis ->
                // parse and create epub book
                val epubBook = epubParser.createEpubBook(fis, shouldUseToc = false)
                _state.value = _state.value.copy(
                    title = epubBook.title,
                    chapters = epubBook.chapters,
                    images = epubBook.images,
                    hasProgressSaved = false,
                    lastChapterIndex = 0,
                    lastChapterOffset = 0,
                    currentChapterIndex = 0
                )
                // Added some delay to avoid choppy animation.
                delay(200L)
                _state.value = _state.value.copy(
                    isLoading = false,
                    shouldShowLoader = false
                )
            }
        }
    }

    fun updateReaderProgress(libraryItemId: Int, chapterIndex: Int, chapterOffset: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val isLastChapter = chapterIndex == state.value.chapters.size - 1
            val hasSavedProgress = state.value.hasProgressSaved
            val progressData = progressDao.getReaderData(libraryItemId)

            when {
                // Update progress for existing book
                hasSavedProgress && !isLastChapter -> {
                    progressData?.let {
                        val updatedProgress = it.copy(
                            lastChapterIndex = chapterIndex,
                            lastChapterOffset = chapterOffset,
                            lastReadTime = System.currentTimeMillis()
                        )
                        updatedProgress.id = it.id
                        progressDao.update(updatedProgress)
                    }
                }

                isLastChapter -> {
                    // Delete progress for completed book
                    progressData?.let { progressDao.delete(it.libraryItemId) }
                }

                else -> {
                    // Insert new progress for new book
                    progressDao.insert(
                        ProgressData(
                            libraryItemId = libraryItemId,
                            lastChapterIndex = chapterIndex,
                            lastChapterOffset = chapterOffset,
                            lastReadTime = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }


    fun setChapterScrollPercent(percent: Float) {
        _state.value = _state.value.copy(chapterScrollPercent = percent)
    }

    fun setVisibleChapterIndex(index: Int) {
        _state.value = _state.value.copy(currentChapterIndex = index)
    }

    fun toggleReaderMenu() {
        _state.value = _state.value.copy(showReaderMenu = !state.value.showReaderMenu)
    }

    fun hideReaderInfo() {
        _state.value = _state.value.copy(showReaderMenu = false)
    }

    fun setFontFamily(font: ReaderFont) {
        preferenceUtil.putString(PreferenceUtil.READER_FONT_STYLE_STR, font.id)
        _state.value = _state.value.copy(fontFamily = font)
    }

    fun setFontSize(newValue: Int) {
        preferenceUtil.putInt(PreferenceUtil.READER_FONT_SIZE_INT, newValue)
        _state.value = _state.value.copy(fontSize = newValue)
    }

}
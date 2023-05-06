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

import androidx.annotation.Keep
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.R
import com.starry.myne.database.library.LibraryDao
import com.starry.myne.database.reader.ReaderDao
import com.starry.myne.database.reader.ReaderItem
import com.starry.myne.epub.createEpubBook
import com.starry.myne.epub.models.EpubBook
import com.starry.myne.utils.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@Keep
sealed class ReaderFont(val id: String, val name: String, val fontFamily: FontFamily) {

    companion object {
        fun getAllFonts() = ReaderFont::class.sealedSubclasses.mapNotNull { it.objectInstance }
        fun getFontByName(name: String) = getAllFonts().find { it.name == name }!!
    }

    @Keep
    object System : ReaderFont("system", "System Default", FontFamily.Default)

    @Keep
    object Serif : ReaderFont("serif", "Serif Font", FontFamily.Serif)

    @Keep
    object Cursive : ReaderFont("cursive", "Cursive Font", FontFamily.Cursive)

    @Keep
    object SansSerif : ReaderFont("sans-serif", "SansSerif Font", FontFamily.SansSerif)

    @Keep
    object Inter : ReaderFont("inter", "Inter Font", FontFamily(Font(R.font.reader_inter_font)))
}

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
            } else if (chapterIndex == state.epubBook?.chapters!!.size - 1) {/*
                 if  the user has reached last chapter, delete this book
                 from reader database instead of saving it's progress
               */
                readerDao.getReaderItem(bookId)?.let { readerDao.delete(it.bookId) }
            } else {
                readerDao.insert(readerItem = ReaderItem(bookId, chapterIndex, chapterOffset))
            }
        }
    }

    fun setFontFamily(font: ReaderFont) {
        PreferenceUtil.putString(PreferenceUtil.READER_FONT_STYLE_STR, font.id)
    }

    fun getFontFamily(): ReaderFont {
        return ReaderFont.getAllFonts().find {
            it.id == PreferenceUtil.getString(
                PreferenceUtil.READER_FONT_STYLE_STR,
                ReaderFont.System.id
            )
        }!!
    }

}
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

package com.starry.myne.ui.screens.library.viewmodels

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.database.library.LibraryDao
import com.starry.myne.database.library.LibraryItem
import com.starry.myne.epub.EpubParser
import com.starry.myne.utils.PreferenceUtil
import com.starry.myne.utils.book.BookDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

enum class ImportStatus {
    IMPORTING, SUCCESS, ERROR, IDLE
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryDao: LibraryDao,
    private val epubParser: EpubParser,
    private val preferenceUtil: PreferenceUtil
) : ViewModel() {
    val allItems: LiveData<List<LibraryItem>> = libraryDao.getAllItems()

    val importStatus: State<ImportStatus> get() = _importStatus
    private val _importStatus = mutableStateOf(ImportStatus.IDLE)

    fun deleteItemFromDB(item: LibraryItem) {
        viewModelScope.launch(Dispatchers.IO) { libraryDao.delete(item) }
    }

    fun getInternalReaderSetting() = preferenceUtil.getBoolean(
        PreferenceUtil.INTERNAL_READER_BOOL, true
    )

    fun shouldShowLibraryTooltip() = preferenceUtil.getBoolean(
        PreferenceUtil.SHOW_LIBRARY_TOOLTIP_BOOL, true
    )

    fun libraryTooltipDismissed() = preferenceUtil.putBoolean(
        PreferenceUtil.SHOW_LIBRARY_TOOLTIP_BOOL, false
    )

    fun importBook(context: Context, fileStream: FileInputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _importStatus.value = ImportStatus.IMPORTING
                fileStream.use { fis ->
                    val epubBook = epubParser.createEpubBook(fis)
                    // reset the stream position to 0 so that it can be read again
                    fis.channel.position(0)
                    // copy the book to internal storage
                    val filePath = copyBookToInternalStorage(
                        context, fis,
                        BookDownloader.createFileName(epubBook.title)
                    )
                    // insert the book into the database
                    val libraryItem = LibraryItem(
                        bookId = 0,
                        title = epubBook.title,
                        authors = epubBook.author,
                        filePath = filePath,
                        createdAt = System.currentTimeMillis(),
                        isExternalBook = true
                    )
                    libraryDao.insert(libraryItem)
                    delay(500)
                    _importStatus.value = ImportStatus.SUCCESS
                }
            } catch (exc: Exception) {
                delay(500)
                _importStatus.value = ImportStatus.ERROR
                exc.printStackTrace()
                return@launch
            } finally {
                delay(1800) // delay to show either success or error message
                _importStatus.value = ImportStatus.IDLE
            }
        }
    }

    private suspend fun copyBookToInternalStorage(
        context: Context,
        fileStream: FileInputStream,
        filename: String
    ): String = withContext(Dispatchers.IO) {
        val booksFolder = File(context.filesDir, BookDownloader.BOOKS_FOLDER)
        if (!booksFolder.exists()) booksFolder.mkdirs()
        val bookFile = File(booksFolder, filename)
        // write the file to the internal storage
        bookFile.outputStream().use { fileStream.copyTo(it) }
        bookFile.absolutePath
    }
}
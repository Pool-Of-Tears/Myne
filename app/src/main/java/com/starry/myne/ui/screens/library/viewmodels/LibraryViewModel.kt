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
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.myne.database.library.LibraryDao
import com.starry.myne.database.library.LibraryItem
import com.starry.myne.epub.EpubParser
import com.starry.myne.helpers.PreferenceUtil
import com.starry.myne.helpers.book.BookDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryDao: LibraryDao,
    private val epubParser: EpubParser,
    private val preferenceUtil: PreferenceUtil
) : ViewModel() {

    val allItems: LiveData<List<LibraryItem>> = libraryDao.getAllItems()

    private val _showOnboardingTapTargets: MutableState<Boolean> = mutableStateOf(
        value = preferenceUtil.getBoolean(PreferenceUtil.LIBRARY_ONBOARDING_BOOL, true)
    )
    val showOnboardingTapTargets: State<Boolean> = _showOnboardingTapTargets

    fun deleteItemFromDB(item: LibraryItem) {
        epubParser.removeBookFromCache(item.filePath)
        viewModelScope.launch(Dispatchers.IO) { libraryDao.delete(item) }
    }

    fun getInternalReaderSetting() = preferenceUtil.getBoolean(
        PreferenceUtil.INTERNAL_READER_BOOL, true
    )

    fun shouldShowLibraryTooltip(): Boolean {
        return preferenceUtil.getBoolean(PreferenceUtil.LIBRARY_SWIPE_TOOLTIP_BOOL, true)
                && allItems.value?.isNotEmpty() == true
                && allItems.value?.any { !it.isExternalBook } == true
    }

    fun libraryTooltipDismissed() = preferenceUtil.putBoolean(
        PreferenceUtil.LIBRARY_SWIPE_TOOLTIP_BOOL, false
    )

    fun onboardingComplete() {
        preferenceUtil.putBoolean(PreferenceUtil.LIBRARY_ONBOARDING_BOOL, false)
        _showOnboardingTapTargets.value = false
    }

    fun importBooks(
        context: Context,
        fileUris: List<Uri>,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                fileUris.forEach { uri ->
                    context.contentResolver.openInputStream(uri)?.use { fis ->
                        if (fis !is FileInputStream) {
                            throw IllegalArgumentException("File input stream is not valid.")
                        }

                        val epubBook = epubParser.createEpubBook(fis)
                        fis.channel.position(0)

                        val filePath = copyBookToInternalStorage(
                            context, fis,
                            BookDownloader.createFileName(epubBook.title)
                        )

                        val libraryItem = LibraryItem(
                            bookId = 0,
                            title = epubBook.title,
                            authors = epubBook.author,
                            filePath = filePath,
                            createdAt = System.currentTimeMillis(),
                            isExternalBook = true
                        )

                        libraryDao.insert(libraryItem)
                    }
                }

                // Add delay here so user can see the import progress bar even if
                // the import is very fast instead of just a flicker, improving UX
                delay(800)
            }

            withContext(Dispatchers.Main) {
                result.onSuccess {
                    onComplete()
                }.onFailure { exception ->
                    Log.e("LibraryViewModel", "Error importing book", exception)
                    onError(exception)
                }
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
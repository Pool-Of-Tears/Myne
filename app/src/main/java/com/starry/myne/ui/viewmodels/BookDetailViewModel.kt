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

package com.starry.myne.ui.viewmodels

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.annotation.ExperimentalCoilApi
import com.starry.myne.MainActivity
import com.starry.myne.R
import com.starry.myne.api.BooksApi
import com.starry.myne.api.models.Book
import com.starry.myne.api.models.BookSet
import com.starry.myne.api.models.ExtraInfo
import com.starry.myne.database.LibraryDao
import com.starry.myne.database.LibraryItem
import com.starry.myne.others.Constants
import com.starry.myne.utils.BookUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookDetailScreenState(
    val isLoading: Boolean = true,
    val item: BookSet = BookSet(0, null, null, emptyList()),
    val extraInfo: ExtraInfo = ExtraInfo(),
    val bookLibraryItem: LibraryItem? = null
)

data class DownloadInfo(
    val downloadId: Long,
    var status: Int = DownloadManager.STATUS_RUNNING,
    val progress: MutableLiveData<Float> = MutableLiveData(0f)
)

@ExperimentalMaterialApi
@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@HiltViewModel
class BookDetailViewModel @Inject constructor(val libraryDao: LibraryDao) : ViewModel() {
    var state by mutableStateOf(BookDetailScreenState())
    fun getBookDetails(bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val bookItem = BooksApi.getBookById(bookId).getOrNull()!!
            val extraInfo = BooksApi.getExtraInfo(bookItem.books.first().title)
            state = if (extraInfo != null) {
                state.copy(item = bookItem, extraInfo = extraInfo)
            } else {
                state.copy(item = bookItem)
            }
            state = state.copy(
                bookLibraryItem = libraryDao.getItembyId(bookId.toInt()), isLoading = false
            )
        }
    }

    @SuppressLint("Range")
    fun downloadBook(
        book: Book, activity: MainActivity, downloadProgressListener: (Float, Int) -> Unit
    ): String {
        if (activity.checkStoragePermission()) {
            // setup download manager.
            val filename = book.title.split(" ").joinToString(separator = "+") + ".epub"
            val manager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(book.formats.applicationepubzip)
            val request = DownloadManager.Request(uri)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverRoaming(true).setAllowedOverMetered(true).setTitle(book.title)
                .setDescription(BookUtils.getAuthorsAsString(book.authors))
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, Constants.DOWNLOAD_DIR + "/" + filename
                )
            // start downloading.
            val downloadId = manager.enqueue(request)
            // start coroutine for updating download progress.
            viewModelScope.launch(Dispatchers.IO) {
                var isDownloadFinished = false
                var progress = 0f
                var status: Int
                activity.runningDownloads[book.id] = DownloadInfo(downloadId)

                while (!isDownloadFinished) {
                    val cursor: Cursor =
                        manager.query(DownloadManager.Query().setFilterById(downloadId))
                    if (cursor.moveToFirst()) {
                        status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        when (status) {
                            DownloadManager.STATUS_RUNNING -> {
                                val totalBytes: Long =
                                    cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                                if (totalBytes > 0) {
                                    val downloadedBytes: Long =
                                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                    progress = (downloadedBytes * 100 / totalBytes).toFloat() / 100
                                }
                            }
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                insertIntoDB(book, filename)
                                isDownloadFinished = true
                                progress = 1f
                                state =
                                    state.copy(bookLibraryItem = libraryDao.getItembyId(book.id))
                            }
                            DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {}
                            DownloadManager.STATUS_FAILED -> {
                                isDownloadFinished = true
                                progress = 0f
                            }
                        }

                    } else {
                        // Download cancelled by the user.
                        isDownloadFinished = true
                        progress = 0f
                        status = DownloadManager.STATUS_FAILED
                    }
                    // update download info at the ebd of iteration.
                    activity.runningDownloads[book.id]?.status = status
                    downloadProgressListener(progress, status)
                    activity.runOnUiThread {
                        activity.runningDownloads[book.id]?.progress?.postValue(progress)
                    }
                    cursor.close()
                }
                // remove download from running downloads when loop ends.
                delay(500L)
                activity.runningDownloads.remove(book.id)
            }
            return activity.getString(R.string.downloading)
        } else {
            return activity.getString(R.string.storage_perm_error)
        }
    }

    fun cancelDownload(context: Context, downloadId: Long?) {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId?.let { manager.remove(it) }
    }

    private fun insertIntoDB(book: Book, filename: String) {
        val libraryItem = LibraryItem(
            book.id,
            book.title,
            BookUtils.getAuthorsAsString(book.authors),
            "/storage/emulated/0/${Environment.DIRECTORY_DOWNLOADS}/${Constants.DOWNLOAD_DIR}/$filename",
            System.currentTimeMillis()
        )
        libraryDao.insert(libraryItem)
    }
}
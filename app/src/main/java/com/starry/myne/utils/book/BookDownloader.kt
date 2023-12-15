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

package com.starry.myne.utils.book

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import com.starry.myne.utils.Constants
import com.starry.myne.repo.models.Book
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

class BookDownloader(context: Context) {

    companion object {
        val FILE_FOLDER_PATH =
            "/storage/emulated/0/${Environment.DIRECTORY_DOWNLOADS}/${Constants.DOWNLOAD_DIR}"
    }

    private val downloadJob = Job()
    private val downloadScope = CoroutineScope(Dispatchers.IO + downloadJob)
    private val downloadManager by lazy { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    data class DownloadInfo(
        val downloadId: Long,
        var status: Int = DownloadManager.STATUS_RUNNING,
        val progress: MutableStateFlow<Float> = MutableStateFlow(0f)
    )

    /** Stores running download with book id as key */
    private val runningDownloads = HashMap<Int, DownloadInfo>()

    /**
     * Start downloading epub file for the given [Book] object.
     * @param book [Book] which needs to be downloaded.
     * @param downloadProgressListener a callable which takes download progress; [Float] and
     * download status; [Int] as arguments.
     * @param onDownloadSuccess: a callable which will be executed after download has been
     * completed successfully.
     */
    @SuppressLint("Range")
    fun downloadBook(
        book: Book, downloadProgressListener: (Float, Int) -> Unit, onDownloadSuccess: () -> Unit
    ) {
        val filename = getFilenameForBook(book)
        val uri = Uri.parse(book.formats.applicationepubzip)
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverRoaming(true).setAllowedOverMetered(true).setTitle(book.title)
            .setDescription(BookUtils.getAuthorsAsString(book.authors))
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, Constants.DOWNLOAD_DIR + "/" + filename
            )
        val downloadId = downloadManager.enqueue(request)

        downloadScope.launch {
            var isDownloadFinished = false
            var progress = 0f
            var status: Int
            runningDownloads[book.id] = DownloadInfo(downloadId)

            while (!isDownloadFinished) {
                val cursor: Cursor =
                    downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
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
                            isDownloadFinished = true
                            progress = 1f
                            onDownloadSuccess()
                        }

                        DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {}
                        DownloadManager.STATUS_FAILED -> {
                            isDownloadFinished = true
                            progress = 0f
                        }
                    }

                } else {
                    /** Download cancelled by the user. */
                    isDownloadFinished = true
                    progress = 0f
                    status = DownloadManager.STATUS_FAILED
                }

                /** update download info at the end of iteration. */
                runningDownloads[book.id]?.status = status
                downloadProgressListener(progress, status)
                runningDownloads[book.id]?.progress?.value = progress
                cursor.close()
            }
            /**
            Remove download from running downloads when loop ends.
            added dome delay here so we get time to update our UI before
            download info gets removed.
             */
            delay(500L)
            runningDownloads.remove(book.id)
        }
    }

    /**
     * Returns true if book with the given id is currently being downloaded
     * false otherwise.
     */
    fun isBookCurrentlyDownloading(bookId: Int) = runningDownloads.containsKey(bookId)

    /**
     * Returns [DownloadInfo] for the given book id if it's currently
     * being downloaded, null otherwise.
     */
    fun getRunningDownload(bookId: Int) = runningDownloads[bookId]

    /**
     * Cancels download of book by using it's download id (if download is running).
     */
    fun cancelDownload(downloadId: Long?) = downloadId?.let { downloadManager.remove(it) }

    /**
     * Sanitizes book title by replacing forbidden chars which are not allowed
     * as the file name & builds file name for the epub file by joining all of
     * the words in the  book title at the end.
     */
    fun getFilenameForBook(book: Book) = book.title.replace(":", ";")
        .replace("\"", "").split(" ").joinToString(separator = "+") + ".epub"

}
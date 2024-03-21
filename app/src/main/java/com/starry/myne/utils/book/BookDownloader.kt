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

package com.starry.myne.utils.book

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import com.starry.myne.repo.models.Book
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File


/**
 * Class to handle downloading of books.
 * @param context [Context] required to access [DownloadManager] and to get file path for the downloaded file.
 */
class BookDownloader(private val context: Context) {

    private val downloadJob = Job()
    private val downloadScope = CoroutineScope(Dispatchers.IO + downloadJob)
    private val downloadManager by lazy { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    /**
     * Data class to store download info for a book.
     * @param downloadId id of the download.
     * @param status status of the download.
     * @param progress progress of the download.
     */
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
        book: Book, downloadProgressListener: (progress: Float, status: Int) -> Unit,
        onDownloadSuccess: (fileName: String) -> Unit
    ) {
        val filename = getFilenameForBook(book)
        val uri = Uri.parse(book.formats.applicationepubzip)
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverRoaming(true).setAllowedOverMetered(true).setTitle(book.title)
            .setDescription(BookUtils.getAuthorsAsString(book.authors))
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename)
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
                            onDownloadSuccess(filename)
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
     * Returns file path for the given book's file name.
     * @param fileName name of the file for which file path is required.
     * @return [String] file path for the given file name.
     */
    fun getFilePathForBook(fileName: String): String {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        return File(externalFilesDir, fileName).canonicalPath
    }

    /**
     * Returns true if book with the given id is currently being downloaded
     * false otherwise.
     * @param bookId id of the book for which download status is required.
     * @return [Boolean] true if book is currently being downloaded, false otherwise.
     */
    fun isBookCurrentlyDownloading(bookId: Int) = runningDownloads.containsKey(bookId)

    /**
     * Returns [DownloadInfo] if book with the given id is currently being downloaded.
     * @param bookId id of the book for which download info is required.
     * @return [DownloadInfo] if book is currently being downloaded, null otherwise.
     */
    fun getRunningDownload(bookId: Int) = runningDownloads[bookId]

    /**
     * Cancels download of book by using it's download id (if download is running).
     * @param downloadId id of the download which needs to be cancelled.
     */
    fun cancelDownload(downloadId: Long?) = downloadId?.let { downloadManager.remove(it) }

    /**
     * Sanitizes book title by replacing forbidden chars which are not allowed
     * as the file name & builds file name for the epub file by joining all of
     * the words in the  book title at the end.
     * @param book [Book] for which file name is required.
     * @return [String] file name for the given book.
     */
    private fun getFilenameForBook(book: Book) = book.title.replace(":", ";")
        .replace("\"", "").split(" ").joinToString(separator = "+") + ".epub"

}
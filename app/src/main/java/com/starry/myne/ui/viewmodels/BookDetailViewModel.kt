package com.starry.myne.ui.viewmodels

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.annotation.ExperimentalCoilApi
import com.starry.myne.MainActivity
import com.starry.myne.R
import com.starry.myne.api.BooksApi
import com.starry.myne.api.models.Book
import com.starry.myne.api.models.BookSet
import com.starry.myne.api.models.ExtraInfo
import com.starry.myne.others.Constants
import com.starry.myne.utils.BookUtils
import com.starry.myne.utils.toToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ScreenState(
    val isLoading: Boolean = true,
    val item: BookSet = BookSet(0, null, null, emptyList()),
    val extraInfo: ExtraInfo = ExtraInfo()
)

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
class BookDetailViewModel : ViewModel() {
    var state by mutableStateOf(ScreenState())

    fun getBookDetails(bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val bookItem = BooksApi.getBookById(bookId).getOrNull()!!
            val extraInfo = BooksApi.getExtraInfo(bookItem.books.first().title)
            state = if (extraInfo != null) {
                state.copy(isLoading = false, item = bookItem, extraInfo = extraInfo)
            } else {
                state.copy(isLoading = false, item = bookItem)
            }
        }
    }

    fun downloadBook(book: Book, activity: MainActivity): String {
        if (activity.checkStoragePermission()) {
            // setup download manager.
            val filename = book.title.split(" ").joinToString(separator = "+")
            val manager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(book.formats.applicationepubzip)
            val request = DownloadManager.Request(uri)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverRoaming(true)
                .setAllowedOverMetered(true)
                .setTitle(book.title)
                .setDescription(BookUtils.getAuthorsAsString(book.authors))
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    Constants.DOWNLOAD_DIR + "/" + "${filename}.epub"
                )
            // start downloading.
            manager.enqueue(request)
            return activity.getString(R.string.downloading)
        } else {
           return activity.getString(R.string.storage_perm_error)
        }
    }
}
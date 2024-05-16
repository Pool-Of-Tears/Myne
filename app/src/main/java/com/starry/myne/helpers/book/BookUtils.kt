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

package com.starry.myne.helpers.book

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.starry.myne.BuildConfig
import com.starry.myne.R
import com.starry.myne.api.models.Author
import com.starry.myne.database.library.LibraryItem
import com.starry.myne.helpers.toToast
import com.starry.myne.ui.navigation.Screens
import java.io.File
import java.util.Locale

object BookUtils {

    /**
     * Converts the list of authors into a single string.
     *
     * @param authors List of authors.
     * @return String representation of the authors.
     */
    fun getAuthorsAsString(authors: List<Author>): String {
        return if (authors.isEmpty()) {
            "Unknown Author"
        } else {
            authors.joinToString(", ") { fixAuthorName(it.name) }
        }
    }

    /**
     * For some weird reasons, gutenberg gives name of authors in
     * reversed, where first name and last are separated by a comma
     * Eg: "Fyodor Dostoyevsky" becomes "Dostoyevsky, Fyodor", This
     * function fixes that and returns name in correct format.
     *
     * @param name Name of the author.
     * @return Name of the author in correct format.
     */
    private fun fixAuthorName(name: String): String {
        return name.split(",").reversed().joinToString(" ") { it.trim() }
    }

    /**
     * Converts the list of languages into a single string.
     *
     * @param languages List of languages.
     * @return String representation of the languages.
     */
    fun getLanguagesAsString(languages: List<String>): String {
        return languages.joinToString(", ") { Locale(it).displayLanguage }
    }

    /**
     * Converts the list of subjects into a single string.
     *
     * @param subjects List of subjects.
     * @param limit Maximum number of subjects to show.
     * @return String representation of the subjects.
     */
    fun getSubjectsAsString(subjects: List<String>, limit: Int): String {
        val allSubjects = subjects.flatMap { it.split("--") }.map { it.trim() }.toSet()
        val truncatedSubs = if (allSubjects.size > limit) allSubjects.take(limit) else allSubjects
        return truncatedSubs.joinToString(", ")
    }

    /**
     * Opens the book file using the appropriate app or the internal reader.
     *
     * @param context Context of the app.
     * @param internalReader Whether to use the internal reader or not.
     * @param libraryItem Library item to open.
     * @param navController Navigation controller to navigate to the reader screen.
     */
    fun openBookFile(
        context: Context,
        internalReader: Boolean,
        libraryItem: LibraryItem,
        navController: NavController
    ) {
        if (internalReader) {
            navController.navigate(Screens.ReaderDetailScreen.withLibraryItemId(libraryItem.id.toString()))
        } else {
            val uri = FileProvider.getUriForFile(
                context, BuildConfig.APPLICATION_ID + ".provider", File(libraryItem.filePath)
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(uri, context.contentResolver.getType(uri))
            val chooser = Intent.createChooser(
                intent, context.getString(R.string.open_app_chooser)
            )
            try {
                context.startActivity(chooser)
            } catch (exc: ActivityNotFoundException) {
                context.getString(R.string.no_app_to_handle_epub).toToast(context)
            }
        }
    }
}
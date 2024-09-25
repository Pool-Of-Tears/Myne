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

package com.starry.myne.epub

import android.content.Context
import android.util.Log
import com.starry.myne.epub.models.EpubBook
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * A cache for storing epub books.
 *
 * @param context The context.
 */
class EpubCache(private val context: Context) {

    companion object {
        private const val TAG = "EpubCache"
        private const val EPUB_CACHE = "epub_cache"
        private const val CACHE_VERSION_FILE = "cache_version"

        // Increment this if the cache format changes.
        private const val EPUB_CACHE_VERSION = 1
    }

    init {
        create()
        checkCacheVersion()
    }

    private fun getPath(): String {
        return context.cacheDir.absolutePath + File.separator + EPUB_CACHE
    }

    private fun getVersionFilePath(): String {
        return getPath() + File.separator + CACHE_VERSION_FILE
    }

    private fun checkCacheVersion() {
        Log.d(TAG, "Checking cache version")
        val versionFile = File(getVersionFilePath())
        if (versionFile.exists()) {
            val cachedVersion = versionFile.readText().toIntOrNull()
            if (cachedVersion != EPUB_CACHE_VERSION) {
                Log.d(TAG, "Cache version mismatch, clearing cache")
                clear()
                create()
                saveCacheVersion()
            }
        } else {
            Log.d(TAG, "Cache version not found, saving cache version")
            saveCacheVersion()
        }
    }

    private fun saveCacheVersion() {
        Log.d(TAG, "Saving cache version")
        val versionFile = File(getVersionFilePath())
        versionFile.writeText(EPUB_CACHE_VERSION.toString())
    }

    private fun create() {
        val cacheDir = File(getPath())
        if (!cacheDir.exists()) {
            Log.d(TAG, "Creating cache directory")
            cacheDir.mkdirs()
        }
    }

    private fun clear() {
        val cacheDir = File(getPath())
        if (cacheDir.exists()) {
            Log.d(TAG, "Clearing cache directory")
            cacheDir.deleteRecursively()
        }
    }

    /**
     * Adds a book to the cache.
     *
     * @param book The book to add.
     * @param filepath The path to the book file.
     */
    fun put(book: EpubBook, filepath: String) {
        Log.d(TAG, "Inserting book into cache: ${book.title}")
        val fileName = File(filepath).nameWithoutExtension
        val bookFile = File(getPath(), "$fileName.json")
        val jsonString = Json.encodeToString(book)
        bookFile.writeText(jsonString)
    }

    /**
     * Gets a book from the cache.
     * If the book is not cached, null is returned.
     *
     * @param filepath The path to the book file.
     * @return The book if it is cached, null otherwise.
     */
    fun get(filepath: String): EpubBook? {
        Log.d(TAG, "Getting book from cache: $filepath")
        val fileName = File(filepath).nameWithoutExtension
        val bookFile = File(getPath(), "$fileName.json")
        return if (bookFile.exists()) {
            Log.d(TAG, "Book found in cache: $filepath")
            val jsonString = bookFile.readText()
            Json.decodeFromString(jsonString)
        } else {
            Log.d(TAG, "Book not found in cache: $filepath")
            null
        }
    }

    /**
     * Removes a book from the cache.
     *
     * @param filepath The path to the book file.
     */
    fun remove(filepath: String): Boolean {
        Log.d(TAG, "Removing book from cache: $filepath")
        val fileName = File(filepath).nameWithoutExtension
        val bookFile = File(getPath(), "$fileName.json")
        return if (bookFile.exists()) {
            bookFile.delete()
        } else {
            false
        }
    }

    /**
     * Checks if a book is cached.
     *
     * @param filepath The path to the book file.
     * @return True if the book is cached, false otherwise.
     */
    fun isCached(filepath: String): Boolean {
        val fileName = File(filepath).nameWithoutExtension
        val bookFile = File(getPath(), "$fileName.json")
        return bookFile.exists()
    }
}

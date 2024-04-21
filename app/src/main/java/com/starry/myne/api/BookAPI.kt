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

package com.starry.myne.api

import android.content.Context
import com.google.gson.Gson
import com.starry.myne.BuildConfig
import com.starry.myne.api.models.BookSet
import com.starry.myne.api.models.ExtraInfo
import com.starry.myne.helpers.book.BookLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * This class is responsible for handling all the API requests related to books.
 * It uses OkHttp for making network requests and Gson for parsing JSON responses.
 * @param context The context of the application.
 */
class BookAPI(context: Context) {

    private val baseApiUrl = "https://myne.pooloftears.xyz/books"
    private val googleBooksUrl = "https://www.googleapis.com/books/v1/volumes"

    private val googleApiKey =
        BuildConfig.GOOGLE_API_KEY ?: "AIzaSyBCaXx-U0sbEpGVPWylSggC4RaR4gCGkVE" // Backup API key

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
        .cache(Cache(File(context.cacheDir, "http-cache"), CacheInterceptor.CACHE_SIZE))
        .addNetworkInterceptor(CacheInterceptor())
        .build()

    private val gsonClient = Gson() // Gson client for parsing JSON responses.


    /**
     * This function fetches all the books from the API.
     * @param page The page number of the books to fetch.
     * @param bookLanguage The language of the books to fetch.
     * @return A Result object containing the BookSet if the request was successful, or an exception if it failed.
     */
    suspend fun getAllBooks(
        page: Long,
        bookLanguage: BookLanguage = BookLanguage.AllBooks
    ): Result<BookSet> {
        var url = "${baseApiUrl}?page=$page"
        if (bookLanguage != BookLanguage.AllBooks) {
            url += "&languages=${bookLanguage.isoCode}"
        }
        val request = Request.Builder().get().url(url).build()
        return makeApiRequest(request)
    }

    /**
     * This function searches for books based on the query provided.
     * @param query The query to search for.
     * @return A Result object containing the BookSet if the request was successful, or an exception if it failed.
     */
    suspend fun searchBooks(query: String): Result<BookSet> {
        val encodedString = withContext(Dispatchers.IO) {
            URLEncoder.encode(query, "UTF-8")
        }
        val request = Request.Builder().get().url("${baseApiUrl}?search=$encodedString").build()
        return makeApiRequest(request)
    }

    /**
     * This function fetches a book by its ID.
     * @param bookId The ID of the book to fetch.
     * @return A Result object containing the BookSet if the request was successful, or an exception if it failed.
     */
    suspend fun getBookById(bookId: String): Result<BookSet> {
        val request = Request.Builder().get().url("${baseApiUrl}?ids=$bookId").build()
        return makeApiRequest(request)
    }

    /**
     * This function fetches books by category.
     * @param category The category of the books to fetch.
     * @param page The page number of the books to fetch.
     * @param bookLanguage The language of the books to fetch.
     * @return A Result object containing the BookSet if the request was successful, or an exception if it failed.
     */
    suspend fun getBooksByCategory(
        category: String,
        page: Long,
        bookLanguage: BookLanguage = BookLanguage.AllBooks
    ): Result<BookSet> {
        var url = "${baseApiUrl}?page=$page&topic=$category"
        if (bookLanguage != BookLanguage.AllBooks) {
            url += "&languages=${bookLanguage.isoCode}"
        }
        val request = Request.Builder().get().url(url).build()
        return makeApiRequest(request)
    }

    // Helper function to make API requests.
    private suspend fun makeApiRequest(request: Request): Result<BookSet> =
        suspendCoroutine { continuation ->
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(Result.failure(exception = e))
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        continuation.resume(
                            Result.success(
                                gsonClient.fromJson(response.body!!.string(), BookSet::class.java)
                            )
                        )
                    }
                }
            })
        }

    // Function to fetch extra info such as cover image, page count, and description of a book.
    // From Google Books API.
    suspend fun getExtraInfo(bookName: String): ExtraInfo? = suspendCoroutine { continuation ->
        val encodedName = URLEncoder.encode(bookName, "UTF-8")
        val url = "${googleBooksUrl}?q=$encodedName&startIndex=0&maxResults=1&key=$googleApiKey"
        val request = Request.Builder().get().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    continuation.resume(parseExtraInfoJson(response.body!!.string()))
                }
            }
        })
    }

    // Helper function to parse extra info JSON.
    private fun parseExtraInfoJson(jsonString: String): ExtraInfo? {
        return try {
            val jsonObj = JSONObject(jsonString)
            val totalItems = jsonObj.getInt("totalItems")
            if (totalItems != 0) {
                val items = jsonObj.getJSONArray("items")
                val item = items.getJSONObject(0)
                val volumeInfo = item.getJSONObject("volumeInfo")
                val imageLinks = volumeInfo.getJSONObject("imageLinks")
                // Build Extra info.
                val coverImage = imageLinks.getString("thumbnail").replace(
                    "http://", "https://"
                )
                val pageCount = try {
                    volumeInfo.getInt("pageCount")
                } catch (exc: JSONException) {
                    0
                }
                val description = try {
                    volumeInfo.getString("description")
                } catch (exc: JSONException) {
                    ""
                }
                ExtraInfo(coverImage, pageCount, description)
            } else {
                null
            }
        } catch (exc: JSONException) {
            exc.printStackTrace()
            null
        }
    }

}
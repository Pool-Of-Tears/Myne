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

package com.starry.myne.repo

import com.google.gson.Gson
import com.starry.myne.repo.models.BookSet
import com.starry.myne.repo.models.ExtraInfo
import com.starry.myne.others.BookLanguages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class BookRepository {

    private lateinit var baseApiUrl: String
    private val googleBooksUrl = "https://www.googleapis.com/books/v1/volumes"
    private val googleApiKey = "AIzaSyBCaXx-U0sbEpGVPWylSggC4RaR4gCGkVE"


    private val okHttpClient = OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS).readTimeout(100, TimeUnit.SECONDS).build()

    private val gsonClient = Gson()

    suspend fun getAllBooks(
        page: Long,
        bookLanguage: BookLanguages = BookLanguages.English
    ): Result<BookSet> {
        setApiUrlIfNotSetAlready()
        val url = if (bookLanguage != BookLanguages.AllBooks) {
            "${baseApiUrl}?page=$page&languages=${bookLanguage.isoCode}"
        } else {
            "${baseApiUrl}?page=$page"
        }
        val request = Request.Builder().get().url(url).build()
        return makeApiRequest(request)
    }

    suspend fun searchBooks(query: String): Result<BookSet> {
        setApiUrlIfNotSetAlready()
        val encodedString = withContext(Dispatchers.IO) {
            URLEncoder.encode(query, "UTF-8")
        }
        val request = Request.Builder().get().url("${baseApiUrl}?search=$encodedString").build()
        return makeApiRequest(request)
    }

    suspend fun getBookById(bookId: String): Result<BookSet> {
        setApiUrlIfNotSetAlready()
        val request = Request.Builder().get().url("${baseApiUrl}?ids=$bookId").build()
        return makeApiRequest(request)
    }

    suspend fun getBooksByCategory(category: String, page: Long): Result<BookSet> {
        setApiUrlIfNotSetAlready()
        val request =
            Request.Builder().get().url("${baseApiUrl}?page=$page&topic=$category").build()
        return makeApiRequest(request)
    }

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
                                gsonClient.fromJson(
                                    response.body!!.string(), BookSet::class.java
                                )
                            )
                        )
                    }
                }
            })
        }

    suspend fun getExtraInfo(bookName: String): ExtraInfo? = suspendCoroutine { continuation ->
        val encodedName = URLEncoder.encode(bookName, "UTF-8")
        val url =
            "${googleBooksUrl}?q=$encodedName&startIndex=0&maxResults=1&apiKey=$googleApiKey"
        val request = Request.Builder().get().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    continuation.resume(
                        parseExtraInfoJson(response.body!!.string())
                    )
                }
            }
        })
    }

    fun parseExtraInfoJson(jsonString: String): ExtraInfo? {
        val jsonObj = JSONObject(jsonString)
        return try {
            val totalItems = jsonObj.getInt("totalItems")
            if (totalItems != 0) {
                val items = jsonObj.getJSONArray("items")
                val item = items.getJSONObject(0)
                val volumeInfo = item.getJSONObject("volumeInfo")
                val imageLinks = volumeInfo.getJSONObject("imageLinks")
                // Build Extra info.
                val coverImage = imageLinks.getString("thumbnail")
                val pageCount = volumeInfo.getInt("pageCount")
                val description = volumeInfo.getString("description")
                ExtraInfo(coverImage, pageCount, description)
            } else {
                null
            }
        } catch (exc: JSONException) {
            null
        }
    }

    private suspend fun setApiUrlIfNotSetAlready() {
        if (!this::baseApiUrl.isInitialized) {
            val request = Request.Builder().get()
                .url("https://raw.githubusercontent.com/starry-shivam/stuffs/main/myne-api-url")
                .build()
            val response = suspendCoroutine { continuation ->
                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        continuation.resumeWithException(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            continuation.resume(
                                response.body!!.string()
                            )
                        }
                    }
                })
            }
            val jsonObj = JSONObject(response)
            baseApiUrl = jsonObj.getString("api_url")
        }
    }

}
package com.starry.myne.api

import com.google.gson.Gson
import com.starry.myne.api.models.BookSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BooksApi {

    companion object {
        const val BASE_URL = "https://gutendex.com/books"
    }

    private val okHttpClient = OkHttpClient()
    private val gsonClient = Gson()

    suspend fun getAllBooks(page: Long): Result<BookSet> {
        val request = Request.Builder().get().url("${BASE_URL}?page=$page").build()
        return makeApiRequest(request)
    }

    suspend fun searchBooks(query: String): Result<BookSet> {
        val encodedString = withContext(Dispatchers.IO) {
            URLEncoder.encode(query, "UTF-8")
        }
        val request = Request.Builder().get().url("${BASE_URL}?search=$encodedString").build()
        return makeApiRequest(request)
    }

    private suspend fun makeApiRequest(request: Request): Result<BookSet> =
        suspendCoroutine { continuation ->
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        continuation.resume(
                            Result.success(
                                gsonClient.fromJson(
                                    response.body!!.string(),
                                    BookSet::class.java
                                )
                            )
                        )
                    }
                }
            })
        }
}
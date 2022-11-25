package com.starry.myne.api

import com.google.gson.Gson
import com.starry.myne.api.models.BookSet
import okhttp3.*
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BooksApi {

    companion object {
        const val BASE_URL = "https://gutendex.com/books"
    }

    private val okHttpClient = OkHttpClient()
    private val gsonClient = Gson()

    suspend fun getAllBooks(page: Long): Result<BookSet> = suspendCoroutine { continuation ->
        val request = Request.Builder().get().url("${BASE_URL}?page=$page").build()
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
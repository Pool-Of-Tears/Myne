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

import com.starry.myne.api.CacheInterceptor.Companion.CACHE_MAX_AGE
import com.starry.myne.api.CacheInterceptor.Companion.CACHE_SIZE
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Interceptor to cache the response for a week to
 * reduce the number of network calls.
 *
 * @property CACHE_SIZE 32 MiB
 * @property CACHE_MAX_AGE 1 week
 */
class CacheInterceptor : Interceptor {

    companion object {
        const val CACHE_SIZE = 32L * 1024L * 1024L // 32 MiB
        const val CACHE_MAX_AGE = 7 // 1 week
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        val cacheControl = CacheControl.Builder()
            .maxAge(CACHE_MAX_AGE, TimeUnit.DAYS)
            .build()
        return response.newBuilder()
            .header("Cache-Control", cacheControl.toString())
            .build()
    }
}
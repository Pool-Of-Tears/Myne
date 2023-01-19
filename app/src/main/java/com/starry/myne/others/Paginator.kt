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

package com.starry.myne.others

class Paginator<Page, BookSet>(
    private val initialPage: Page,
    private inline val onLoadUpdated: (Boolean) -> Unit,
    private inline val onRequest: suspend (nextPage: Page) -> Result<BookSet>,
    private inline val getNextPage: suspend (BookSet) -> Page,
    private inline val onError: suspend (Throwable?) -> Unit,
    private inline val onSuccess: suspend (item: BookSet, newPage: Page) -> Unit
) {

    private var currentPage = initialPage
    private var isMakingRequest = false

    suspend fun loadNextItems() {
        if (isMakingRequest) {
            return
        }
        isMakingRequest = true
        onLoadUpdated(true)
        val result = onRequest(currentPage)
        isMakingRequest = false
        val bookSet = result.getOrElse {
            onError(it)
            onLoadUpdated(false)
            return
        }
        currentPage = getNextPage(bookSet)
        onSuccess(bookSet, currentPage)
        onLoadUpdated(false)
    }

    fun reset() {
        currentPage = initialPage
    }
}
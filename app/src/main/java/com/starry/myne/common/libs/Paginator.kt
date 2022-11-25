package com.starry.myne.common.libs

interface Paginator<Page, BookSet> {
    suspend fun loadNextItems()
    fun reset()
}
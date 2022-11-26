package com.starry.myne.others

interface Paginator<Page, BookSet> {
    suspend fun loadNextItems()
    fun reset()
}
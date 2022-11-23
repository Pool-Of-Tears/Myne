package com.starry.myne.common

interface Paginator<Key, Item> {
    suspend fun loadNextItems()
    fun reset()
}
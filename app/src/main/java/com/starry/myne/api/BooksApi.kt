package com.starry.myne.api

import com.starry.myne.api.models.AllBooks

class BooksApi {
    companion object BookApiConstants {
        const val BASE_URL = "https://gutendex.com/"
    }

    fun getAllBooks() : AllBooks {
        TODO("Not Yet Implemented.")
    }

    fun getNextBookSet(books: AllBooks) : AllBooks {
        TODO("Not Yet Implemented.")
    }
}
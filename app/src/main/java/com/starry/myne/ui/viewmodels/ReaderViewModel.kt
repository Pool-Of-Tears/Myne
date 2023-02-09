package com.starry.myne.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.starry.myne.epub.createEpubBook


class ReaderViewModel: ViewModel() {

    fun parseEpubFile(filePath: String) {
        val epubBook = createEpubBook(filePath)
        println(epubBook.chapters.first())
    }

}
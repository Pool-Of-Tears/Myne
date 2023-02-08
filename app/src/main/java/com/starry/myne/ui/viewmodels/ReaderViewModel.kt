package com.starry.myne.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.starry.myne.epub.createEpubBook
import java.io.FileInputStream


class ReaderViewModel: ViewModel() {

    fun parseEpubFile(filePath: String) {
        println( createEpubBook(FileInputStream(filePath)))
    }

}
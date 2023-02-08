package com.starry.myne.epub.models

data class EpubBook(
    val fileName: String,
    val title: String,
    val coverImagePath: String,
    val chapters: List<EpubChapter>,
    val images: List<EpubImage>
)
package com.starry.myne.epub.models

import android.graphics.Bitmap

data class EpubBook(
    val fileName: String,
    val title: String,
    val coverImage: Bitmap?,
    val chapters: List<EpubChapter>,
    val images: List<EpubImage>
)
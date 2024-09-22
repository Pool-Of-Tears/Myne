package com.starry.myne.api.models


import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Book(
    @SerialName("authors")
    val authors: List<Author>,
    @SerialName("bookshelves")
    val bookshelves: List<String>,
    @SerialName("copyright")
    val copyright: Boolean,
    @SerialName("download_count")
    val downloadCount: Int,
    @SerialName("formats")
    val formats: Formats,
    @SerialName("id")
    val id: Int,
    @SerialName("languages")
    val languages: List<String>,
    @SerialName("media_type")
    val mediaType: String,
    @SerialName("subjects")
    val subjects: List<String>,
    @SerialName("title")
    val title: String,
    @SerialName("translators")
    val translators: List<Translator>
)
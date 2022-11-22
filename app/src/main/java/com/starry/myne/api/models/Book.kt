package com.starry.myne.api.models


import com.google.gson.annotations.SerializedName

data class Book(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("authors")
    val authors: List<Author>,
    @SerializedName("translators")
    val translators: List<Translator>,
    @SerializedName("subjects")
    val subjects: List<String>,
    @SerializedName("bookshelves")
    val bookshelves: List<String>,
    @SerializedName("languages")
    val languages: List<String>,
    @SerializedName("copyright")
    val copyright: Boolean,
    @SerializedName("media_type")
    val mediaType: String,
    @SerializedName("formats")
    val formats: Formats,
    @SerializedName("download_count")
    val downloadCount: Int
)
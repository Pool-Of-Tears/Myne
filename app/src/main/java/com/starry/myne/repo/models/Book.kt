/*
Copyright 2022 - 2023 Stɑrry Shivɑm

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.starry.myne.repo.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
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
    val downloadCount: Long
)
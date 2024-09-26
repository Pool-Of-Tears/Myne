/**
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
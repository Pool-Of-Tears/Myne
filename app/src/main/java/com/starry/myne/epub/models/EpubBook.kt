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


package com.starry.myne.epub.models

import android.graphics.Bitmap
import com.starry.myne.epub.cache.BitmapSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * Represents an epub book.
 *
 * @param fileName The name of the epub file.
 * @param title The title of the book.
 * @param author The author of the book.
 * @param language The language code of the book.
 * @param coverImage The cover image of the book.
 * @param chapters The list of chapters in the book.
 * @param images The list of images in the book.
 */
@Serializable
data class EpubBook @OptIn(ExperimentalSerializationApi::class) constructor(
    @ProtoNumber(1) val fileName: String,
    @ProtoNumber(2) val title: String,
    @ProtoNumber(3) val author: String,
    @ProtoNumber(4) val language: String,
    @ProtoNumber(5) @Serializable(with = BitmapSerializer::class) val coverImage: Bitmap?,
    @ProtoNumber(6) val chapters: List<EpubChapter> = emptyList(),
    @ProtoNumber(7) val images: List<EpubImage> = emptyList()
)
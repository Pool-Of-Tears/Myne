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

package com.starry.myne.epub.cache

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.ByteArrayOutputStream

/**
 * A [KSerializer] for [Bitmap] objects.
 * It serializes the bitmap to a byte array and deserializes it back to a bitmap.
 */
object BitmapSerializer : KSerializer<Bitmap> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Bitmap") {
        element<ByteArray>("bytes")
    }

    override fun serialize(encoder: Encoder, value: Bitmap) {
        val stream = ByteArrayOutputStream()
        value.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        encoder.encodeSerializableValue(ByteArraySerializer(), byteArray)
    }

    override fun deserialize(decoder: Decoder): Bitmap {
        val byteArray = decoder.decodeSerializableValue(ByteArraySerializer())
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}


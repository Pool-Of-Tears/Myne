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
data class Formats(
    @SerialName("application/epub+zip")
    val applicationepubzip: String? = null,
    @SerialName("application/octet-stream")
    val applicationoctetStream: String? = null,
    @SerialName("application/rdf+xml")
    val applicationrdfxml: String? = null,
    @SerialName("application/x-mobipocket-ebook")
    val applicationxMobipocketEbook: String? = null,
    @SerialName("image/jpeg")
    val imagejpeg: String? = null,
    @SerialName("text/html")
    val texthtml: String? = null,
    @SerialName("text/html; charset=iso-8859-1")
    val texthtmlCharsetiso88591: String? = null,
    @SerialName("text/html; charset=utf-8")
    val texthtmlCharsetutf8: String? = null,
    @SerialName("text/plain; charset=iso-8859-1")
    val textplainCharsetiso88591: String? = null,
    @SerialName("text/plain; charset=us-ascii")
    val textplainCharsetusAscii: String? = null,
    @SerialName("text/plain; charset=utf-8")
    val textplainCharsetutf8: String? = null,
)
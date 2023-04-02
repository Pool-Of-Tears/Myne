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
data class Formats(
    @SerializedName("text/plain")
    val textplain: String,
    @SerializedName("application/x-mobipocket-ebook")
    val applicationxMobipocketEbook: String?,
    @SerializedName("text/html")
    val texthtml: String?,
    @SerializedName("application/octet-stream")
    val applicationoctetStream: String?,
    @SerializedName("text/plain; charset=us-ascii")
    val textplainCharsetusAscii: String?,
    @SerializedName("application/epub+zip")
    val applicationepubzip: String?,
    @SerializedName("image/jpeg")
    val imagejpeg: String?,
    @SerializedName("application/rdf+xml")
    val applicationrdfxml: String?,
    @SerializedName("text/html; charset=iso-8859-1")
    val texthtmlCharsetiso88591: String?,
    @SerializedName("text/html; charset=utf-8")
    val texthtmlCharsetutf8: String?,
    @SerializedName("text/plain; charset=utf-8")
    val textplainCharsetutf8: String?,
    @SerializedName("text/plain; charset=iso-8859-1")
    val textplainCharsetiso88591: String?
)
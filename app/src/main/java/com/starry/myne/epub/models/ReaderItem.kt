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

sealed class ReaderItem {
    data class Text(val spans: List<HtmlSpan>) : ReaderItem()
    data class Image(val path: String, val yrel: Float) : ReaderItem()
    data class CodeBlock(val code: String) : ReaderItem()
    data class Blockquote(val spans: List<HtmlSpan>) : ReaderItem()
}

sealed class HtmlSpan {
    data class Text(val text: String) : HtmlSpan()
    data class Tag(
        val name: String,
        val attributes: Map<String, String>,
        val children: List<HtmlSpan>
    ) : HtmlSpan()
}

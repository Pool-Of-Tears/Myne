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


package com.starry.myne.epub

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
import com.starry.myne.epub.models.HtmlSpan
import org.jsoup.Jsoup

fun String.asAnnotatedString(collapseWhitespace: Boolean = true): AnnotatedString {
    val document = Jsoup.parseBodyFragment(this)
    document.outputSettings().prettyPrint(false)
    val spans = document.body().childNodes().map { it.toHtmlSpan() }
    return spans.asAnnotatedString(collapseWhitespace)
}

private fun org.jsoup.nodes.Node.toHtmlSpan(): HtmlSpan {
    return when (this) {
        is org.jsoup.nodes.TextNode -> HtmlSpan.Text(this.wholeText)
        is org.jsoup.nodes.Element -> HtmlSpan.Tag(
            name = this.tagName(),
            attributes = this.attributes().associate { it.key to it.value },
            children = this.childNodes().map { it.toHtmlSpan() }
        )

        else -> HtmlSpan.Text("")
    }
}

fun List<HtmlSpan>.asAnnotatedString(collapseWhitespace: Boolean = true): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var lastWasSpace = true // Start as true to trim leading space of the block

    fun parseSpan(span: HtmlSpan) {
        when (span) {
            is HtmlSpan.Text -> {
                if (collapseWhitespace) {
                    // Collapse all whitespace sequences (including newlines) into a single space.
                    // This prevents hard wrapped text from breaking mid-sentence.
                    val normalized = span.text
                        .replace("\u00A0", " ")
                        .replace(Regex("\\s+"), " ")

                    if (normalized.isEmpty()) return

                    var toAppend = normalized
                    if (lastWasSpace) {
                        toAppend = toAppend.trimStart()
                    }

                    if (toAppend.isNotEmpty()) {
                        builder.append(toAppend)
                        lastWasSpace = toAppend.endsWith(" ")
                    }
                } else {
                    builder.append(span.text)
                }
            }

            is HtmlSpan.Tag -> {
                val style = when (span.name) {
                    "b", "strong" -> SpanStyle(fontWeight = FontWeight.Bold)
                    "i", "em" -> SpanStyle(fontStyle = FontStyle.Italic)
                    "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
                    "strike", "del", "s" -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                    "code" -> SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color.LightGray.copy(alpha = 0.3f)
                    )

                    "sub" -> SpanStyle(
                        baselineShift = BaselineShift.Subscript,
                        fontSize = 0.75.em
                    )

                    "sup" -> SpanStyle(
                        baselineShift = BaselineShift.Superscript,
                        fontSize = 0.75.em
                    )

                    else -> null
                }

                when {
                    span.name == "br" -> {
                        builder.append("\n")
                        lastWasSpace = true
                    }

                    span.name == "a" -> {
                        val url = span.attributes["href"] ?: ""
                        builder.pushStringAnnotation("URL", url)
                        span.children.forEach { parseSpan(it) }
                        builder.pop()
                    }

                    style != null -> {
                        builder.pushStyle(style)
                        span.children.forEach { parseSpan(it) }
                        builder.pop()
                    }

                    else -> {
                        span.children.forEach { parseSpan(it) }
                    }
                }
            }
        }
    }

    this.forEach { parseSpan(it) }

    // Final cleanup of trailing space
    return builder.toAnnotatedString().trimEnd()
}

private fun AnnotatedString.trimEnd(): AnnotatedString {
    var lastIndex = text.length - 1
    while (lastIndex >= 0 && (text[lastIndex].isWhitespace() || text[lastIndex] == '\u00A0')) {
        lastIndex--
    }
    if (lastIndex == -1) return AnnotatedString("")
    if (lastIndex == text.length - 1) return this
    return subSequence(0, lastIndex + 1)
}

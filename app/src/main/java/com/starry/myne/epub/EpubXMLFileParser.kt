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

import android.graphics.BitmapFactory
import android.util.Log
import com.starry.myne.epub.models.HtmlSpan
import com.starry.myne.epub.models.ReaderItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.io.File
import kotlin.io.path.invariantSeparatorsPathString

/**
 * Parses an XML file from an EPUB archive and extracts the title and body content.
 *
 * @property fileAbsolutePath The absolute path of the XML file.
 * @property data The raw data of the XML file.
 * @property zipFile The map of file paths to their respective [EpubParser.EpubFile] instances.
 * @property fragmentId The ID of the fragment to extract from the XML file.
 * @property nextFragmentId The ID of the next fragment to extract from the XML file.
 */
class EpubXMLFileParser(
    val fileAbsolutePath: String,
    val data: ByteArray,
    private val zipFile: Map<String, EpubParser.EpubFile>,
    private val fragmentId: String? = null,
    private val nextFragmentId: String? = null
) {

    /**
     * Represents the output of the XML document parsing.
     *
     * @property title The title of the XML document.
     * @property body The body content of the XML document as a list of [ReaderItem].
     */
    data class Output(val title: String?, val body: List<ReaderItem>)

    // The parent folder of the XML file.
    private val fileParentFolder: File = File(fileAbsolutePath).parentFile ?: File("")

    companion object {
        const val TAG = "EpubXMLFileParser"

        private const val HEADER_SELECTORS =
            "h1, h2, h3, h4, h5, h6, .title, .chapter-title, .header"

        /**
         * List of tags that should be treated as inline elements.
         * These tags will not trigger a line break (new ReaderItem.Text).
         */
        private val INLINE_TAGS = setOf(
            "a", "abbr", "b", "bdi", "bdo", "big", "br", "cite", "code", "del", "dfn",
            "em", "font", "i", "ins", "kbd", "mark", "q", "rp", "rt", "ruby", "s",
            "samp", "small", "span", "strike", "strong", "sub", "sup", "time", "u",
            "var", "wbr"
        )
    }


    /**
     * Parses the input data as an XML document and returns the title and body content.
     *
     * @return [Output] The title and body content of the XML document.
     */
    fun parseAsDocument(): Output {
        val document = Jsoup.parse(data.inputStream(), "UTF-8", "")

        val title: String
        val bodyContent: List<ReaderItem>
        val bodyElement: Element?

        if (fragmentId != null) {
            // Check if the fragment ID represents a <div> tag
            bodyElement = document.selectFirst("div#$fragmentId")

            if (bodyElement != null) {
                // If the fragment ID represents a <div> tag, fetch the entire body content
                Log.d(
                    TAG,
                    "Fragment ID: $fragmentId represents a <div> tag. Using the fragment ID."
                )
                val header = bodyElement.selectFirst(HEADER_SELECTORS) ?: document.selectFirst(
                    HEADER_SELECTORS
                )
                title = header?.text() ?: ""
                header?.remove()
                bodyContent = processNodes(bodyElement.childNodes())
            } else {
                Log.d(
                    TAG,
                    "Fragment ID: $fragmentId doesn't represent a <div> tag. Using the fragment and next fragment logic."
                )
                // If the fragment ID doesn't represent a <div> tag, use the fragment and next fragment logic
                val fragmentElement = document.selectFirst("#$fragmentId")
                // Check if fragment itself is a header
                val header = if (fragmentElement != null && fragmentElement.isHeader) {
                    fragmentElement
                } else {
                    fragmentElement?.selectFirst(HEADER_SELECTORS)
                }
                title = header?.text() ?: ""
                val fragmentNodes = mutableListOf<Node>()
                var currentNode: Node? = if (header != null && header == fragmentElement) {
                    fragmentElement.nextSibling()
                } else {
                    fragmentElement
                }
                val nextFragmentIdElement = if (nextFragmentId != null) {
                    document.selectFirst("#$nextFragmentId")
                } else {
                    null
                }
                header?.remove()

                while (currentNode != null && currentNode != nextFragmentIdElement) {
                    fragmentNodes.add(currentNode)
                    currentNode = getNextSibling(currentNode)
                }
                bodyContent = processNodes(fragmentNodes)
            }
        } else {
            // If no fragment ID is provided, fetch the entire body content
            Log.d(TAG, "No fragment ID provided. Fetching the entire body content.")
            bodyElement = document.body()
            val header = document.selectFirst(HEADER_SELECTORS)
            title = header?.text() ?: ""
            header?.remove()
            bodyContent = processNodes(bodyElement.childNodes())
        }

        // If the body is empty after fragment-based parsing, fallback to full file content.
        // This handles some edge cases where the fragment ID points to a location at the
        // very end of a file or just before a page break.
        val finalBody = if (bodyContent.isEmpty() && fragmentId != null) {
            Log.w(TAG, "Empty body content for fragment $fragmentId. Falling back to full file.")
            processNodes(document.body().childNodes())
        } else {
            bodyContent
        }

        return Output(
            title = title.smartTrim(),
            body = finalBody
        )
    }

    private val Element.isHeader: Boolean
        get() = tagName() in listOf("h1", "h2", "h3", "h4", "h5", "h6")
                || hasClass("title") || hasClass("chapter-title") || hasClass("header")

    /**
     * Parses the input data as an image and returns the [ReaderItem.Image].
     *
     * @param absolutePathImage The absolute path of the image file.
     * @return [ReaderItem.Image] The image path and aspect ratio.
     */
    private fun parseAsImage(absolutePathImage: String): ReaderItem.Image {
        // Use run catching so it can be run locally without crash
        val bitmap = zipFile[absolutePathImage]?.data?.runCatching {
            BitmapFactory.decodeByteArray(this, 0, this.size)
        }?.getOrNull()

        return ReaderItem.Image(
            path = absolutePathImage,
            yrel = bitmap?.let { it.height.toFloat() / it.width.toFloat() } ?: 1.45f
        )
    }

    // Traverses the XML document to find the next sibling node.
    private fun getNextSibling(currentNode: Node?): Node? {
        var nextSibling: Node? = currentNode?.nextSibling()

        if (nextSibling == null) {
            var parentNode = currentNode?.parent()
            while (parentNode != null) {
                nextSibling = parentNode.nextSibling()
                if (nextSibling != null) {
                    // If the parent's next sibling is not null, traverse its descendants
                    // to find the next node
                    return traverseDescendants(nextSibling)
                }
                parentNode = parentNode.parent()
            }
        }

        return nextSibling
    }

    // Traverses the descendants of a node to find the next node.
    private fun traverseDescendants(node: Node): Node? {
        val children = node.childNodes()
        if (children.isNotEmpty()) {
            return children.first()
        }

        val siblings = node.nextSiblingNodes()
        if (siblings.isNotEmpty()) {
            return traverseDescendants(siblings.first())
        }

        return null
    }

    // Rewrites the image node to XML for the next stage.
    private fun declareImgEntry(node: Node): ReaderItem.Image {
        val attrs = node.attributes().associate { it.key to it.value }
        val relPathEncoded = attrs["src"] ?: attrs["xlink:href"] ?: ""

        val absolutePathImage = File(fileParentFolder, relPathEncoded.decodedURL)
            .canonicalFile
            .toPath()
            .invariantSeparatorsPathString
            .removePrefix("/")

        return parseAsImage(absolutePathImage)
    }

    private fun processNodes(nodes: List<Node>): List<ReaderItem> {
        val items = mutableListOf<ReaderItem>()
        val inlineBuffer = mutableListOf<Node>()

        fun flushInlineBuffer() {
            if (inlineBuffer.isNotEmpty()) {
                val spans = inlineBuffer.map { it.toHtmlSpan() }
                if (spans.any { it.isNotBlank() }) {
                    items.add(ReaderItem.Text(spans))
                }
                inlineBuffer.clear()
            }
        }

        for (node in nodes) {
            when (node) {
                is TextNode -> {
                    inlineBuffer.add(node)
                }

                is Element -> {
                    val tagName = node.tagName()
                    when (tagName) {
                        in INLINE_TAGS -> {
                            inlineBuffer.add(node)
                        }

                        "p" -> {
                            flushInlineBuffer()
                            val spans = node.childNodes().map { it.toHtmlSpan() }
                            if (spans.any { it.isNotBlank() }) {
                                items.add(ReaderItem.Text(spans))
                            }
                        }

                        in listOf("img", "image") -> {
                            flushInlineBuffer()
                            items.add(declareImgEntry(node))
                        }

                        "pre" -> {
                            flushInlineBuffer()
                            // For code blocks, we want to preserve all whitespace.
                            items.add(ReaderItem.CodeBlock(node.wholeText()))
                        }

                        "blockquote" -> {
                            flushInlineBuffer()
                            val spans = node.childNodes().map { it.toHtmlSpan() }
                            if (spans.any { it.isNotBlank() }) {
                                items.add(ReaderItem.Blockquote(spans))
                            }
                        }

                        else -> {
                            // Recursively process children of unknown blocks, but keep them as blocks
                            flushInlineBuffer()
                            items.addAll(processNodes(node.childNodes()))
                        }
                    }
                }
            }
        }
        flushInlineBuffer()

        // Filter out leading and trailing empty items to avoid extra spacing
        // at the start or bottom of chapters.
        val finalItems = items.toMutableList()
        while (finalItems.isNotEmpty() && finalItems.first().isTrulyEmpty()) {
            finalItems.removeAt(0)
        }
        while (finalItems.isNotEmpty() && finalItems.last().isTrulyEmpty()) {
            finalItems.removeAt(finalItems.size - 1)
        }

        return finalItems
    }

    private fun ReaderItem.isTrulyEmpty(): Boolean {
        return when (this) {
            is ReaderItem.Text -> spans.all { !it.isNotBlank() }
            is ReaderItem.Blockquote -> spans.all { !it.isNotBlank() }
            is ReaderItem.CodeBlock -> code.isEmpty()
            is ReaderItem.Image -> false
        }
    }

    private fun HtmlSpan.isNotBlank(): Boolean {
        return when (this) {
            is HtmlSpan.Text -> text.any { !it.isWhitespace() && it != '\u00A0' && it != '\u200B' }
            is HtmlSpan.Tag -> name == "br" || children.any { it.isNotBlank() }
        }
    }

    private fun Node.toHtmlSpan(): HtmlSpan {
        return when (this) {
            is TextNode -> HtmlSpan.Text(this.wholeText)
            is Element -> HtmlSpan.Tag(
                name = this.tagName(),
                attributes = this.attributes().associate { it.key to it.value },
                children = this.childNodes().map { it.toHtmlSpan() }
            )

            else -> HtmlSpan.Text("")
        }
    }
}

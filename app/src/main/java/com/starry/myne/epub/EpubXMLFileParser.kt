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
import org.jsoup.Jsoup
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
     * @property body The body content of the XML document.
     */
    data class Output(val title: String?, val body: String)

    private val fileParentFolder: File = File(fileAbsolutePath).parentFile ?: File("")


    /**
     * Parses the input data as an XML document and returns the title and body content.
     *
     * @return [Output] The title and body content of the XML document.
     */
    fun parseAsDocument(): Output {
        val document = Jsoup.parse(data.inputStream(), "UTF-8", "")

        val title: String
        val bodyContent: String
        val bodyElement: org.jsoup.nodes.Element?

        if (fragmentId != null) {
            // Check if the fragment ID represents a <div> tag
            bodyElement = document.selectFirst("div#$fragmentId")

            if (bodyElement != null) {
                // If the fragment ID represents a <div> tag, fetch the entire body content
                Log.d(
                    "EpubXMLFileParser",
                    "Fragment ID: $fragmentId represents a <div> tag. Using the fragment ID."
                )
                title = document.selectFirst("h1, h2, h3, h4, h5, h6")?.text() ?: ""
                bodyElement.selectFirst("h1, h2, h3, h4, h5, h6")?.remove()
                bodyContent = getNodeStructuredText(bodyElement)
            } else {
                Log.d(
                    "EpubXMLFileParser",
                    "Fragment ID: $fragmentId doesn't represent a <div> tag. Using the fragment and next fragment logic."
                )
                // If the fragment ID doesn't represent a <div> tag, use the fragment and next fragment logic
                val fragmentElement = document.selectFirst("#$fragmentId")
                title = fragmentElement?.selectFirst("h1, h2, h3, h4, h5, h6")?.text() ?: ""
                val bodyBuilder = StringBuilder()
                var currentNode: Node? = fragmentElement?.nextSibling()
                val nextFragmentIdElement = if (nextFragmentId != null) {
                    document.selectFirst("#$nextFragmentId")
                } else {
                    null
                }
                fragmentElement?.selectFirst("h1, h2, h3, h4, h5, h6")?.remove()

                while (currentNode != null && currentNode != nextFragmentIdElement) {
                    bodyBuilder.append(getNodeStructuredText(currentNode, true) + "\n\n")
                    currentNode = getNextSibling(currentNode)
                }
                bodyContent = bodyBuilder.toString()
            }
        } else {
            // If no fragment ID is provided, fetch the entire body content
            Log.d("EpubXMLFileParser", "No fragment ID provided. Fetching the entire body content.")
            bodyElement = document.body()
            title = document.selectFirst("h1, h2, h3, h4, h5, h6")?.text() ?: ""
            document.selectFirst("h1, h2, h3, h4, h5, h6")?.remove()
            bodyContent = getNodeStructuredText(bodyElement)
        }

        return Output(
            title = title,
            body = bodyContent
        )
    }

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

    fun parseAsImage(absolutePathImage: String): String {
        // Use run catching so it can be run locally without crash
        val bitmap = zipFile[absolutePathImage]?.data?.runCatching {
            BitmapFactory.decodeByteArray(this, 0, this.size)
        }?.getOrNull()

        val text = BookTextMapper.ImgEntry(
            path = absolutePathImage,
            yrel = bitmap?.let { it.height.toFloat() / it.width.toFloat() } ?: 1.45f
        ).toXMLString()

        return "\n\n$text\n\n"
    }

    // Rewrites the image node to xml for the next stage.
    private fun declareImgEntry(node: Node): String {
        val attrs = node.attributes().associate { it.key to it.value }
        val relPathEncoded = attrs["src"] ?: attrs["xlink:href"] ?: ""

        val absolutePathImage = File(fileParentFolder, relPathEncoded.decodedURL)
            .canonicalFile
            .toPath()
            .invariantSeparatorsPathString
            .removePrefix("/")

        return parseAsImage(absolutePathImage)
    }

    private fun getPTraverse(node: Node): String {
        fun innerTraverse(node: Node): String =
            node.childNodes().joinToString("") { child ->
                when {
                    child.nodeName() == "br" -> "\n"
                    child.nodeName() == "img" -> declareImgEntry(child)
                    child.nodeName() == "image" -> declareImgEntry(child)
                    child is TextNode -> child.text()
                    else -> innerTraverse(child)
                }
            }

        val paragraph = innerTraverse(node).trim()
        return if (paragraph.isNotEmpty()) "$paragraph\n\n" else ""
    }

    private fun getNodeTextTraverse(node: Node): String {
        val children = node.childNodes()
        if (children.isEmpty())
            return ""

        return children.joinToString("") { child ->
            when {
                child.nodeName() == "p" -> getPTraverse(child)
                child.nodeName() == "br" -> "\n"
                child.nodeName() == "hr" -> "\n\n"
                child.nodeName() == "img" -> declareImgEntry(child)
                child.nodeName() == "image" -> declareImgEntry(child)
                child is TextNode -> {
                    val text = child.text().trim()
                    if (text.isEmpty()) "" else text + "\n\n"
                }

                else -> getNodeTextTraverse(child)
            }
        }
    }

    private fun getNodeStructuredText(node: Node, singleNode: Boolean = false): String {
        val nodeActions = mapOf(
            "p" to { n: Node -> getPTraverse(n) },
            "br" to { "\n" },
            "hr" to { "\n\n" },
            "img" to ::declareImgEntry,
            "image" to ::declareImgEntry
        )

        val action: (Node) -> String = { n: Node ->
            if (n is TextNode) {
                n.text().trim()
            } else {
                getNodeTextTraverse(n)
            }
        }

        val children = if (singleNode) listOf(node) else node.childNodes()
        return children.joinToString("") { child ->
            nodeActions[child.nodeName()]?.invoke(child) ?: action(child)
        }
    }
}
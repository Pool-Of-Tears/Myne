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

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.File
import java.io.InputStream
import java.net.URLDecoder
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.invariantSeparatorsPathString
import org.jsoup.nodes.Node as JsoupNode

fun parseXMLText(text: String): Document? = text.reader().runCatching {
    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(this))
}.getOrNull()

fun parseXMLFile(inputSteam: InputStream): Document? =
    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSteam)

fun parseXMLFile(byteArray: ByteArray): Document? = parseXMLFile(byteArray.inputStream())

val String.decodedURL: String get() = URLDecoder.decode(this, "UTF-8")
fun String.asFileName(): String = this.replace("/", "_")

fun String.hrefAbsolutePath(hrefRootPath: File): String {
    return File(hrefRootPath, this).canonicalFile
        .toPath()
        .invariantSeparatorsPathString
        .removePrefix("/")
}

fun ZipInputStream.entries() = generateSequence { nextEntry }

fun Document.selectFirstTag(tag: String): Node? = getElementsByTagName(tag).item(0)
fun Node.selectFirstChildTag(tag: String) = childElements.find { it.tagName == tag }
fun Node.selectChildTag(tag: String) = childElements.filter { it.tagName == tag }
fun Node.getAttributeValue(attribute: String): String? =
    attributes?.getNamedItem(attribute)?.textContent

val NodeList.elements get() = (0..length).asSequence().mapNotNull { item(it) as? Element }
val Node.childElements get() = childNodes.elements

fun JsoupNode.nextSiblingNodes(): List<org.jsoup.nodes.Node> {
    val siblings = mutableListOf<org.jsoup.nodes.Node>()
    var nextSibling = nextSibling()
    while (nextSibling != null) {
        siblings.add(nextSibling)
        nextSibling = nextSibling.nextSibling()
    }
    return siblings
}

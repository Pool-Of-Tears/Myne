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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.starry.myne.epub.models.EpubBook
import com.starry.myne.epub.models.EpubChapter
import com.starry.myne.epub.models.EpubImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream


/**
 * Parses an EPUB file and creates an [EpubBook] object.
 */
class EpubParser {

    /**
     * Represents an item in the EPUB manifest.
     * @param id The ID of the item.
     * @param absPath The absolute path of the item.
     * @param mediaType The media type of the item.
     * @param properties The properties of the item.
     */
    data class EpubManifestItem(
        val id: String, val absPath: String, val mediaType: String, val properties: String
    )

    /**
     * Represents a temporary EPUB chapter.
     * @param url The URL of the chapter.
     * @param title The title of the chapter.
     * @param body The body of the chapter.
     * @param chapterIndex The index of the chapter.
     */
    data class TempEpubChapter(
        val url: String, val title: String?, val body: String, val chapterIndex: Int
    )

    /**
     * Represents an EPUB file.
     * @param absPath The absolute path of the file.
     * @param data The file data.
     */
    data class EpubFile(val absPath: String, val data: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EpubFile

            if (absPath != other.absPath) return false
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            var result = absPath.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }

    companion object {
        const val TAG = "EpubParser"
    }

    /**
     * Creates an [EpubBook] object from an EPUB file.
     *
     * Note: The caller is responsible for closing the input stream.
     *
     * @param inputStream The input stream of the EPUB file.
     * @param shouldUseToc Whether to use the table of contents to parse chapters.
     * @return The [EpubBook] object.
     */
    suspend fun createEpubBook(inputStream: InputStream, shouldUseToc: Boolean = true): EpubBook {
        return parseAndCreateEbook(inputStream, shouldUseToc)
    }

    /**
     * Creates an [EpubBook] object from an EPUB file.
     *
     * @param filePath The file path of the EPUB file.
     * @param shouldUseToc Whether to use the table of contents to parse chapters.
     * @return The [EpubBook] object.
     */
    suspend fun createEpubBook(filePath: String, shouldUseToc: Boolean = true): EpubBook {
        val inputStream = withContext(Dispatchers.IO) { FileInputStream(filePath) }
        inputStream.use { return parseAndCreateEbook(it, shouldUseToc) }
    }

    private suspend fun parseAndCreateEbook(
        inputStream: InputStream,
        shouldUseToc: Boolean
    ): EpubBook =
        withContext(Dispatchers.IO) {
            val files = getZipFiles(inputStream)

            val container = files["META-INF/container.xml"]
                ?: throw Exception("META-INF/container.xml file missing")

            val opfFilePath = parseXMLFile(container.data)
                ?.selectFirstTag("rootfile")
                ?.getAttributeValue("full-path")
                ?.decodedURL ?: throw Exception("Invalid container.xml file")

            val opfFile = files[opfFilePath] ?: throw Exception(".opf file missing")

            val document = parseXMLFile(opfFile.data)
                ?: throw Exception(".opf file failed to parse data")
            val metadata = document.selectFirstTag("metadata")
                ?: throw Exception(".opf file metadata section missing")
            val manifest = document.selectFirstTag("manifest")
                ?: throw Exception(".opf file manifest section missing")
            val spine = document.selectFirstTag("spine")
                ?: throw Exception(".opf file spine section missing")

            val metadataTitle = metadata.selectFirstChildTag("dc:title")?.textContent
                ?: "Unknown Title"
            val metadataAuthor = metadata.selectFirstChildTag("dc:creator")?.textContent
                ?: "Unknown Author"
            val metadataLanguage = metadata.selectFirstChildTag("dc:language")?.textContent
                ?: "en"

            val metadataCoverId = metadata
                .selectChildTag("meta")
                .find { it.getAttributeValue("name") == "cover" }
                ?.getAttributeValue("content")

            val hrefRootPath = File(opfFilePath).parentFile ?: File("")

            val manifestItems = manifest.selectChildTag("item").map {
                EpubManifestItem(
                    id = it.getAttribute("id"),
                    absPath = it.getAttribute("href").decodedURL.hrefAbsolutePath(hrefRootPath),
                    mediaType = it.getAttribute("media-type"),
                    properties = it.getAttribute("properties")
                )
            }.associateBy { it.id }

            // Find the table of contents (toc.ncx) file.
            val tocFileItem = manifestItems.values.firstOrNull {
                it.absPath.endsWith(".ncx", ignoreCase = true)
            }

            /**
             * Parse chapters based on the table of contents (toc.ncx) file.
             * If it is not present, fallback to parsing with the spine logic.
             */
            val chapters = if (tocFileItem != null && shouldUseToc) {
                Log.d(TAG, "Parsing based on ToC file")
                parseUsingTocFile(tocFileItem, files, hrefRootPath)
            } else {
                Log.d(TAG, "Parsing based on spine; shouldUseToc: $shouldUseToc")
                parseUsingSpine(spine, manifestItems, files)
            }

            Log.d(TAG, "Parsing images")
            val images = parseImages(manifestItems, files)
            Log.d(TAG, "Parsing cover image")
            val coverImage = parseCoverImage(metadataCoverId, manifestItems, files)

            Log.d(TAG, "EpubBook created")
            return@withContext EpubBook(
                fileName = metadataTitle.asFileName(),
                title = metadataTitle,
                author = metadataAuthor,
                language = metadataLanguage,
                coverImage = coverImage,
                chapters = chapters,
                images = images
            )

        }

    private suspend fun getZipFiles(
        inputStream: InputStream
    ): Map<String, EpubFile> = withContext(Dispatchers.IO) {
        ZipInputStream(inputStream).let { zipInputStream ->
            zipInputStream
                .entries()
                .filterNot { it.isDirectory }
                .map { EpubFile(absPath = it.name, data = zipInputStream.readBytes()) }
                .associateBy { it.absPath }
        }
    }

    private fun findNestedNavPoints(element: Element?): List<Element> {
        val navPoints = mutableListOf<Element>()
        if (element == null) {
            return navPoints
        }
        if (element.tagName == "navPoint") {
            navPoints.add(element)
        }
        // Recursively search for nested navPoints
        for (child in element.childElements) {
            navPoints.addAll(findNestedNavPoints(child))
        }
        return navPoints
    }

    private fun parseUsingTocFile(
        tocFileItem: EpubManifestItem,
        files: Map<String, EpubFile>,
        hrefRootPath: File
    ): List<EpubChapter> {
        val tocFile = tocFileItem.let { files[it.absPath] }
        val tocDocument = tocFile?.let { parseXMLFile(it.data) }
        val tocNavPoints =
            findNestedNavPoints((tocDocument?.selectFirstTag("navMap") as Element?))

        // Parse each chapter entry.
        return tocNavPoints.flatMap { navPoint ->
            val title =
                navPoint.selectFirstChildTag("navLabel")
                    ?.selectFirstChildTag("text")?.textContent
            val chapterSrc = navPoint.selectFirstChildTag("content")?.getAttributeValue("src")
                ?.hrefAbsolutePath(hrefRootPath)

            if (chapterSrc != null) {
                // Check if the chapter source contains a fragment ID
                val (fragmentPath, fragmentId) = if ('#' in chapterSrc) {
                    val (path, id) = chapterSrc.split("#", limit = 2)
                    path to id
                } else {
                    chapterSrc to null
                }

                val nextNavPoint = navPoint.nextSibling
                val nextFragmentId = nextNavPoint?.nextSibling?.selectFirstChildTag("content")
                    ?.getAttributeValue("src")
                    ?.let { src -> src.takeIf { '#' in it }?.substringAfterLast('#') }


                val chapterFile = files[fragmentPath]
                val parser = chapterFile?.let {
                    EpubXMLFileParser(
                        fileAbsolutePath = it.absPath,
                        data = it.data,
                        zipFile = files,
                        fragmentId = fragmentId,
                        nextFragmentId = nextFragmentId
                    )
                }

                val res = parser?.parseAsDocument()
                if (res != null) {
                    listOf(
                        EpubChapter(
                            absPath = chapterSrc,
                            title = title ?: "",
                            body = res.body
                        )
                    )
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }.filter { it.body.isNotBlank() }.toList()
    }

    private fun parseUsingSpine(
        spine: Node,
        manifestItems: Map<String, EpubManifestItem>,
        files: Map<String, EpubFile>,
    ): List<EpubChapter> {
        var chapterIndex = 0
        val chapterExtensions = listOf("xhtml", "xml", "html", "htm").map { ".$it" }
        return spine
            .selectChildTag("itemref")
            .mapNotNull { manifestItems[it.getAttribute("idref")] }
            .filter { item ->
                chapterExtensions.any {
                    item.absPath.endsWith(it, ignoreCase = true)
                } || item.mediaType.startsWith("image/")
            }
            .mapNotNull { files[it.absPath]?.let { file -> it to file } }
            .map { (item, file) ->
                val parser = EpubXMLFileParser(file.absPath, file.data, files)
                if (item.mediaType.startsWith("image/")) {
                    TempEpubChapter(
                        url = "image_${file.absPath}",
                        title = null,
                        body = parser.parseAsImage(item.absPath),
                        chapterIndex = chapterIndex,
                    )
                } else {
                    val res = parser.parseAsDocument()
                    // A full chapter usually is split in multiple sequential entries,
                    // try to merge them and extract the main title of each one.
                    // Is is not perfect but better than nothing.
                    val chapterTitle = res.title ?: if (chapterIndex == 0) "" else null
                    if (chapterTitle != null)
                        chapterIndex += 1

                    TempEpubChapter(
                        url = file.absPath,
                        title = chapterTitle,
                        body = res.body,
                        chapterIndex = chapterIndex,
                    )
                }
            }.groupBy {
                it.chapterIndex
            }.map { (index, list) ->
                EpubChapter(
                    absPath = list.first().url,
                    title = list.first().title ?: "Chapter $index",
                    body = list.joinToString("\n\n") { it.body }
                )
            }.filter {
                it.body.isNotBlank()
            }
    }

    private fun parseImages(
        manifestItems: Map<String, EpubManifestItem>,
        files: Map<String, EpubFile>
    ): List<EpubImage> {
        val imageExtensions =
            listOf("png", "gif", "raw", "png", "jpg", "jpeg", "webp", "svg").map { ".$it" }
        val unlistedImages = files
            .asSequence()
            .filter { (_, file) ->
                imageExtensions.any { file.absPath.endsWith(it, ignoreCase = true) }
            }
            .map { (_, file) ->
                EpubImage(absPath = file.absPath, image = file.data)
            }

        val listedImages = manifestItems.asSequence()
            .map { it.value }
            .filter { it.mediaType.startsWith("image") }
            .mapNotNull { files[it.absPath] }
            .map { EpubImage(absPath = it.absPath, image = it.data) }

        return (listedImages + unlistedImages).distinctBy { it.absPath }.toList()
    }

    private fun parseCoverImage(
        metadataCoverId: String?,
        manifestItems: Map<String, EpubManifestItem>,
        files: Map<String, EpubFile>
    ): Bitmap? {
        val coverImage = manifestItems[metadataCoverId]
            ?.let { files[it.absPath] }
            ?.let { EpubImage(absPath = it.absPath, image = it.data) }

        return if (coverImage?.image != null) {
            BitmapFactory.decodeByteArray(coverImage.image, 0, coverImage.image.size)
        } else {
            Log.e(TAG, "Cover image not found"); null
        }
    }
}
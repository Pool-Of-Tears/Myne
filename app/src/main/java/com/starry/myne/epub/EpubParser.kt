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


package com.starry.myne.epub

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.starry.myne.epub.models.EpubBook
import com.starry.myne.epub.models.EpubChapter
import com.starry.myne.epub.models.EpubImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import kotlin.io.path.invariantSeparatorsPathString


data class EpubManifestItem(
    val id: String, val absPath: String, val mediaType: String, val properties: String
)

data class TempEpubChapter(
    val url: String, val title: String?, val body: String, val chapterIndex: Int
)

data class EpubFile(val absPath: String, val data: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EpubFile

        if (absPath != other.absPath) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = absPath.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

private suspend fun getZipFiles(
    inputStream: InputStream
): Map<String, EpubFile> = withContext(Dispatchers.IO) {
    ZipInputStream(inputStream).use { zipInputStream ->
        zipInputStream
            .entries()
            .filterNot { it.isDirectory }
            .map { EpubFile(absPath = it.name, data = zipInputStream.readBytes()) }
            .associateBy { it.absPath }
    }
}

private suspend fun parseAndCreateEpubBook(inputStream: FileInputStream): EpubBook =
    withContext(Dispatchers.Default) {
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
            ?: throw Exception(".opf metadata title tag missing")

        val metadataCoverId = metadata
            .selectChildTag("meta")
            .find { it.getAttributeValue("name") == "cover" }
            ?.getAttributeValue("content")

        val hrefRootPath = File(opfFilePath).parentFile ?: File("")
        fun String.hrefAbsolutePath() = File(hrefRootPath, this).canonicalFile
            .toPath()
            .invariantSeparatorsPathString
            .removePrefix("/")

        val manifestItems = manifest.selectChildTag("item").map {
            EpubManifestItem(
                id = it.getAttribute("id"),
                absPath = it.getAttribute("href").decodedURL.hrefAbsolutePath(),
                mediaType = it.getAttribute("media-type"),
                properties = it.getAttribute("properties")
            )
        }.associateBy { it.id }


        var chapterIndex = 0
        val chapterExtensions = listOf("xhtml", "xml", "html").map { ".$it" }
        val chapters = spine
            .selectChildTag("itemref")
            .mapNotNull { manifestItems[it.getAttribute("idref")] }
            .filter { item ->
                chapterExtensions.any {
                    item.absPath.endsWith(it, ignoreCase = true)
                } || item.mediaType.startsWith("image/")
            }
            .mapNotNull { files[it.absPath]?.let { file -> it to file } }
            .mapIndexed { index, (item, file) ->
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
                    // Is is not perfect but better than dealing with a table of contents
                    val chapterTitle = res.title ?: if (index == 0) metadataTitle else null
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

        val images = (listedImages + unlistedImages).distinctBy { it.absPath }

        val coverImage = manifestItems[metadataCoverId]
            ?.let { files[it.absPath] }
            ?.let { EpubImage(absPath = it.absPath, image = it.data) }

        val coverImageBm: Bitmap? = if (coverImage?.image != null) {
            BitmapFactory.decodeByteArray(coverImage.image, 0, coverImage.image.size)
        } else {
            null
        }
        return@withContext EpubBook(
            fileName = metadataTitle.asFileName(),
            title = metadataTitle,
            coverImage = coverImageBm,
            chapters = chapters.toList(),
            images = images.toList()
        )
    }

suspend fun createEpubBook(filePath: String): EpubBook {
    val inputStream = withContext(Dispatchers.IO) {
        FileInputStream(filePath)
    }
    return parseAndCreateEpubBook(inputStream)
}

suspend fun createEpubBook(inputStream: FileInputStream) = parseAndCreateEpubBook(inputStream)

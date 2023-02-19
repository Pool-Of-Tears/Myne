package com.starry.myne.epub

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.starry.myne.epub.models.EpubBook
import com.starry.myne.epub.models.EpubChapter
import com.starry.myne.epub.models.EpubImage
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream


data class EpubManifestItem(
    val id: String, val href: String, val mediaType: String, val properties: String
)

data class TempEpubChapter(
    val url: String, val title: String?, val body: String, val chapterIndex: Int
)

fun createEpubBook(filePath: String): EpubBook {
    val inputStream = FileInputStream(filePath)
    val zipFile = ZipInputStream(inputStream).use { zipInputStream ->
        zipInputStream.entries().filterNot { it.isDirectory }
            .associate { it.name to (it to zipInputStream.readBytes()) }
    }

    val container =
        zipFile["META-INF/container.xml"] ?: throw Exception("META-INF/container.xml file missing")

    val opfFilePath = parseXMLFile(container.second)?.selectFirstTag("rootfile")
        ?.getAttributeValue("full-path")?.decodedURL
        ?: throw Exception("Invalid container.xml file")

    val opfFile = zipFile[opfFilePath] ?: throw Exception(".opf file missing")

    val document = parseXMLFile(opfFile.second) ?: throw Exception(".opf file failed to parse data")
    val metadata =
        document.selectFirstTag("metadata") ?: throw Exception(".opf file metadata section missing")
    val manifest =
        document.selectFirstTag("manifest") ?: throw Exception(".opf file manifest section missing")
    val spine =
        document.selectFirstTag("spine") ?: throw Exception(".opf file spine section missing")

    val bookTitle = metadata.selectFirstChildTag("dc:title")?.textContent
        ?: throw Exception(".opf metadata title tag missing")
    val bookUrl = bookTitle.asFileName()
    val rootPath = File(opfFilePath).parentFile ?: File("")
    fun String.absPath() = File(rootPath, this).path.replace("""\""", "/").removePrefix("/")

    val items = manifest.selectChildTag("item").map {
        EpubManifestItem(
            id = it.getAttribute("id"),
            href = it.getAttribute("href").decodedURL,
            mediaType = it.getAttribute("media-type"),
            properties = it.getAttribute("properties")
        )
    }.associateBy { it.id }

    val idRef = spine.selectChildTag("itemref").map { it.getAttribute("idref") }

    var chapterIndex = 0
    val chapterExtensions = listOf("xhtml", "xml", "html").map { ".$it" }
    val chapters = idRef.mapNotNull { items[it] }
        .filter { item -> chapterExtensions.any { item.href.endsWith(it, ignoreCase = true) } }
        .mapNotNull { zipFile[it.href.absPath()] }.mapIndexed { index, (entry, byteArray) ->
            val res = EpubXMLFileParser(entry.name, byteArray, zipFile).parse()
            // A full chapter usually is split in multiple sequential entries,
            // try to merge them and extract the main title of each one.
            // Is is not perfect but better than dealing with a table of contents
            val chapterTitle = res.title ?: if (index == 0) bookTitle else null
            if (chapterTitle != null) chapterIndex += 1

            TempEpubChapter(
                url = "$bookUrl/${entry.name}",
                title = chapterTitle,
                body = res.body,
                chapterIndex = chapterIndex,
            )
        }.groupBy {
            it.chapterIndex
        }.map { (_, list) ->
            EpubChapter(url = list.first().url,
                title = list.first().title!!,
                body = list.joinToString("\n\n") { it.body })
        }.filter {
            it.body.isNotBlank()
        }

    val listedImages = items.values.asSequence().filter { it.mediaType.startsWith("image/") }
        .mapNotNull { zipFile[it.href.absPath()] }
        .map { (entry, byteArray) -> EpubImage(path = entry.name, image = byteArray) }

    val imageExtensions = listOf("png", "gif", "raw", "png", "jpg", "jpeg", "webp").map { ".$it" }
    val unlistedImages = zipFile.values.asSequence().filterNot { (entry, _) -> entry.isDirectory }
        .filter { (entry, _) -> imageExtensions.any { entry.name.endsWith(it, ignoreCase = true) } }
        .map { (entry, byteArray) -> EpubImage(path = entry.name, image = byteArray) }

    val images = (listedImages + unlistedImages).distinctBy { it.path }

    val coverImage = items.values.asSequence().filter { it.mediaType.startsWith("image/") }
        .find { it.properties == "cover-image" }?.let { zipFile[it.href.absPath()] }
        ?.let { (entry, byteArray) -> EpubImage(path = entry.name, image = byteArray) }

    val coverImageBm: Bitmap? = if (coverImage?.image != null) {
        BitmapFactory.decodeByteArray(coverImage.image, 0, coverImage.image.size)
    } else {
        null
    }

    return EpubBook(
        fileName = bookUrl,
        title = bookTitle,
        coverImage = coverImageBm,
        chapters = chapters.toList(),
        images = images.toList()
    )
}
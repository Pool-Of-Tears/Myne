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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.starry.myne.epub.cache.EpubCache
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
import java.util.concurrent.ThreadLocalRandom
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream


/**
 * Parses an EPUB file and creates an [EpubBook] object.
 */
class EpubParser(private val context: Context) {

    /**
     * Represents an EPUB document.
     *
     * @param metadata The metadata of the document.
     * @param manifest The manifest of the document.
     * @param spine The spine of the document.
     * @param opfFilePath The file path of the OPF file.
     */
    data class EpubDocument(
        val metadata: Node, val manifest: Node, val spine: Node, val opfFilePath: String
    )

    /**
     * Represents an item in the EPUB manifest.
     *
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
     *
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
     *
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

    private val epubCache = EpubCache(context)

    /**
     * Creates an [EpubBook] object from an EPUB file.
     *
     * @param filePath The file path of the EPUB file.
     * @param shouldUseToc Whether to use the table of contents (ToC) file for parsing.
     * @return The [EpubBook] object.
     */
    suspend fun createEpubBook(filePath: String, shouldUseToc: Boolean = true): EpubBook {
        return withContext(Dispatchers.IO) {
            val epubBook = epubCache.get(filePath)
            if (epubBook != null) {
                Log.d(TAG, "EpubBook found in cache")
                return@withContext epubBook
            }
            Log.d(TAG, "Parsing EPUB file: $filePath")
            val files = getZipFilesFromFile(filePath)
            val document = createEpubDocument(files)
            val book = parseAndCreateEbook(files, document, shouldUseToc)
            epubCache.put(book, filePath)
            return@withContext book
        }
    }

    /**
     * Creates an [EpubBook] object from an EPUB input stream.
     *
     * Note: Caller is responsible for closing the input stream.
     *
     * @param inputStream The input stream of the EPUB file.
     * @param shouldUseToc Whether to use the table of contents (ToC) file for parsing.
     * @return The [EpubBook] object.
     */
    suspend fun createEpubBook(inputStream: InputStream, shouldUseToc: Boolean = true): EpubBook {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Parsing EPUB input stream")
            val (files, document) = getZipFilesAndDocument(inputStream)
            parseAndCreateEbook(files, document, shouldUseToc)
        }
    }

    /**
     * Checks if an EPUB book is cached.
     *
     * @param filePath The file path of the EPUB file.
     * @return True if the book is cached, false otherwise.
     */
    fun isBookCached(filePath: String): Boolean {
        return epubCache.isCached(filePath)
    }

    /**
     * Removes an EPUB book from the cache.
     *
     * @param filePath The file path of the EPUB file.
     */
    fun removeBookFromCache(filePath: String): Boolean {
        return epubCache.remove(filePath)
    }

    /**
     * Parses and creates an [EpubBook] object from the EPUB files and document.
     * This function is called from [createEpubBook] and [createEpubBook].
     *
     * @param files The EPUB files.
     * @param document The EPUB document.
     * @param shouldUseToc Whether to use the table of contents (ToC) file for parsing.
     * @return The [EpubBook] object.
     */
    private suspend fun parseAndCreateEbook(
        files: Map<String, EpubFile>, document: EpubDocument, shouldUseToc: Boolean
    ): EpubBook = withContext(Dispatchers.IO) {
        val metadataTitle =
            document.metadata.selectFirstChildTag("dc:title")?.textContent ?: "Unknown Title"
        val metadataAuthor =
            document.metadata.selectFirstChildTag("dc:creator")?.textContent ?: "Unknown Author"
        val metadataLanguage =
            document.metadata.selectFirstChildTag("dc:language")?.textContent ?: "en"

        val metadataCoverId = getMetadataCoverId(document.metadata)
        val hrefRootPath = File(document.opfFilePath).parentFile ?: File("")

        val manifestItems = getManifestItems(manifest = document.manifest, hrefRootPath)

        // Find the table of contents (toc.ncx) file.
        val tocFileItem = manifestItems.values.firstOrNull {
            it.absPath.endsWith(".ncx", ignoreCase = true)
        }

        // Find the nested navPoints in the table of contents (toc.ncx) file.
        val tocNavPoints = tocFileItem?.let { navItem ->
            val tocFile = files[navItem.absPath]
            val tocDocument = tocFile?.let { parseXMLFile(it.data) }
            findNestedNavPoints((tocDocument?.selectFirstTag("navMap") as Element?))
        }

        // Determine the method of parsing chapters based on the presence of ToC and
        // the shouldUseToc flag. We also check if the ToC file has more than one navPoint
        // to ensure that it is a valid ToC file.
        val chapters = if (shouldUseToc && !tocNavPoints.isNullOrEmpty() && tocNavPoints.size > 1) {
            Log.d(TAG, "Parsing based on ToC file")
            parseUsingTocFile(tocNavPoints, files, hrefRootPath, document, manifestItems)
        } else {
            Log.d(TAG, "Parsing based on spine; shouldUseToc: $shouldUseToc")
            parseUsingSpine(document.spine, manifestItems, files)
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

    /**
     * Get all of the files located in the EPUB archive and the EPUB document (content.opf).
     *
     * @param inputStream The input stream of the EPUB file.
     * @return A pair of the EPUB files and the EPUB document.
     */
    private suspend fun getZipFilesAndDocument(
        inputStream: InputStream
    ): Pair<Map<String, EpubFile>, EpubDocument> {
        var files = getZipFilesFromStream(inputStream)
        val epubDocument = try {
            createEpubDocument(files)
        } catch (exc: EpubParserException) {
            // In some rare cases, the ZipInputStream does not contain or fails to read all of the files
            // required to parse the EPUB archive, even though the zip file itself contains them.
            // In such cases, retry parsing the EPUB file by directly using the ZipFile API.
            // Since ZipFile requires a file path, we need to create a temporary file from the input stream.
            //
            // The reasons for this issue are unknown and may be related to how the EPUB file is compressed,
            // i.e., whether it is missing some metadata or file/folder entry in its header, or how the
            // ZipInputStream reads the file.
            //
            // If anyone knows the exact reason for this issue or has dealt with it before, please
            // let me know, or feel free to create a PR with a better solution.
            if (exc.message == "META-INF/container.xml file missing"
                || exc.message == ".opf file missing"
            ) {
                Log.e(
                    TAG, "Failed to parse EPUB file using ZipInputStream "
                            + "due to missing files required for parsing! "
                            + "Retrying using ZipFile by creating temporary file "
                            + "from input stream.", exc
                )
                Log.w(TAG, "Resetting input stream position to beginning")
                withContext(Dispatchers.IO) {
                    (inputStream as FileInputStream).channel.position(0)
                }
                files = getZipFilesFromTempFile(inputStream)
                createEpubDocument(files)
            } else {
                throw exc
            }
        }

        return Pair(files, epubDocument)
    }

    /**
     * Get all of the files located in the EPUB archive.
     * This method is called from [createEpubBook].
     *
     * @param filePath The file path of the EPUB file.
     * @return The EPUB files.
     */
    private fun getZipFilesFromFile(filePath: String): Map<String, EpubFile> {
        return ZipFile(filePath).use { zipFile ->
            zipFile.entries().asSequence()
                .filterNot { it.isDirectory }
                .map { entry ->
                    val content = zipFile.getInputStream(entry).readBytes()
                    EpubFile(absPath = entry.name, data = content)
                }
                .associateBy { it.absPath }
        }
    }

    /**
     * Copy the input stream to a temporary file and get all of the files located in the EPUB archive.
     * This is a fallback method if the ZipInputStream fails to read all of the files required for parsing.
     * This method is called from [getZipFilesAndDocument].
     *
     * @param inputStream The input stream of the EPUB file.
     * @return The EPUB files.
     */
    private fun getZipFilesFromTempFile(inputStream: InputStream): Map<String, EpubFile> {
        Log.w(TAG, "Copying input stream to a temporary file")
        val tempFile = File(context.cacheDir, "_zip_temp.epub")
        tempFile.outputStream().use { output -> inputStream.copyTo(output) }
        Log.w(TAG, "Input stream copied to temporary file")
        val epubFile = getZipFilesFromFile(tempFile.absolutePath)
        Log.w(TAG, "EPUB file created from ZipFile")
        tempFile.delete()
        Log.w(TAG, "Temporary file deleted")
        return epubFile
    }

    /**
     * Get all of the files located in the EPUB archive from the input stream.
     * This method is called from [getZipFilesAndDocument] and [getZipFilesFromTempFile].
     *
     * @param inputStream The input stream of the EPUB file.
     * @return The EPUB files.
     */
    private suspend fun getZipFilesFromStream(
        inputStream: InputStream
    ): Map<String, EpubFile> = withContext(Dispatchers.IO) {
        ZipInputStream(inputStream).let { zipInputStream ->
            zipInputStream.entries().filterNot { it.isDirectory }
                .map { EpubFile(absPath = it.name, data = zipInputStream.readBytes()) }
                .associateBy { it.absPath }
        }
    }

    /**
     * Create an [EpubDocument] object from the EPUB files.
     * This method is called from [createEpubBook] and [getZipFilesAndDocument].
     *
     * @param files The EPUB files.
     * @return The [EpubDocument] object.
     */
    @Throws(EpubParserException::class)
    private fun createEpubDocument(files: Map<String, EpubFile>): EpubDocument {
        val container = files["META-INF/container.xml"]
            ?: throw EpubParserException("META-INF/container.xml file missing")

        val opfFilePath = parseXMLFile(container.data)?.selectFirstTag("rootfile")
            ?.getAttributeValue("full-path")?.decodedURL
            ?: throw EpubParserException("Invalid container.xml file")

        val opfFile = files[opfFilePath] ?: throw EpubParserException(".opf file missing")

        val document = parseXMLFile(opfFile.data)
            ?: throw EpubParserException(".opf file failed to parse data")
        val metadata = document.selectFirstTag("metadata")
            ?: document.selectFirstTag("opf:metadata")
            ?: throw EpubParserException(".opf file metadata section missing")
        val manifest = document.selectFirstTag("manifest")
            ?: document.selectFirstTag("opf:manifest")
            ?: throw EpubParserException(".opf file manifest section missing")
        val spine = document.selectFirstTag("spine")
            ?: document.selectFirstTag("opf:spine")
            ?: throw EpubParserException(".opf file spine section missing")

        return EpubDocument(metadata, manifest, spine, opfFilePath)
    }

    /**
     * Get the cover ID from the EPUB metadata.
     * This method is called from [parseAndCreateEbook].
     *
     * @param metadata The metadata of the EPUB document.
     * @return The cover ID.
     */
    private fun getMetadataCoverId(metadata: Node): String? {
        return metadata.selectChildTag("meta")
            .ifEmpty { metadata.selectChildTag("opf:meta") }
            .find { it.getAttributeValue("name") == "cover" }?.getAttributeValue("content")
    }

    /**
     * Get the manifest items from the EPUB manifest.
     * This method is called from [parseAndCreateEbook].
     *
     * @param manifest The manifest of the EPUB document.
     * @param hrefRootPath The root path of the href attribute.
     * @return The manifest items.
     */
    private fun getManifestItems(
        manifest: Node,
        hrefRootPath: File
    ): Map<String, EpubManifestItem> {
        return manifest.selectChildTag("item")
            .ifEmpty { manifest.selectChildTag("opf:item") }
            .map {
                EpubManifestItem(
                    id = it.getAttribute("id"),
                    absPath = it.getAttribute("href").decodedURL.hrefAbsolutePath(hrefRootPath),
                    mediaType = it.getAttribute("media-type"),
                    properties = it.getAttribute("properties")
                )
            }.associateBy { it.id }
    }

    /**
     * Find all the nested navPoints in the table of contents (ToC) file.
     * This method is called from [parseAndCreateEbook].
     *
     * @param element The element to search for nested navPoints.
     * @return The list of nested navPoints.
     */
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

    /**
     * Generate a unique ID for a chapter.
     *
     * @return The generated ID.
     */
    private fun generateId(): String {
        val timestamp = System.currentTimeMillis()
        val randomSuffix = ThreadLocalRandom.current().nextInt(1000, 9999)
        return "$timestamp-$randomSuffix"
    }

    /**
     * Parse the EPUB file using the table of contents (ToC) file.
     * This method is called from [parseAndCreateEbook].
     *
     * @param tocNavPoints The list of navPoints in the ToC file.
     * @param files The EPUB files.
     * @param hrefRootPath The root path of the href attribute.
     * @param document The EPUB document.
     * @param manifestItems The manifest items.
     * @return The list of parsed chapters.
     */
    private fun parseUsingTocFile(
        tocNavPoints: List<Element>,
        files: Map<String, EpubFile>,
        hrefRootPath: File,
        document: EpubDocument,
        manifestItems: Map<String, EpubManifestItem>
    ): List<EpubChapter> {
        // Parse each chapter entry.
        val chapters = tocNavPoints.flatMapIndexed { index, navPoint ->
            val title =
                navPoint.selectFirstChildTag("navLabel")?.selectFirstChildTag("text")?.textContent
            val chapterSrc = navPoint.selectFirstChildTag("content")?.getAttributeValue("src")
                ?.decodedURL?.hrefAbsolutePath(hrefRootPath)

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
                            chapterId = generateId(),
                            absPath = chapterSrc,
                            title = title?.takeIf { it.isNotEmpty() } ?: "Chapter $index",
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

        // If 25% or more chapters have empty bodies, that means the ToC file is not
        // reliable and has incorrect references. In such cases, switch to spine-based parsing.
        val emptyChapterThreshold = 0.25
        val totalChapters = tocNavPoints.size
        val emptyChapters = totalChapters - chapters.size

        if (emptyChapters.toDouble() / totalChapters >= emptyChapterThreshold) {
            Log.w(TAG, "25% or more chapters have empty bodies; switching to spine-based parsing")
            return parseUsingSpine(document.spine, manifestItems, files)
        }

        return chapters // Return the parsed chapters from the ToC file.
    }

    /**
     * Parse the EPUB file using the spine.
     * This method is called from [parseAndCreateEbook].
     *
     * @param spine The spine of the EPUB document.
     * @param manifestItems The manifest items.
     * @param files The EPUB files.
     * @return The list of parsed chapters.
     */
    private fun parseUsingSpine(
        spine: Node,
        manifestItems: Map<String, EpubManifestItem>,
        files: Map<String, EpubFile>,
    ): List<EpubChapter> {
        var chapterIndex = 0
        val chapterExtensions = listOf("xhtml", "xml", "html", "htm").map { ".$it" }
        return spine.selectChildTag("itemref")
            .ifEmpty { spine.selectChildTag("opf:itemref") }
            .mapNotNull { manifestItems[it.getAttribute("idref")] }
            .filter { item ->
                chapterExtensions.any {
                    item.absPath.endsWith(it, ignoreCase = true)
                } || item.mediaType.startsWith("image/")
            }.mapNotNull {
                files[it.absPath]?.let { file -> it to file }
            }.map { (item, file) ->
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
                    if (chapterTitle != null) chapterIndex += 1

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
                    chapterId = generateId(),
                    absPath = list.first().url,
                    title = list.first().title?.takeIf { it.isNotBlank() } ?: "Chapter $index",
                    body = list.joinToString("\n\n") { it.body }
                )
            }.filter {
                it.body.isNotBlank()
            }
    }

    /**
     * Parse the EPUB images.
     * This method is called from [parseAndCreateEbook].
     *
     * @param manifestItems The manifest items.
     * @param files The EPUB files.
     * @return The list of parsed images.
     */
    private fun parseImages(
        manifestItems: Map<String, EpubManifestItem>, files: Map<String, EpubFile>
    ): List<EpubImage> {
        val imageExtensions =
            listOf("png", "gif", "raw", "png", "jpg", "jpeg", "webp", "svg").map { ".$it" }
        val unlistedImages = files.asSequence().filter { (_, file) ->
            imageExtensions.any { file.absPath.endsWith(it, ignoreCase = true) }
        }.map { (_, file) ->
            EpubImage(absPath = file.absPath, image = file.data)
        }

        val listedImages =
            manifestItems.asSequence()
                .map { it.value }.filter { it.mediaType.startsWith("image") }
                .mapNotNull { files[it.absPath] }
                .map { EpubImage(absPath = it.absPath, image = it.data) }

        return (listedImages + unlistedImages).distinctBy { it.absPath }.toList()
    }

    /**
     * Parse the EPUB cover image.
     * This method is called from [parseAndCreateEbook].
     *
     * @param metadataCoverId The cover ID from the EPUB metadata.
     * @param manifestItems The manifest items.
     * @param files The EPUB files.
     * @return The cover image.
     */
    private fun parseCoverImage(
        metadataCoverId: String?,
        manifestItems: Map<String, EpubManifestItem>,
        files: Map<String, EpubFile>
    ): Bitmap? {
        val coverImage = manifestItems[metadataCoverId]?.let { files[it.absPath] }
            ?.let { EpubImage(absPath = it.absPath, image = it.data) }

        return if (coverImage?.image != null) {
            BitmapFactory.decodeByteArray(coverImage.image, 0, coverImage.image.size)
        } else {
            Log.e(TAG, "Cover image not found"); null
        }
    }
}
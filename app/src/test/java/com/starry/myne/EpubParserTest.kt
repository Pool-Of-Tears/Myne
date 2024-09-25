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

package com.starry.myne

import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import com.starry.myne.epub.EpubParser
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33]) // Run on Android 13
class EpubParserTest {

    private lateinit var epubParser: EpubParser
    private lateinit var testEpubFile: ByteArray
    private lateinit var testEpubWithTocFile: ByteArray

    @Before
    fun setup() {
        epubParser = EpubParser(RuntimeEnvironment.getApplication())
        // Create a sample EPUB file for testing
        testEpubFile = createSampleEpubFile(hasToc = false)
        testEpubWithTocFile = createSampleEpubFile(hasToc = true)
    }

    @Test
    fun testCreateEpubBookFromInputStream() = runBlocking {
        val inputStream = ByteArrayInputStream(testEpubFile)
        val epubBook = epubParser.createEpubBook(inputStream)

        assertThat(epubBook.title).isEqualTo("Test Book")
        assertThat(epubBook.author).isEqualTo("Test Author")
        assertThat(epubBook.language).isEqualTo("en")
        assertThat(epubBook.chapters.map { it.title }).containsExactly("Chapter 1", "Chapter 2")
        assertThat(epubBook.images).isNotEmpty()
        assertThat(epubBook.coverImage).isNotNull()

    }

    @Test
    fun testCreateEpubBookFromFilePath(): Unit = runBlocking {
        val tempFile = createTempEpubFile(testEpubFile)
        val epubBook = epubParser.createEpubBook(tempFile.absolutePath)

        assertThat(epubBook.title).isEqualTo("Test Book")
        assertThat(epubBook.author).isEqualTo("Test Author")
        assertThat(epubBook.language).isEqualTo("en")
        assertThat(epubBook.chapters.map { it.title }).containsExactly("Chapter 1", "Chapter 2")
        assertThat(epubBook.images).isNotEmpty()
        assertThat(epubBook.coverImage).isNotNull()

        tempFile.delete()
    }

    @Test
    fun testParseUsingTocFile(): Unit = runBlocking {
        val inputStream = ByteArrayInputStream(testEpubWithTocFile)
        val epubBook = epubParser.createEpubBook(inputStream, shouldUseToc = true)

        assertThat(epubBook.chapters).isNotEmpty()
        assertThat(epubBook.chapters.map { it.title }).containsExactly("Chapter 1", "Chapter 2")
    }

    @Test
    fun testParseUsingSpine(): Unit = runBlocking {
        val inputStream = ByteArrayInputStream(testEpubFile)
        // Should not use TOC since it's not present in the EPUB file
        val epubBook = epubParser.createEpubBook(inputStream, shouldUseToc = true)

        assertThat(epubBook.chapters).isNotEmpty()
        assertThat(epubBook.chapters.map { it.title }).containsExactly("Chapter 1", "Chapter 2")
    }

    @Test
    fun testParseUsingSpineWithToc(): Unit = runBlocking {
        val inputStream = ByteArrayInputStream(testEpubWithTocFile)
        // Should not use TOC since it's disabled.
        val epubBook = epubParser.createEpubBook(inputStream, shouldUseToc = false)

        assertThat(epubBook.chapters).isNotEmpty()
        assertThat(epubBook.chapters.map { it.title }).containsExactly("Chapter 1", "Chapter 2")
    }

    @Test
    fun testParseImages() = runBlocking {
        val inputStream = ByteArrayInputStream(testEpubFile)
        val epubBook = epubParser.createEpubBook(inputStream)

        assertThat(epubBook.images).isNotEmpty()
        assertThat(epubBook.images.map { it.absPath }).contains("image1.jpg")
        assertThat(epubBook.images.map { it.absPath }).contains("image2.png")
    }

    @Test
    fun testParseCoverImage() = runBlocking {
        val inputStream = ByteArrayInputStream(testEpubFile)
        val epubBook = epubParser.createEpubBook(inputStream)

        assertThat(epubBook.coverImage).isNotNull()
        assertThat(epubBook.coverImage).isInstanceOf(Bitmap::class.java)
        assertThat(epubBook.coverImage!!.byteCount).isGreaterThan(0)
    }

    // Creates a sample EPUB file with two chapters and two images
    private fun createSampleEpubFile(hasToc: Boolean): ByteArray {
        val containerXml = """
           <?xml version="1.0" encoding="UTF-8"?>
           <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
               <rootfiles>
                   <rootfile full-path="content.opf" media-type="application/oebps-package+xml"/>
               </rootfiles>
           </container>
       """.trimIndent()

        val contentOpf = """
           <?xml version="1.0" encoding="UTF-8"?>
           <package xmlns="http://www.idpf.org/2007/opf" unique-identifier="BookId" version="2.0">
               <metadata xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:opf="http://www.idpf.org/2007/opf">
                   <dc:title>Test Book</dc:title>
                   <dc:creator opf:role="aut">Test Author</dc:creator>
                   <dc:language>en</dc:language>
                   <meta name="cover" content="cover-image"/>
               </metadata>
               <manifest>
                   <item id="cover-image" href="cover.jpg" media-type="image/jpeg" properties="cover-image"/>
                   <item id="chapter1" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
                   <item id="chapter2" href="chapter2.xhtml" media-type="application/xhtml+xml"/>
                   <item id="image1" href="image1.jpg" media-type="image/jpeg"/>
                   <item id="image2" href="image2.png" media-type="image/png"/>
                   ${if (hasToc) "<item id=\"toc\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>" else ""}
               </manifest>
               <spine toc="${if (hasToc) "toc" else ""}">
                   <itemref idref="chapter1"/>
                   <itemref idref="chapter2"/>
               </spine>
           </package>
       """.trimIndent()

        val chapter1 = """
           <?xml version="1.0" encoding="UTF-8"?>
           <!DOCTYPE html>
           <html xmlns="http://www.w3.org/1999/xhtml">
               <head>
                   <title>Chapter 1</title>
               </head>
               <body>
                   <h1>Chapter 1</h1>
                   <p>This is the content of Chapter 1.</p>
                   <img src="image1.jpg" alt="Image 1"/>
               </body>
           </html>
       """.trimIndent()

        val chapter2 = """
           <?xml version="1.0" encoding="UTF-8"?>
           <!DOCTYPE html>
           <html xmlns="http://www.w3.org/1999/xhtml">
               <head>
                   <title>Chapter 2</title>
               </head>
               <body>
                   <h1>Chapter 2</h1>
                   <p>This is the content of Chapter 2.</p>
                   <img src="image2.png" alt="Image 2"/>
               </body>
           </html>
       """.trimIndent()

        val tocNcx = """
           <?xml version="1.0" encoding="UTF-8"?>
           <!DOCTYPE ncx PUBLIC "-//NISO//DTD ncx 2005-1//EN" "http://www.daisy.org/z3986/2005/ncx-2005-1.dtd">
           <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
               <head>
                   <meta name="dtb:uid" content="urn:uuid:1234567890"/>
                   <meta name="dtb:depth" content="2"/>
                   <meta name="dtb:totalPageCount" content="0"/>
                   <meta name="dtb:maxPageNumber" content="0"/>
                   <docTitle>
                       <text>Test Book</text>
                   </docTitle>
               </head>
               <navMap>
                   <navPoint id="navPoint-1" playOrder="1">
                       <navLabel>
                           <text>Chapter 1</text>
                       </navLabel>
                       <content src="chapter1.xhtml"/>
                   </navPoint>
                   <navPoint id="navPoint-2" playOrder="2">
                       <navLabel>
                           <text>Chapter 2</text>
                       </navLabel>
                       <content src="chapter2.xhtml"/>
                   </navPoint>
               </navMap>
           </ncx>
       """.trimIndent()

        val coverImage = Random.nextBytes(1024)

        val image1 = Random.nextBytes(2048)
        val image2 = Random.nextBytes(4096)

        return ByteArrayOutputStream().use { outputStream ->
            ZipOutputStream(outputStream).use { zipOutputStream ->
                zipOutputStream.putNextEntry(ZipEntry("META-INF/container.xml"))
                zipOutputStream.write(containerXml.toByteArray())
                zipOutputStream.closeEntry()

                zipOutputStream.putNextEntry(ZipEntry("content.opf"))
                zipOutputStream.write(contentOpf.toByteArray())
                zipOutputStream.closeEntry()

                zipOutputStream.putNextEntry(ZipEntry("chapter1.xhtml"))
                zipOutputStream.write(chapter1.toByteArray())
                zipOutputStream.closeEntry()

                zipOutputStream.putNextEntry(ZipEntry("chapter2.xhtml"))
                zipOutputStream.write(chapter2.toByteArray())
                zipOutputStream.closeEntry()

                if (hasToc) {
                    zipOutputStream.putNextEntry(ZipEntry("toc.ncx"))
                    zipOutputStream.write(tocNcx.toByteArray())
                    zipOutputStream.closeEntry()
                }

                zipOutputStream.putNextEntry(ZipEntry("cover.jpg"))
                zipOutputStream.write(coverImage)
                zipOutputStream.closeEntry()

                zipOutputStream.putNextEntry(ZipEntry("image1.jpg"))
                zipOutputStream.write(image1)
                zipOutputStream.closeEntry()

                zipOutputStream.putNextEntry(ZipEntry("image2.png"))
                zipOutputStream.write(image2)
                zipOutputStream.closeEntry()
            }
            outputStream.toByteArray()
        }
    }

    private fun createTempEpubFile(content: ByteArray): File {
        val tempFile = File.createTempFile("test.epub", null)
        tempFile.writeBytes(content)
        return tempFile
    }
}
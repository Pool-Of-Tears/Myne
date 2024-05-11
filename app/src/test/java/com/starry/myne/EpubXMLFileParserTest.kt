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

import com.google.common.truth.Truth.assertThat
import com.starry.myne.epub.EpubParser
import com.starry.myne.epub.EpubXMLFileParser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33]) // Run on Android 13
class EpubXMLFileParserTest {

    private lateinit var parser: EpubXMLFileParser
    private lateinit var zipFile: Map<String, EpubParser.EpubFile>

    @Before
    fun setUp() {
        val sampleXmlData = createSampleXmlData().toByteArray()
        val imageData = createSampleImageData()
        zipFile = mapOf(
            "file1.xml" to EpubParser.EpubFile("file1.xml", sampleXmlData),
            "image1.jpg" to EpubParser.EpubFile("image1.jpg", imageData)
        )
        parser = EpubXMLFileParser(
            "file1.xml",
            sampleXmlData,
            zipFile,
            fragmentId = "fragment1",
            nextFragmentId = "fragment2"
        )
    }

    @Test
    fun testParseAsDocument() {
        val output = parser.parseAsDocument()

        assertThat(output.title).isEqualTo("Sample Title")
        assertThat(output.body).contains("This is a sample paragraph.")
        assertThat(output.body).contains("<img src=\"image1.jpg\" yrel=\"1.00\">")
        assertThat(output.body).doesNotContain("This fragment will be skipped")
    }

    @Test
    fun testParseAsImage() {
        val imagePath = File("image1.jpg").absolutePath
        val expectedOutput = "\n\n<img src=\"$imagePath\" yrel=\"1.45\">\n\n"
        val output = parser.parseAsImage(imagePath)
        assertThat(output).isEqualTo(expectedOutput)
    }

    private fun createSampleXmlData(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml">
                <head>
                    <title>Sample Title</title>
                </head>
                <body>
                    <div id="fragment1">
                        <h1>Sample Title</h1>
                        <p>This is a sample paragraph.</p>
                        <img src="image1.jpg" alt="Sample Image" />
                    </div>
                    <div id="fragment2">
                        <!-- This fragment will be skipped -->
                    </div>
                </body>
            </html>
        """.trimIndent()
    }

    private fun createSampleImageData(): ByteArray {
        // Create a sample image data byte array
        return ByteArray(1024).apply {
            for (i in indices) {
                this[i] = 0xFF.toByte()
            }
        }
    }
}
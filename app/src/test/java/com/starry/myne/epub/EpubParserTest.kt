package com.starry.myne.epub

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.FileInputStream


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class EpubParserTest {
    private lateinit var inputStream: FileInputStream

    @Before
    fun setup() {
        inputStream = FileInputStream(javaClass.classLoader!!.getResource("The+Idiot.epub").file)
    }

    @Test
    fun `test epub parser output`() = runTest {
        val epubBook = createEpubBook(inputStream)
        assertThat(epubBook.title).isEqualTo("The Idiot")
        assertThat(epubBook.fileName).isEqualTo("The Idiot")
        assertThat(epubBook.chapters.size).isEqualTo(10)
        assertThat(epubBook.chapters.first().title).contains("Gutenberg eBook of The Idiot")
        assertThat(epubBook.chapters.first().body).contains("Fyodor Dostoyevsky")
        assertThat(epubBook.chapters.first().body).contains("www.gutenberg.org")
        assertThat(epubBook.chapters.last().title).isEqualTo("THE FULL PROJECT GUTENBERG LICENSE")
        assertThat(epubBook.chapters.last().body).contains("General Terms of Use")
    }
}
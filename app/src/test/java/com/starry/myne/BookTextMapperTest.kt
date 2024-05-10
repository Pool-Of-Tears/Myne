package com.starry.myne

import com.google.common.truth.Truth.assertThat
import com.starry.myne.epub.BookTextMapper
import org.junit.Test


class BookTextMapperTest {

    @Test
    fun testImgEntryFromXMLStringV1() {
        val xmlString = """<img src="image.jpg" yrel="1.23">"""
        val imgEntry = BookTextMapper.ImgEntry.fromXMLString(xmlString)

        assertThat(imgEntry).isNotNull()
        assertThat(imgEntry?.path).isEqualTo("image.jpg")
        assertThat(imgEntry?.yrel).isEqualTo(1.23f)
    }

    @Test
    fun testImgEntryFromXMLStringV0() {
        val xmlString = """<img yrel="1.45">image.jpg</img>"""
        val imgEntry = BookTextMapper.ImgEntry.fromXMLString(xmlString)

        assertThat(imgEntry).isNotNull()
        assertThat(imgEntry?.path).isEqualTo("image.jpg")
        assertThat(imgEntry?.yrel).isEqualTo(1.45f)
    }

    @Test
    fun testImgEntryFromInvalidXMLString() {
        val invalidXmlString = """<invalidTag>image.jpg</invalidTag>"""
        val imgEntry = BookTextMapper.ImgEntry.fromXMLString(invalidXmlString)

        assertThat(imgEntry).isNull()
    }

    @Test
    fun testImgEntryToXMLStringV1() {
        val imgEntry = BookTextMapper.ImgEntry("image.jpg", 1.23f)
        val expectedXmlString = """<img src="image.jpg" yrel="1.23">"""

        assertThat(imgEntry.toXMLString()).isEqualTo(expectedXmlString)
    }

    // Commented out since the v0 version is deprecated
    /*
    @Test
    fun testImgEntryToXMLStringV0() {
        val imgEntry = BookTextMapper.ImgEntry("image.jpg", 1.45f)
        val expectedXmlString = """<img yrel="1.45">image.jpg</img>"""

        assertThat(imgEntry.toXMLString()).isEqualTo(expectedXmlString)
    }
    */
}
package com.starry.myne

import com.starry.myne.helpers.Utils.prettyCount
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class UtilsTest {
    @Test
    fun `prettyCount with a small number`() = runTest {
        assertEquals("100", prettyCount(100))
    }

    @Test
    fun `prettyCount with a large number`() = runTest {
        assertEquals("1.0k", prettyCount(1000))
        assertEquals("1.5k", prettyCount(1500))
        assertEquals("10.0k", prettyCount(10000))
        assertEquals("999.0k", prettyCount(999000))
        assertEquals("1.0M", prettyCount(1000000))
        assertEquals("1.5M", prettyCount(1500000))
        assertEquals("10.0M", prettyCount(10000000))
        assertEquals("999.0M", prettyCount(999000000))
        assertEquals("1.0B", prettyCount(1000000000))
        assertEquals("1.5B", prettyCount(1500000000))
        assertEquals("10.0B", prettyCount(10000000000))
        assertEquals("999.0B", prettyCount(999000000000))
        assertEquals("1.0T", prettyCount(1000000000000))
        assertEquals("1.5T", prettyCount(1500000000000))
        assertEquals("10.0T", prettyCount(10000000000000))
        assertEquals("999.0T", prettyCount(999000000000000))
        assertEquals("1.0P", prettyCount(1000000000000000))
        assertEquals("1.5P", prettyCount(1500000000000000))
        assertEquals("10.0P", prettyCount(10000000000000000))
        assertEquals("999.0P", prettyCount(999000000000000000))
        assertEquals("1.0E", prettyCount(1000000000000000000))
    }
}
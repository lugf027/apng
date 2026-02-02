package io.github.lugf027.apng.core

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class PngSignatureTest {
    @Test
    fun testValidSignature() {
        val validData = byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10, 1, 2, 3)
        assertTrue(PngSignature.isValid(validData))
    }

    @Test
    fun testInvalidSignature() {
        val invalidData = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        assertFalse(PngSignature.isValid(invalidData))
    }

    @Test
    fun testShortData() {
        val shortData = byteArrayOf(137.toByte(), 80, 78, 71)
        assertFalse(PngSignature.isValid(shortData))
    }

    @Test
    fun testEmptyData() {
        val emptyData = byteArrayOf()
        assertFalse(PngSignature.isValid(emptyData))
    }
}

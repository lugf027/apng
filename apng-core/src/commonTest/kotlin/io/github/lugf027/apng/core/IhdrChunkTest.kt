package io.github.lugf027.apng.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IhdrChunkTest {
    @Test
    fun testValidIhdr() {
        // Create a valid IHDR chunk data
        // Width: 800 (0x320), Height: 600 (0x258)
        val data = byteArrayOf(
            0x00, 0x00, 0x03, 0x20,  // width = 800
            0x00, 0x00, 0x02, 0x58,  // height = 600
            0x08,                     // bit depth = 8
            0x06,                     // color type = 6 (RGBA)
            0x00,                     // compression method = 0
            0x00,                     // filter method = 0
            0x00                      // interlace method = 0
        )

        val ihdr = IhdrChunk.parse(data)
        assertEquals(800, ihdr.width)
        assertEquals(600, ihdr.height)
        assertEquals(8, ihdr.bitDepth)
        assertEquals(6, ihdr.colorType)
    }

    @Test
    fun testInvalidDimensions() {
        val data = byteArrayOf(
            0x00, 0x00, 0x00, 0x00,  // width = 0 (invalid)
            0x00, 0x00, 0x02, 0x58,  // height = 600
            0x08, 0x06, 0x00, 0x00, 0x00
        )

        assertFailsWith<InvalidChunkException> {
            IhdrChunk.parse(data)
        }
    }

    @Test
    fun testShortData() {
        val shortData = byteArrayOf(0x00, 0x00, 0x00)
        assertFailsWith<InvalidChunkException> {
            IhdrChunk.parse(shortData)
        }
    }
}

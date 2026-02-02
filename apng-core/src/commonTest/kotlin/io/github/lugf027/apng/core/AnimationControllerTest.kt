package io.github.lugf027.apng.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class AnimationControllerTest {
    @Test
    fun testStaticImage() {
        val staticImage = ApngImage(
            width = 100,
            height = 100,
            isAnimated = false,
            numFrames = 1,
            frames = emptyList()
        )

        val controller = AnimationController(staticImage)
        assertFalse(controller.isAnimated)
        assertEquals(1, controller.frameCount)
    }

    @Test
    fun testAnimationPlayback() {
        val frames = listOf(
            ApngFrame(0, byteArrayOf(), delayNum = 100, delayDen = 1000),
            ApngFrame(1, byteArrayOf(), delayNum = 100, delayDen = 1000)
        )

        val animImage = ApngImage(
            width = 100,
            height = 100,
            isAnimated = true,
            numFrames = 2,
            frames = frames
        )

        val controller = AnimationController(animImage)
        assertTrue(controller.isAnimated)
        assertEquals(2, controller.frameCount)

        controller.play()
        assertTrue(controller.playing)

        controller.pause()
        assertFalse(controller.playing)
    }

    @Test
    fun testPlaybackSpeed() {
        val frames = listOf(
            ApngFrame(0, byteArrayOf(), delayNum = 100, delayDen = 1000)
        )

        val animImage = ApngImage(
            width = 100,
            height = 100,
            isAnimated = true,
            numFrames = 1,
            frames = frames
        )

        val controller = AnimationController(animImage)
        assertEquals(1.0f, controller.getPlaybackSpeed())

        controller.setPlaybackSpeed(2.0f)
        assertEquals(2.0f, controller.getPlaybackSpeed())

        // With 2x speed, delay should be halved
        val delay = controller.getCurrentFrameDelay()
        assertEquals(50L, delay)
    }
}

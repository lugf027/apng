package io.github.lugf027.apng.internal.platform

import androidx.compose.ui.graphics.ImageBitmap

internal expect fun decodeImageBitmap(bytes: ByteArray): ImageBitmap

internal expect fun composeFrames(
    canvasWidth: Int,
    canvasHeight: Int,
    frameCount: Int,
    getFramePngBytes: (index: Int) -> ByteArray,
    getFrameXOffset: (index: Int) -> Int,
    getFrameYOffset: (index: Int) -> Int,
    getFrameWidth: (index: Int) -> Int,
    getFrameHeight: (index: Int) -> Int,
    getFrameDisposeOp: (index: Int) -> Int,
    getFrameBlendOp: (index: Int) -> Int,
): List<ImageBitmap>

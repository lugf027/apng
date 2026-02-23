package io.github.lugf027.apng.internal.apng

internal enum class DisposeOp(val value: Byte) {
    NONE(0),
    BACKGROUND(1),
    PREVIOUS(2);

    companion object {
        fun fromValue(value: Byte): DisposeOp = entries.first { it.value == value }
    }
}

internal enum class BlendOp(val value: Byte) {
    SOURCE(0),
    OVER(1);

    companion object {
        fun fromValue(value: Byte): BlendOp = entries.first { it.value == value }
    }
}

package io.github.lugf027.apng.compose

/**
 * Web 平台的 APNG 合成数据加载实现
 * 目前提供基础实现，后续可以使用 JS Canvas API 增强
 */
internal actual fun loadApngComposition(data: ByteArray): ApngComposition {
    // Web 平台暂时抛出不支持异常
    // TODO: 使用 JS Canvas API 实现完整的 APNG 解析
    throw UnsupportedOperationException("APNG parsing is not yet supported on Web platform")
}

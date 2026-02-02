package io.github.lugf027.apng.compose

/**
 * Web 平台文件加载实现
 * 
 * 注意：Web 平台由于浏览器沙箱安全限制，无法直接访问本地文件系统
 * 支持的加载方式：
 * 1. 字节数组（ByteArray）- 直接从内存加载
 * 2. 网络 URL - 通过 HTTP/HTTPS 加载（需要服务器支持 CORS）
 * 3. Base64 数据 URL - data:image/png;base64,...
 * 
 * 不支持的加载方式：
 * - 本地文件系统路径（/path/to/file.apng）
 * - Compose Resources（编译时资源）
 */
actual suspend fun loadFileData(path: String): ByteArray {
    throw UnsupportedOperationException(
        """
        Web 平台不支持直接从文件系统加载文件。
        
        请使用以下方式替代：
        1. 使用网络 URL 加载：ApngSource.Url("https://example.com/animation.apng")
        2. 使用字节数组加载：ApngSource.Bytes(byteArray)
        3. 使用 Base64 数据 URL：data:image/png;base64,...
        
        详见文档：https://example.com/web-loading-guide
        """.trimIndent()
    )
}

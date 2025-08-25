package platform

expect object Compression {
    suspend fun decompressXZ(compressedData: ByteArray): Result<ByteArray>
    suspend fun decompressBZ2(compressedData: ByteArray): Result<ByteArray>
}

/**
 * 支持的压缩格式枚举
 */
enum class CompressionFormat {
    XZ,
    BZ2,
    NONE
}

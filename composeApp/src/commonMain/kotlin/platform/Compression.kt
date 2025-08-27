package platform

expect object Compression {
    suspend fun decompressXZ(compressedData: ByteArray): Result<ByteArray>
    suspend fun decompressBZ2(compressedData: ByteArray): Result<ByteArray>
}

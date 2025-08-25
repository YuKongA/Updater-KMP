package platform

actual object Compression {
    actual suspend fun decompressXZ(compressedData: ByteArray): Result<ByteArray> {
        TODO("Not yet implemented")
    }

    actual suspend fun decompressBZ2(compressedData: ByteArray): Result<ByteArray> {
        TODO("Not yet implemented")
    }
}
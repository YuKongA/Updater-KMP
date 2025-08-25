package platform

actual object FileSystem {
    actual suspend fun getDownloadsDirectory(): String {
        TODO("Not yet implemented")
    }
}

actual suspend fun saveFileWithProgress(
    data: ByteArray,
    fileName: String,
    folder: String?,
    onProgress: (FileSaveProgress) -> Unit
): Result<String> {
    TODO("Not yet implemented")
}
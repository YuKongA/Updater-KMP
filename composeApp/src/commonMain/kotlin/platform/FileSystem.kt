package platform

expect object FileSystem {
    suspend fun getDownloadsDirectory(): String
}

data class FileSaveProgress(
    val bytesWritten: Long,
    val totalBytes: Long,
    val progress: Float,
    val isCompleted: Boolean,
    val filePath: String? = null,
    val error: String? = null
)

expect suspend fun saveFileWithProgress(
    data: ByteArray,
    fileName: String,
    folder: String? = null,
    onProgress: (FileSaveProgress) -> Unit = {}
): Result<String>

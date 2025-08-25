package platform

/**
 * 跨平台文件系统操作接口
 */
expect object FileSystem {

    /**
     * 获取下载目录路径
     */
    suspend fun getDownloadsDirectory(): String

    /**
     * 将字节数组保存到指定路径
     * @param data 要保存的数据
     * @param fileName 文件名
     * @param directory 目录路径，如果为空则使用默认下载目录
     * @return 保存成功的文件路径
     */
    suspend fun saveFile(
        data: ByteArray,
        fileName: String,
        directory: String? = null
    ): Result<String>

    /**
     * 检查文件是否存在
     */
    suspend fun fileExists(filePath: String): Boolean

    /**
     * 获取文件大小
     */
    suspend fun getFileSize(filePath: String): Long?

    /**
     * 删除文件
     */
    suspend fun deleteFile(filePath: String): Boolean

    /**
     * 创建目录
     */
    suspend fun createDirectory(directoryPath: String): Boolean
}

/**
 * 文件保存进度回调
 */
data class FileSaveProgress(
    val bytesWritten: Long,
    val totalBytes: Long,
    val progress: Float,
    val isCompleted: Boolean,
    val filePath: String? = null,
    val error: String? = null
)

/**
 * 带进度的文件保存
 */
expect suspend fun saveFileWithProgress(
    data: ByteArray,
    fileName: String,
    directory: String? = null,
    onProgress: (FileSaveProgress) -> Unit = {}
): Result<String>

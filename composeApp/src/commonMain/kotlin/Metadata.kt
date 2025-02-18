import misc.ZipFileUtil.locateCentralDirectory
import misc.ZipFileUtil.locateLocalFileHeader
import misc.ZipFileUtil.locateLocalFileOffset

suspend fun getMetadata(url: String): String {
    val metadataPath = "META-INF/com/android/metadata"
    val endBytes = ByteArray(4096)

    HttpClient.init(url)
    HttpClient.seek(HttpClient.length() - 4096)
    HttpClient.read(endBytes)
    val centralDirectoryInfo = locateCentralDirectory(endBytes, HttpClient.length())
    HttpClient.seek(centralDirectoryInfo.offset)
    val centralDirectory = ByteArray(centralDirectoryInfo.size.toInt())
    HttpClient.read(centralDirectory)
    val localHeaderOffset = locateLocalFileHeader(centralDirectory, metadataPath)

    if (localHeaderOffset != -1L) {
        val localHeaderBytes = ByteArray(256)
        HttpClient.seek(localHeaderOffset)
        HttpClient.read(localHeaderBytes)
        val metadataOffset = locateLocalFileOffset(localHeaderBytes) + localHeaderOffset

        val fileSize = (localHeaderBytes[22].toInt() and 0xff) or
                ((localHeaderBytes[23].toInt() and 0xff) shl 8) or
                ((localHeaderBytes[24].toInt() and 0xff) shl 16) or
                ((localHeaderBytes[25].toInt() and 0xff) shl 24)

        val chunkSize = 8192
        val metadataContent = ByteArray(fileSize)
        var bytesRead = 0

        HttpClient.seek(metadataOffset)
        while (bytesRead < fileSize) {
            val remaining = fileSize - bytesRead
            val currentChunkSize = minOf(chunkSize, remaining)
            val chunk = ByteArray(currentChunkSize)
            HttpClient.read(chunk)
            chunk.forEachIndexed { index, byte ->
                metadataContent[bytesRead + index] = byte
            }
            bytesRead += currentChunkSize
        }
        return metadataContent.decodeToString()
    }
    return ""
}

internal fun getMetadataValue(metadata: String, prefix: String): String {
    return metadata.lineSequence()
        .find { it.startsWith(prefix) }
        ?.substringAfter(prefix)
        ?: ""
}


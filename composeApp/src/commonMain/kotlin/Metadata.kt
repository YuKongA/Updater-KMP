import misc.ZipFileUtil.locateCentralDirectory
import misc.ZipFileUtil.locateLocalFileHeader
import misc.ZipFileUtil.locateLocalFileOffset

private const val METADATA_PATH = "META-INF/com/android/metadata"
private const val CHUNK_SIZE = 8192
private const val END_BYTES_SIZE = 4096
private const val LOCAL_HEADER_SIZE = 256

suspend fun getMetadata(url: String): String {
    HttpClient.init(url)

    val endBytes = ByteArray(END_BYTES_SIZE).also { bytes ->
        HttpClient.seek(HttpClient.length() - END_BYTES_SIZE)
        HttpClient.read(bytes)
    }

    val centralDirectoryInfo = locateCentralDirectory(endBytes, HttpClient.length())
    val centralDirectory = ByteArray(centralDirectoryInfo.size.toInt()).also { bytes ->
        HttpClient.seek(centralDirectoryInfo.offset)
        HttpClient.read(bytes)
    }

    val localHeaderOffset = locateLocalFileHeader(centralDirectory, METADATA_PATH)
    if (localHeaderOffset == -1L) return ""

    val localHeaderBytes = ByteArray(LOCAL_HEADER_SIZE).also { bytes ->
        HttpClient.seek(localHeaderOffset)
        HttpClient.read(bytes)
    }

    val metadataOffset = locateLocalFileOffset(localHeaderBytes) + localHeaderOffset
    val fileSize = localHeaderBytes.getFileSize()

    return ByteArray(fileSize).also { content ->
        HttpClient.seek(metadataOffset)
        readByChunks(content, fileSize)
    }.decodeToString()
}

private fun ByteArray.getFileSize(): Int =
    (this[22].toInt() and 0xff) or
            ((this[23].toInt() and 0xff) shl 8) or
            ((this[24].toInt() and 0xff) shl 16) or
            ((this[25].toInt() and 0xff) shl 24)

private suspend fun readByChunks(content: ByteArray, fileSize: Int) {
    var bytesRead = 0
    while (bytesRead < fileSize) {
        val remaining = fileSize - bytesRead
        val currentChunkSize = minOf(CHUNK_SIZE, remaining)
        val chunk = ByteArray(currentChunkSize)
        HttpClient.read(chunk)
        chunk.copyInto(content, bytesRead)
        bytesRead += currentChunkSize
    }
}

fun getMetadataValue(metadata: String, prefix: String): String =
    metadata.lineSequence()
        .firstOrNull { it.startsWith(prefix) }
        ?.substringAfter(prefix)
        .orEmpty()
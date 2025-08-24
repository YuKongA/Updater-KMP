import chromeos_update_engine.DeltaArchiveManifest
import chromeos_update_engine.PartitionUpdate
import data.PayloadHelper
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import misc.ZipFileUtil.locateCentralDirectory
import misc.ZipFileUtil.locateLocalFileHeader
import misc.ZipFileUtil.locateLocalFileOffset
import pbandk.decodeFromByteArray
import platform.httpClientPlatform
import kotlin.math.min

private const val PAYLOAD_PATH = "payload.bin"
private const val CHUNK_SIZE = 1024 * 1024 // 1MB chunks for better performance
private const val PARALLEL_DOWNLOADS = 4 // Number of parallel download threads
private const val MAX_CHUNK_SIZE = 8 * 1024 * 1024 // 8MB max chunk size
private const val RETRY_ATTEMPTS = 3
private const val PAYLOAD_MAGIC = "CrAU"
private const val PAYLOAD_HEADER_SIZE = 24
private const val END_BYTES_SIZE = 4096
private const val TIMEOUT_MS = 30000L

class PayloadAnalyzer private constructor() {

    companion object {
        private val instance by lazy { PayloadAnalyzer() }

        suspend fun analyzePayload(url: String): PayloadHelper.PayloadInfo? =
            instance.extractPayloadInfo(url)

        fun downloadPartition(
            url: String,
            payloadInfo: PayloadHelper.PayloadInfo,
            partitionName: String
        ): Flow<ByteArray> = instance.downloadPartitionData(url, payloadInfo, partitionName)

    }

    private val client = httpClientPlatform()

    /**
     * 分析 payload.bin 文件并提取基本信息
     */
    private suspend fun extractPayloadInfo(url: String): PayloadHelper.PayloadInfo? {
        return withTimeout(TIMEOUT_MS) {
            try {
                val payloadOffset = findPayloadOffset(url) ?: return@withTimeout null
                parsePayloadHeader(url, payloadOffset)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * 在 ZIP 文件中查找 payload.bin 的偏移位置
     */
    private suspend fun findPayloadOffset(url: String): Long? {
        val fileLength = getFileLength(url) ?: return null
        if (fileLength == 0L) return null

        val actualEndBytesSize = min(fileLength, END_BYTES_SIZE.toLong()).toInt()
        val endBytes = readRange(url, fileLength - actualEndBytesSize, actualEndBytesSize) ?: return null

        val centralDirectoryInfo = locateCentralDirectory(endBytes, fileLength)
        if (centralDirectoryInfo.offset == -1L || centralDirectoryInfo.size == -1L ||
            centralDirectoryInfo.offset < 0 || centralDirectoryInfo.size <= 0 ||
            centralDirectoryInfo.offset + centralDirectoryInfo.size > fileLength
        ) return null

        val centralDirectory = readRange(url, centralDirectoryInfo.offset, centralDirectoryInfo.size.toInt()) ?: return null

        val localHeaderOffset = locateLocalFileHeader(centralDirectory, PAYLOAD_PATH)
        if (localHeaderOffset == -1L || localHeaderOffset < 0 || localHeaderOffset >= fileLength) return null

        val maxBytesForLocalHeader = min(fileLength - localHeaderOffset, 256L).toInt()
        if (maxBytesForLocalHeader < 30) return null

        val localHeaderBytes = readRange(url, localHeaderOffset, maxBytesForLocalHeader) ?: return null
        val payloadInternalOffset = locateLocalFileOffset(localHeaderBytes)

        if (payloadInternalOffset == -1L || payloadInternalOffset > maxBytesForLocalHeader) return null

        return localHeaderOffset + payloadInternalOffset
    }

    /**
     * 解析 payload.bin 文件头
     */
    private suspend fun parsePayloadHeader(url: String, payloadOffset: Long): PayloadHelper.PayloadInfo? {
        // 读取 payload 头部
        val headerBytes = readRange(url, payloadOffset, PAYLOAD_HEADER_SIZE) ?: return null

        // 验证魔数
        val magic = headerBytes.sliceArray(0..3).decodeToString()
        if (magic != PAYLOAD_MAGIC) return null

        // 解析版本和大小信息
        val fileFormatVersion = headerBytes.getLongBE(4)
        val manifestSize = headerBytes.getLongBE(12)
        val metadataSignatureSize = headerBytes.getIntBE(20)

        val header = PayloadHelper.PayloadHeader(
            fileFormatVersion = fileFormatVersion,
            manifestSize = manifestSize,
            metadataSignatureSize = metadataSignatureSize
        )

        // 读取 manifest 数据
        val manifestOffset = payloadOffset + PAYLOAD_HEADER_SIZE
        val manifestBytes = readRange(url, manifestOffset, manifestSize.toInt()) ?: return null

        // 解析 DeltaArchiveManifest
        val manifest = try {
            // 使用 pbandk 的 decodeFromByteArray 方法
            DeltaArchiveManifest.decodeFromByteArray(manifestBytes)
        } catch (e: Exception) {
            return null
        }

        val dataOffset = manifestOffset + manifestSize + metadataSignatureSize
        val blockSize = manifest.blockSize ?: 4096

        return PayloadHelper.PayloadInfo(
            fileName = PAYLOAD_PATH,
            header = header,
            deltaArchiveManifest = manifest,
            dataOffset = dataOffset,
            blockSize = blockSize,
            archiveSize = getFileLength(url) ?: 0L,
            isPath = false
        )
    }

    /**
     * 提取分区列表
     */
    private suspend fun extractPartitionList(url: String): List<PayloadHelper.PartitionInfo> {
        val payloadInfo = extractPayloadInfo(url) ?: return emptyList()

        return payloadInfo.deltaArchiveManifest.partitions.map { partition ->
            val rawSize = calculatePartitionRawSize(partition)
            val compressedSize = calculatePartitionCompressedSize(partition)

            PayloadHelper.PartitionInfo(
                partitionName = partition.partitionName,
                size = compressedSize,
                rawSize = rawSize,
                sha256 = partition.newPartitionInfo?.hash?.array?.joinToString("") { byte ->
                    val value = byte.toInt() and 0xFF
                    if (value < 16) "0${value.toString(16)}" else value.toString(16)
                }?.take(16) ?: "",
                isDownloading = false,
                progress = 0f
            )
        }
    }

    /**
     * 分段下载指定分区的数据 - 优化版本，支持并行下载
     */
    private fun downloadPartitionData(
        url: String,
        payloadInfo: PayloadHelper.PayloadInfo,
        partitionName: String
    ): Flow<ByteArray> = flow {
        val partition = payloadInfo.deltaArchiveManifest.partitions.firstOrNull {
            it.partitionName == partitionName
        } ?: return@flow

        for (operation in partition.operations) {
            if (operation.dataOffset != null && operation.dataLength != null) {
                val dataOffset = payloadInfo.dataOffset + operation.dataOffset
                val dataLength = operation.dataLength.toInt()

                if (dataLength > 0) {
                    if (dataLength > MAX_CHUNK_SIZE) {
                        downloadLargeChunkParallel(url, dataOffset, dataLength).forEach { chunk ->
                            emit(chunk)
                        }
                    } else {
                        downloadSmallChunk(url, dataOffset, dataLength)?.let { chunk ->
                            emit(chunk)
                        }
                    }
                }
            }
        }
    }

    /**
     * 并行下载大块数据
     */
    private suspend fun downloadLargeChunkParallel(
        url: String,
        startOffset: Long,
        totalSize: Int
    ): List<ByteArray> = coroutineScope {
        val chunks = mutableListOf<ByteArray>()
        val chunkSize = min(CHUNK_SIZE, totalSize / PARALLEL_DOWNLOADS)
        var currentOffset = 0

        // 创建下载任务列表
        val downloadTasks = mutableListOf<Pair<Int, suspend () -> ByteArray?>>()

        while (currentOffset < totalSize) {
            val remaining = totalSize - currentOffset
            val actualChunkSize = min(chunkSize, remaining)
            val offset = currentOffset

            downloadTasks.add(offset to {
                downloadChunkWithRetry(url, startOffset + offset, actualChunkSize)
            })

            currentOffset += actualChunkSize
        }

        // 分批并行下载
        val batchSize = PARALLEL_DOWNLOADS
        for (i in downloadTasks.indices step batchSize) {
            val batch = downloadTasks.drop(i).take(batchSize)
            val results = batch.map { (offset, task) ->
                async { offset to task() }
            }.awaitAll()

            // 按顺序添加结果
            results.sortedBy { it.first }.forEach { (_, data) ->
                data?.let { chunks.add(it) }
            }
        }

        chunks
    }

    /**
     * 下载小块数据（单线程）
     */
    private suspend fun downloadSmallChunk(
        url: String,
        startOffset: Long,
        size: Int
    ): ByteArray? {
        return downloadChunkWithRetry(url, startOffset, size)
    }

    /**
     * 带重试的下载块
     */
    private suspend fun downloadChunkWithRetry(
        url: String,
        start: Long,
        size: Int,
        attempt: Int = 0
    ): ByteArray? {
        if (attempt >= RETRY_ATTEMPTS) return null

        try {
            return readRangeOptimized(url, start, size)
        } catch (e: Exception) {
            if (attempt < RETRY_ATTEMPTS - 1) {
                // exponential backoff
                delay(1000L * (1L shl attempt))
                return downloadChunkWithRetry(url, start, size, attempt + 1)
            }
            return null
        }
    }

    /**
     * 计算分区原始大小
     */
    private fun calculatePartitionRawSize(partition: PartitionUpdate): Long {
        return partition.newPartitionInfo?.size ?: 0L
    }

    /**
     * 计算分区压缩大小
     */
    private fun calculatePartitionCompressedSize(partition: PartitionUpdate): Long {
        return partition.operations.sumOf { operation ->
            operation.dataLength ?: 0L
        }
    }

    /**
     * 获取文件长度
     */
    private suspend fun getFileLength(url: String): Long? {
        return try {
            val response = client.head(url) {
                header(HttpHeaders.Range, "bytes=0-0")
            }

            response.headers[HttpHeaders.ContentRange]?.let { contentRange ->
                val parts = contentRange.split("/")
                if (parts.size > 1) {
                    parts[1].toLongOrNull()?.let { if (it > 0) return it }
                }
            }
            response.headers[HttpHeaders.ContentLength]?.toLongOrNull()?.let { if (it > 0) return it }

            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 读取指定范围的数据 - 优化版本
     */
    private suspend fun readRangeOptimized(url: String, start: Long, size: Int): ByteArray? {
        if (size == 0) return ByteArray(0)
        if (size < 0 || start < 0) return null

        return try {
            withTimeout(TIMEOUT_MS) {
                val response = client.get(url) {
                    header(HttpHeaders.Range, "bytes=$start-${start + size - 1}")
                    // 添加 Keep-Alive 和其他优化头部
                    header("Connection", "keep-alive")
                    header("Accept-Encoding", "identity") // 避免压缩影响 range 请求
                }

                val bytes = ByteArray(size)
                val channel = response.bodyAsChannel()
                var totalBytesRead = 0

                while (totalBytesRead < size) {
                    val remaining = size - totalBytesRead
                    val bytesReadThisTurn = channel.readAvailable(
                        bytes,
                        totalBytesRead,
                        remaining
                    )

                    if (bytesReadThisTurn == -1) {
                        // EOF reached before we got all expected bytes
                        return@withTimeout if (totalBytesRead > 0) {
                            bytes.sliceArray(0 until totalBytesRead)
                        } else null
                    }

                    totalBytesRead += bytesReadThisTurn
                }

                bytes
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 读取指定范围的数据 - 保持兼容性
     */
    private suspend fun readRange(url: String, start: Long, size: Int): ByteArray? {
        return readRangeOptimized(url, start, size)
    }
}

// 扩展函数用于字节数组操作
private fun ByteArray.getLongBE(offset: Int): Long {
    return ((this[offset].toLong() and 0xff) shl 56) or
            ((this[offset + 1].toLong() and 0xff) shl 48) or
            ((this[offset + 2].toLong() and 0xff) shl 40) or
            ((this[offset + 3].toLong() and 0xff) shl 32) or
            ((this[offset + 4].toLong() and 0xff) shl 24) or
            ((this[offset + 5].toLong() and 0xff) shl 16) or
            ((this[offset + 6].toLong() and 0xff) shl 8) or
            (this[offset + 7].toLong() and 0xff)
}

private fun ByteArray.getIntBE(offset: Int): Int {
    return ((this[offset].toInt() and 0xff) shl 24) or
            ((this[offset + 1].toInt() and 0xff) shl 16) or
            ((this[offset + 2].toInt() and 0xff) shl 8) or
            (this[offset + 3].toInt() and 0xff)
}

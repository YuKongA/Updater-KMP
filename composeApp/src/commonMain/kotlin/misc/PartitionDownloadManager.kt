package misc

import PayloadAnalyzer
import data.PayloadHelper
import kotlinx.coroutines.flow.catch
import pbandk.ByteArr
import kotlin.math.log10
import kotlin.math.pow

class PartitionDownloadManager {

    data class DownloadProgress(
        val partitionName: String,
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val progress: Float,
        val isCompleted: Boolean,
        val error: String? = null
    )

    companion object {
        suspend fun downloadPartitionToFile(
            url: String,
            payloadInfo: PayloadHelper.PayloadInfo,
            partitionName: String,
            onProgress: (DownloadProgress) -> Unit = {}
        ): Result<ByteArray> {
            return try {
                val partitionData = mutableListOf<ByteArray>()
                var totalBytesDownloaded = 0L
                val partition = payloadInfo.deltaArchiveManifest.partitions.firstOrNull {
                    it.partitionName == partitionName
                } ?: return Result.failure(Exception("Partition $partitionName not found"))

                val totalSize = partition.operations.sumOf { it.dataLength ?: 0L }

                PayloadAnalyzer.downloadPartition(url, payloadInfo, partitionName)
                    .catch { e ->
                        onProgress(
                            DownloadProgress(
                                partitionName = partitionName,
                                bytesDownloaded = totalBytesDownloaded,
                                totalBytes = totalSize,
                                progress = 0f,
                                isCompleted = false,
                                error = e.message
                            )
                        )
                        throw e
                    }
                    .collect { chunk ->
                        partitionData.add(chunk)
                        totalBytesDownloaded += chunk.size

                        val progress = if (totalSize > 0) {
                            (totalBytesDownloaded.toFloat() / totalSize.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        onProgress(
                            DownloadProgress(
                                partitionName = partitionName,
                                bytesDownloaded = totalBytesDownloaded,
                                totalBytes = totalSize,
                                progress = progress,
                                isCompleted = false
                            )
                        )
                    }

                // 合并所有数据块
                val totalData = ByteArray(totalBytesDownloaded.toInt())
                var offset = 0
                partitionData.forEach { chunk ->
                    chunk.copyInto(totalData, offset)
                    offset += chunk.size
                }

                Result.success(totalData)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun getAllPartitionsInfo(payloadInfo: PayloadHelper.PayloadInfo): List<PayloadHelper.PartitionInfo> {
            return payloadInfo.deltaArchiveManifest.partitions.map { partition ->
                val rawSize = partition.newPartitionInfo?.size ?: 0L
                val compressedSize = partition.operations.sumOf { it.dataLength ?: 0L }

                PayloadHelper.PartitionInfo(
                    partitionName = partition.partitionName,
                    size = compressedSize,
                    rawSize = rawSize,
                    sha256 = formatHashToHex(partition.newPartitionInfo?.hash),
                    isDownloading = false,
                    progress = 0f
                )
            }
        }

        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
            val size = bytes / 1024.0.pow(digitGroups.toDouble())
            return "${(size * 100).toLong() / 100.0} ${units[digitGroups]}"
        }

        fun formatHashToHex(hash: ByteArr?): String {
            return hash?.array?.joinToString("") { byte ->
                val value = byte.toInt() and 0xFF
                if (value < 16) "0${value.toString(16)}" else value.toString(16)
            } ?: ""
        }
    }
}

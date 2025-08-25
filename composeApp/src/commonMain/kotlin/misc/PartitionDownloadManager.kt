package misc

import PayloadAnalyzer
import data.PayloadHelper
import kotlinx.coroutines.flow.catch
import pbandk.ByteArr
import platform.saveFileWithProgress
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
            version: String? = null,
            onProgress: (DownloadProgress) -> Unit = {}
        ): Result<String> {
            return try {
                val downloadResult = downloadPartition(url, payloadInfo, partitionName) { downloadProgress ->
                    onProgress(downloadProgress)
                }

                if (downloadResult.isFailure) {
                    return Result.failure(downloadResult.exceptionOrNull() ?: Exception("下载失败"))
                }

                val operationData = downloadResult.getOrNull() ?: return Result.failure(Exception("下载失败"))

                val rebuildResult = PartitionRebuilder.rebuildPartitionImage(
                    operationData,
                    payloadInfo,
                    partitionName
                ) { rebuildProgress ->
                    val adjustedProgress = 0.7f + (rebuildProgress.progress * 0.2f)
                    onProgress(
                        DownloadProgress(
                            partitionName = partitionName,
                            bytesDownloaded = rebuildProgress.bytesProcessed,
                            totalBytes = rebuildProgress.totalBytes,
                            progress = adjustedProgress,
                            isCompleted = false,
                        )
                    )
                }

                if (rebuildResult.isFailure) {
                    return Result.failure(rebuildResult.exceptionOrNull() ?: Exception("重建失败"))
                }

                val partitionData = rebuildResult.getOrNull() ?: return Result.failure(Exception("重建失败"))

                val fileName = "${partitionName}.img"

                val saveResult = saveFileWithProgress(
                    data = partitionData,
                    fileName = fileName,
                    folder = version
                ) { saveProgress ->
                    val adjustedProgress = 0.9f + (saveProgress.progress * 0.1f)
                    onProgress(
                        DownloadProgress(
                            partitionName = partitionName,
                            bytesDownloaded = partitionData.size.toLong(),
                            totalBytes = partitionData.size.toLong(),
                            progress = adjustedProgress,
                            isCompleted = false,
                            error = if (saveProgress.error != null) "保存文件: ${saveProgress.error}" else null
                        )
                    )
                }

                saveResult.fold(
                    onSuccess = { filePath ->
                        onProgress(
                            DownloadProgress(
                                partitionName = partitionName,
                                bytesDownloaded = partitionData.size.toLong(),
                                totalBytes = partitionData.size.toLong(),
                                progress = 1f,
                                isCompleted = true,
                                error = "已保存到: $filePath (${formatFileSize(partitionData.size.toLong())})"
                            )
                        )
                        Result.success(filePath)
                    },
                    onFailure = { error ->
                        onProgress(
                            DownloadProgress(
                                partitionName = partitionName,
                                bytesDownloaded = 0,
                                totalBytes = 0,
                                progress = 0f,
                                isCompleted = false,
                                error = "保存失败: ${error.message}"
                            )
                        )
                        Result.failure(error)
                    }
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun downloadPartition(
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
                } ?: return Result.failure(Exception("分区 '$partitionName' 未找到"))

                val totalSize = partition.operations.sumOf { it.dataLength ?: 0L }

                if (totalSize <= 0L) {
                    onProgress(
                        DownloadProgress(
                            partitionName = partitionName,
                            bytesDownloaded = 0,
                            totalBytes = 0,
                            progress = 0.7f,
                            isCompleted = false,
                            error = "无需下载数据，正在重建分区..."
                        )
                    )
                    return Result.success(ByteArray(0))
                }

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
                            val downloadProgress = (totalBytesDownloaded.toFloat() / totalSize.toFloat()).coerceIn(0f, 1f)
                            downloadProgress * 0.7f
                        } else 0f

                        onProgress(
                            DownloadProgress(
                                partitionName = partitionName,
                                bytesDownloaded = totalBytesDownloaded,
                                totalBytes = totalSize,
                                progress = progress,
                                isCompleted = false,
                                error = "下载中... (${formatFileSize(totalBytesDownloaded)}/${formatFileSize(totalSize)})"
                            )
                        )
                    }

                val totalData = ByteArray(totalBytesDownloaded.toInt())
                var offset = 0
                partitionData.forEach { chunk ->
                    chunk.copyInto(totalData, offset)
                    offset += chunk.size
                }

                onProgress(
                    DownloadProgress(
                        partitionName = partitionName,
                        bytesDownloaded = totalBytesDownloaded,
                        totalBytes = totalSize,
                        progress = 0.7f,
                        isCompleted = false,
                        error = "下载完成，正在重建分区..."
                    )
                )

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
                    sha256 = formatHashToHex(partition.newPartitionInfo?.hash).take(16),
                    isDownloading = false,
                    progress = 0f
                )
            }
        }

        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB")
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

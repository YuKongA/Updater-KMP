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

        /**
         * 下载指定分区并保存到本地（恢复为原始 raw 文件）
         */
        suspend fun downloadPartitionToFile(
            url: String,
            payloadInfo: PayloadHelper.PayloadInfo,
            partitionName: String,
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
                    directory = null
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

        /**
         * 下载指定分区到内存
         */
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

                // 合并所有数据块
                val totalData = ByteArray(totalBytesDownloaded.toInt())
                var offset = 0
                partitionData.forEach { chunk ->
                    chunk.copyInto(totalData, offset)
                    offset += chunk.size
                }

                // 下载完成
                onProgress(
                    DownloadProgress(
                        partitionName = partitionName,
                        bytesDownloaded = totalBytesDownloaded,
                        totalBytes = totalSize,
                        progress = 0.7f, // 下载阶段完成，占总进度70%
                        isCompleted = false, // 整个流程还未完成
                        error = "下载完成，正在重建分区..."
                    )
                )

                Result.success(totalData)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        /**
         * 获取所有分区的信息
         */
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

        /**
         * 格式化文件大小
         */
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
            val size = bytes / 1024.0.pow(digitGroups.toDouble())
            return "${(size * 100).toLong() / 100.0} ${units[digitGroups]}"
        }

        /**
         * 将pbandk.ByteArr转换为十六进制字符串
         */
        fun formatHashToHex(hash: ByteArr?): String {
            return hash?.array?.joinToString("") { byte ->
                val value = byte.toInt() and 0xFF
                if (value < 16) "0${value.toString(16)}" else value.toString(16)
            } ?: ""
        }
    }
}

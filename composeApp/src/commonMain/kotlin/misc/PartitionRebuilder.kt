package misc

import chromeos_update_engine.InstallOperation
import data.PayloadHelper
import platform.Compression
import kotlin.math.min

/**
 * 高级分区重建器
 * 负责将下载的操作数据重建为完整的分区镜像
 */
object PartitionRebuilder {

    data class RebuildProgress(
        val partitionName: String,
        val currentOperation: Int,
        val totalOperations: Int,
        val bytesProcessed: Long,
        val totalBytes: Long,
        val progress: Float,
        val currentOperationType: String,
        val isCompleted: Boolean,
        val error: String? = null
    )

    /**
     * 重建完整的分区镜像
     */
    suspend fun rebuildPartitionImage(
        operationData: ByteArray,
        payloadInfo: PayloadHelper.PayloadInfo,
        partitionName: String,
        onProgress: (RebuildProgress) -> Unit = {}
    ): Result<ByteArray> {
        return try {
            val partition = payloadInfo.deltaArchiveManifest.partitions.firstOrNull {
                it.partitionName == partitionName
            } ?: return Result.failure(Exception("分区 '$partitionName' 未找到"))

            val targetSize = partition.newPartitionInfo?.size ?: 0L
            if (targetSize <= 0) {
                return Result.failure(Exception("分区大小无效: $targetSize"))
            }

            // 创建目标分区缓冲区
            val partitionBuffer = ByteArray(targetSize.toInt()) { 0x00 }
            val blockSize = payloadInfo.blockSize

            var operationDataOffset = 0
            val totalOperations = partition.operations.size
            var processedBytes = 0L

            onProgress(
                RebuildProgress(
                    partitionName = partitionName,
                    currentOperation = 0,
                    totalOperations = totalOperations,
                    bytesProcessed = 0,
                    totalBytes = targetSize,
                    progress = 0f,
                    currentOperationType = "初始化",
                    isCompleted = false
                )
            )

            // 处理每个操作
            for ((index, operation) in partition.operations.withIndex()) {
                val operationType = operation.type.name ?: "UNKNOWN"

                onProgress(
                    RebuildProgress(
                        partitionName = partitionName,
                        currentOperation = index + 1,
                        totalOperations = totalOperations,
                        bytesProcessed = processedBytes,
                        totalBytes = targetSize,
                        progress = ((index + 1).toFloat() / totalOperations.toFloat()).coerceIn(0f, 1f),
                        currentOperationType = operationType,
                        isCompleted = false
                    )
                )

                try {
                    val operationResult = processOperation(
                        operation,
                        operationData,
                        operationDataOffset,
                        partitionBuffer,
                        blockSize
                    )

                    operationDataOffset += operation.dataLength?.toInt() ?: 0
                    processedBytes += operationResult.bytesWritten

                } catch (e: Exception) {
                    onProgress(
                        RebuildProgress(
                            partitionName = partitionName,
                            currentOperation = index + 1,
                            totalOperations = totalOperations,
                            bytesProcessed = processedBytes,
                            totalBytes = targetSize,
                            progress = ((index + 1).toFloat() / totalOperations.toFloat()).coerceIn(0f, 1f),
                            currentOperationType = operationType,
                            isCompleted = false,
                            error = "处理操作失败: ${e.message}"
                        )
                    )
                    return Result.failure(e)
                }
            }

            // 完成重建
            onProgress(
                RebuildProgress(
                    partitionName = partitionName,
                    currentOperation = totalOperations,
                    totalOperations = totalOperations,
                    bytesProcessed = targetSize,
                    totalBytes = targetSize,
                    progress = 1f,
                    currentOperationType = "完成",
                    isCompleted = true
                )
            )

            Result.success(partitionBuffer)

        } catch (e: Exception) {
            onProgress(
                RebuildProgress(
                    partitionName = partitionName,
                    currentOperation = 0,
                    totalOperations = 0,
                    bytesProcessed = 0,
                    totalBytes = 0,
                    progress = 0f,
                    currentOperationType = "错误",
                    isCompleted = false,
                    error = "重建失败: ${e.message}"
                )
            )
            Result.failure(e)
        }
    }

    /**
     * 处理单个操作结果
     */
    data class OperationResult(
        val bytesWritten: Long,
        val success: Boolean,
        val error: String? = null
    )

    /**
     * 处理单个 InstallOperation
     */
    private suspend fun processOperation(
        operation: InstallOperation,
        sourceData: ByteArray,
        sourceOffset: Int,
        targetBuffer: ByteArray,
        blockSize: Int
    ): OperationResult {
        return try {
            // 提取操作数据
            val operationData = if (operation.dataLength != null && operation.dataLength > 0) {
                val dataLength = operation.dataLength.toInt()
                if (sourceOffset + dataLength <= sourceData.size) {
                    sourceData.sliceArray(sourceOffset until sourceOffset + dataLength)
                } else {
                    throw Exception("操作数据超出范围: offset=$sourceOffset, length=$dataLength, sourceSize=${sourceData.size}")
                }
            } else {
                byteArrayOf()
            }

            // 根据操作类型处理数据
            val processedData = processOperationByType(operation, operationData, blockSize)

            // 写入目标缓冲区
            val bytesWritten = writeToTargetBuffer(operation, processedData, targetBuffer, blockSize)

            OperationResult(
                bytesWritten = bytesWritten,
                success = true
            )

        } catch (e: Exception) {
            OperationResult(
                bytesWritten = 0,
                success = false,
                error = e.message
            )
        }
    }

    /**
     * 根据操作类型处理数据
     */
    private suspend fun processOperationByType(
        operation: InstallOperation,
        data: ByteArray,
        blockSize: Int
    ): ByteArray {
        println("Processing operation type: ${operation.type}, data length: ${data.size}")
        return when (operation.type) {
            InstallOperation.Type.REPLACE -> {
                // 直接替换，不需要解压
                data
            }

            InstallOperation.Type.REPLACE_BZ -> {
                // bzip2 解压
                val result = Compression.decompressBZ2(data)
                result.getOrElse {
                    throw Exception("bzip2 解压失败: ${it.message}")
                }
            }

            InstallOperation.Type.REPLACE_XZ -> {
                // XZ 解压
                val result = Compression.decompressXZ(data)
                result.getOrElse {
                    throw Exception("XZ 解压失败: ${it.message}")
                }
            }

            InstallOperation.Type.ZERO -> {
                // 生成零字节
                val outputSize = calculateOperationOutputSize(operation, blockSize)
                ByteArray(outputSize.toInt())
            }

            InstallOperation.Type.DISCARD -> {
                // 丢弃操作，不写入数据
                byteArrayOf()
            }

            else -> {
                // 其他未知类型，返回原始数据（例如 REPLACE 等）
                println("Unknown operation type: ${operation.type}, treating as raw data")
                data
            }
        }
    }

    /**
     * 将处理后的数据写入目标缓冲区
     */
    private fun writeToTargetBuffer(
        operation: InstallOperation,
        processedData: ByteArray,
        targetBuffer: ByteArray,
        blockSize: Int
    ): Long {
        if (processedData.isEmpty() || operation.dstExtents.isEmpty()) {
            return 0L
        }

        var totalBytesWritten = 0L
        var dataOffset = 0

        // 处理每个目标范围
        for (extent in operation.dstExtents) {
            val startBlock = extent.startBlock ?: 0L
            val numBlocks = extent.numBlocks ?: 0L

            if (numBlocks <= 0) continue

            val startOffset = (startBlock * blockSize).toInt()
            val bytesToWrite = (numBlocks * blockSize).toInt()

            if (startOffset + bytesToWrite > targetBuffer.size) {
                throw Exception("写入位置超出缓冲区范围: startOffset=$startOffset, bytesToWrite=$bytesToWrite, bufferSize=${targetBuffer.size}")
            }

            val actualBytesToWrite = min(bytesToWrite, processedData.size - dataOffset)
            if (actualBytesToWrite > 0) {
                processedData.copyInto(
                    destination = targetBuffer,
                    destinationOffset = startOffset,
                    startIndex = dataOffset,
                    endIndex = dataOffset + actualBytesToWrite
                )
                dataOffset += actualBytesToWrite
                totalBytesWritten += actualBytesToWrite
            }

            // 如果数据已用完，结束写入
            if (dataOffset >= processedData.size) break
        }

        return totalBytesWritten
    }

    /**
     * 计算操作的输出大小
     */
    private fun calculateOperationOutputSize(operation: InstallOperation, blockSize: Int): Long {
        return if (operation.dstExtents.isNotEmpty()) {
            operation.dstExtents.sumOf { extent ->
                (extent.numBlocks ?: 0L) * blockSize
            }
        } else {
            operation.dstLength ?: 0L
        }
    }
}

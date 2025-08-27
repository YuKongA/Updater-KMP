package misc

import chromeos_update_engine.InstallOperation
import data.PayloadHelper
import platform.Compression
import kotlin.math.min

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

    suspend fun rebuildPartitionImage(
        operationData: ByteArray,
        payloadInfo: PayloadHelper.PayloadInfo,
        partitionName: String,
        onProgress: (RebuildProgress) -> Unit = {}
    ): Result<ByteArray> {
        return try {
            val partition = payloadInfo.deltaArchiveManifest.partitions.firstOrNull {
                it.partitionName == partitionName
            } ?: return Result.failure(Exception("Error: Partition $partitionName not found"))

            val targetSize = partition.newPartitionInfo?.size ?: 0L
            if (targetSize <= 0) {
                return Result.failure(Exception("Error: Invalid target size for partition $partitionName"))
            }

            val partitionBuffer = ByteArray(targetSize.toInt())
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
                    currentOperationType = "Initializing",
                    isCompleted = false
                )
            )

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
                            error = "Error: ${e.message}"
                        )
                    )
                    return Result.failure(e)
                }
            }

            onProgress(
                RebuildProgress(
                    partitionName = partitionName,
                    currentOperation = totalOperations,
                    totalOperations = totalOperations,
                    bytesProcessed = targetSize,
                    totalBytes = targetSize,
                    progress = 1f,
                    currentOperationType = "Completed",
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
                    currentOperationType = "Error",
                    isCompleted = false,
                    error = "Error: ${e.message}"
                )
            )
            Result.failure(e)
        }
    }

    data class OperationResult(
        val bytesWritten: Long,
        val success: Boolean,
        val error: String? = null
    )

    private suspend fun processOperation(
        operation: InstallOperation,
        sourceData: ByteArray,
        sourceOffset: Int,
        targetBuffer: ByteArray,
        blockSize: Int
    ): OperationResult {
        return try {
            val operationData = if (operation.dataLength != null && operation.dataLength > 0) {
                val dataLength = operation.dataLength.toInt()
                if (sourceOffset + dataLength <= sourceData.size) {
                    sourceData.sliceArray(sourceOffset until sourceOffset + dataLength)
                } else {
                    throw Exception("Error: offset=$sourceOffset, length=$dataLength, sourceSize=${sourceData.size}")
                }
            } else {
                byteArrayOf()
            }

            val processedData = processOperationByType(operation, operationData, blockSize)
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

    private suspend fun processOperationByType(
        operation: InstallOperation,
        data: ByteArray,
        blockSize: Int
    ): ByteArray {
        return when (operation.type) {
            InstallOperation.Type.REPLACE -> {
                data
            }

            InstallOperation.Type.REPLACE_BZ -> {
                val result = Compression.decompressBZ2(data)
                result.getOrElse {
                    throw Exception("Bzip2 decompression failed: ${it.message}")
                }
            }

            InstallOperation.Type.REPLACE_XZ -> {
                val result = Compression.decompressXZ(data)
                result.getOrElse {
                    throw Exception("XZ decompression failed: ${it.message}")
                }
            }

            InstallOperation.Type.ZERO -> {
                val outputSize = calculateOperationOutputSize(operation, blockSize)
                ByteArray(outputSize.toInt())
            }

            InstallOperation.Type.DISCARD -> {
                byteArrayOf()
            }

            else -> {
                data
            }
        }
    }

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

        for (extent in operation.dstExtents) {
            val startBlock = extent.startBlock ?: 0L
            val numBlocks = extent.numBlocks ?: 0L

            if (numBlocks <= 0) continue

            val startOffset = (startBlock * blockSize).toInt()
            val bytesToWrite = (numBlocks * blockSize).toInt()

            if (startOffset + bytesToWrite > targetBuffer.size) {
                throw Exception("Error: startOffset=$startOffset, bytesToWrite=$bytesToWrite, bufferSize=${targetBuffer.size}")
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

            if (dataOffset >= processedData.size) break
        }

        return totalBytesWritten
    }

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

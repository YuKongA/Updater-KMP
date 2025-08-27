package platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import lzma.LZMA_OK
import lzma.LZMA_STREAM_END
import lzma.lzma_stream_buffer_decode
import platform.darwin.BZ2_bzBuffToBuffDecompress

actual object Compression {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun decompressXZ(compressedData: ByteArray): Result<ByteArray> {
        return try {
            if (compressedData.isEmpty()) {
                return Result.failure(Exception("XZ decompression failed: Input data is empty"))
            }
            if (compressedData.size < 6 ||
                compressedData[0] != 0xFD.toByte() ||
                compressedData[1] != 0x37.toByte() ||
                compressedData[2] != 0x7A.toByte() ||
                compressedData[3] != 0x58.toByte() ||
                compressedData[4] != 0x5A.toByte() ||
                compressedData[5] != 0x00.toByte()
            ) {
                return Result.failure(Exception("XZ decompression failed: Invalid file header"))
            }
            val compressedUBytes = compressedData.toUByteArray()
            var outBufSize = compressedData.size * 50
            val maxBufSize = 100 * 1024 * 1024
            var result: ByteArray? = null
            var lastRet = 0u
            var attemptCount = 0
            while (outBufSize <= maxBufSize && result == null) {
                attemptCount++
                memScoped {
                    val inPos = alloc<ULongVar>()
                    val outPos = alloc<ULongVar>()
                    val memLimit = alloc<ULongVar>()
                    inPos.value = 0uL
                    outPos.value = 0uL
                    memLimit.value = ULong.MAX_VALUE
                    val outUBuf = UByteArray(outBufSize)
                    val ret = lzma_stream_buffer_decode(
                        memLimit.ptr,
                        0u,
                        null,
                        compressedUBytes.refTo(0),
                        inPos.ptr,
                        compressedUBytes.size.toULong(),
                        outUBuf.refTo(0),
                        outPos.ptr,
                        outUBuf.size.toULong()
                    )
                    lastRet = ret
                    when (ret) {
                        LZMA_OK, LZMA_STREAM_END -> {
                            val actualSize = outPos.value.toInt()
                            if (actualSize > 0) {
                                result = outUBuf.copyOf(actualSize).toByteArray()
                            }
                        }
                        5u -> {
                            outBufSize *= 2
                        }
                        6u, 8u -> {
                            break
                        }
                        else -> {
                            break
                        }
                    }
                }
            }
            if (result != null) {
                Result.success(result)
            } else {
                val errorMsg = when (lastRet) {
                    6u -> "Data corruption detected"
                    8u -> "Invalid XZ format"
                    else -> "Unknown error (code: $lastRet)"
                }
                Result.failure(Exception("XZ decompression failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("XZ decompression failed: ${e.message}", e))
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun decompressBZ2(compressedData: ByteArray): Result<ByteArray> {
        return try {
            if (compressedData.isEmpty()) {
                return Result.failure(Exception("BZ2 decompression failed: Input data is empty"))
            }
            if (compressedData.size < 4 ||
                compressedData[0] != 0x42.toByte() ||
                compressedData[1] != 0x5A.toByte() ||
                compressedData[2] != 0x68.toByte()
            ) {
                return Result.failure(Exception("BZ2 decompression failed: Invalid file header"))
            }
            val expectedOutputSize = 2097152
            var result: ByteArray? = null
            memScoped {
                val outBuf = ByteArray(expectedOutputSize)
                val destLen = alloc<UIntVar>()
                destLen.value = expectedOutputSize.toUInt()
                val ret = BZ2_bzBuffToBuffDecompress(
                    outBuf.refTo(0), destLen.ptr,
                    compressedData.refTo(0), compressedData.size.toUInt(),
                    0, 0
                )
                when (ret) {
                    0 -> {
                        val actualSize = destLen.value.toInt()
                        if (actualSize > 0) {
                            result = outBuf.copyOf(actualSize)
                        }
                    }
                    -5 -> {
                        val largerBuf = ByteArray(expectedOutputSize * 2)
                        destLen.value = (expectedOutputSize * 2).toUInt()
                        val retryRet = BZ2_bzBuffToBuffDecompress(
                            largerBuf.refTo(0), destLen.ptr,
                            compressedData.refTo(0), compressedData.size.toUInt(),
                            0, 0
                        )
                        if (retryRet == 0) {
                            val actualSize = destLen.value.toInt()
                            result = largerBuf.copyOf(actualSize)
                        }
                    }
                    else -> {
                    }
                }
            }
            if (result != null) {
                Result.success(result)
            } else {
                val errorMsg = "Decompression failed with native BZ2 library"
                Result.failure(Exception("BZ2 decompression failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("BZ2 decompression failed: ${e.message}", e))
        }
    }
}

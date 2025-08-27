package platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import platform.darwin.BZ2_bzBuffToBuffDecompress

actual object Compression {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun decompressXZ(compressedData: ByteArray): Result<ByteArray> {
        TODO("Not yet implemented")
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

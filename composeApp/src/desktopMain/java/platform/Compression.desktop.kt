package platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.tukaani.xz.XZInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

actual object Compression {

    actual suspend fun decompressXZ(compressedData: ByteArray): Result<ByteArray> {
        return withContext(Dispatchers.IO) {
            try {
                ByteArrayInputStream(compressedData).use { inputStream ->
                    XZInputStream(inputStream).use { xzStream ->
                        val output = ByteArrayOutputStream()
                        xzStream.copyTo(output, 8192)
                        Result.success(output.toByteArray())
                    }
                }
            } catch (e: Exception) {
                Result.failure(Exception("XZ解压失败: ${e.message}", e))
            }
        }
    }

    actual suspend fun decompressBZ2(compressedData: ByteArray): Result<ByteArray> {
        return withContext(Dispatchers.IO) {
            try {
                ByteArrayInputStream(compressedData).use { inputStream ->
                    BZip2CompressorInputStream(inputStream).use { bzStream ->
                        val output = ByteArrayOutputStream()
                        bzStream.copyTo(output, 8192)
                        Result.success(output.toByteArray())
                    }
                }
            } catch (e: Exception) {
                Result.failure(Exception("bzip2解压失败: ${e.message}", e))
            }
        }
    }
}

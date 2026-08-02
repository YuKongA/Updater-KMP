package data.repository

import io.ktor.client.HttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import platform.httpClientPlatform
import utils.HttpRangeReader
import utils.PayloadBinUtils
import utils.PayloadBinUtils.PayloadOperation
import utils.ZipFileUtils
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

/**
 * Reads the kernel release out of an AB package's boot image over HTTP range
 * requests, decompressing only the payload operation holding the banner.
 */
class KernelVersionFetcherImpl(
    client: HttpClient = httpClientPlatform(),
) : KernelVersionFetcher {
    private companion object {
        const val TIMEOUT_MS = 30000L
        const val END_BYTES_SIZE = 4096
        const val LOCAL_HEADER_SIZE = 256

        const val BOOT_PARTITION = "boot"
        const val BOOT_MAGIC = "ANDROID!"
        const val BOOT_HEADER_SIZE = 64
        const val KERNEL_SIZE_PROBE = 128 * 1024

        // "Linux version" sits ~47.6% into every GKI kernel measured; only
        // picks which operation to fetch first, a miss widens outwards.
        const val BANNER_POSITION = 0.476

        // A miss is cheap to recover from, but not unboundedly so.
        const val MAX_PROBES = 12
        const val MAX_OP_DATA = 64L shl 20
    }

    private val reader = HttpRangeReader(client)
    private val bannerRegex = Regex("""^Linux version ([\w.+-]+) \(""")

    override suspend fun getKernelVersion(url: String): String? {
        return try {
            withTimeout(TIMEOUT_MS.milliseconds) {
                try {
                    extract(url)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
            }
        } catch (_: TimeoutCancellationException) {
            null
        }
    }

    private suspend fun extract(url: String): String? {
        val fileLength = reader.fileLength(url) ?: return null
        if (fileLength <= 0L) return null

        val tailSize = min(fileLength, END_BYTES_SIZE.toLong()).toInt()
        val tail = reader.read(url, fileLength - tailSize, tailSize) ?: return null

        val cd = ZipFileUtils.locateCentralDirectory(tail, fileLength)
        if (cd.offset < 0 || cd.size <= 0 || cd.offset + cd.size > fileLength) return null
        val cdBytes = reader.read(url, cd.offset, cd.size.toInt()) ?: return null

        val entry = ZipFileUtils.locateEntries(cdBytes, setOf(PayloadBinUtils.PAYLOAD_ENTRY))[PayloadBinUtils.PAYLOAD_ENTRY]
            ?: return null
        if (entry.method != 0) return null // range math only works on a stored entry

        val headerOffset = entry.localHeaderOffset
        if (headerOffset !in 0..<fileLength) return null
        val maxLocalHeaderRead = min(fileLength - headerOffset, LOCAL_HEADER_SIZE.toLong()).toInt()
        if (maxLocalHeaderRead < 30) return null
        val localHeaderBytes = reader.read(url, headerOffset, maxLocalHeaderRead) ?: return null
        val internalOffset = ZipFileUtils.locateLocalFileOffset(localHeaderBytes)
        if (internalOffset !in 0..maxLocalHeaderRead.toLong()) return null

        val payloadStart = headerOffset + internalOffset
        val header = reader.read(url, payloadStart, PayloadBinUtils.HEADER_SIZE) ?: return null
        val payloadHeader = PayloadBinUtils.parseHeader(header) ?: return null

        val manifest = reader.read(url, payloadStart + PayloadBinUtils.HEADER_SIZE, payloadHeader.manifestSize.toInt())
            ?: return null
        val operations = PayloadBinUtils.partitionOperations(manifest, BOOT_PARTITION)
        if (operations.isEmpty()) return null

        val dataBase = payloadStart + PayloadBinUtils.HEADER_SIZE + payloadHeader.manifestSize + payloadHeader.signatureSize

        val kernelSize = readKernelSize(url, dataBase, operations.first()) ?: return null

        // The kernel follows the page-aligned boot header.
        val target = 4096L + (kernelSize * BANNER_POSITION).toLong()
        return probeForBanner(url, dataBase, operations, target)
    }

    /** Kernel image size from the boot header at the first operation's head. */
    private suspend fun readKernelSize(url: String, dataBase: Long, first: PayloadOperation): Long? {
        val probeSize = min(first.dataLength, KERNEL_SIZE_PROBE.toLong())
        if (probeSize <= 0) return null
        val compressed = reader.read(url, dataBase + first.dataOffset, probeSize.toInt()) ?: return null

        val header = PayloadBinUtils.decompressOperation(first, compressed, BOOT_HEADER_SIZE, allowTruncated = true)
            ?: return null
        if (header.size < BOOT_HEADER_SIZE) return null
        for (i in BOOT_MAGIC.indices) {
            if (header[i] != BOOT_MAGIC[i].code.toByte()) return null
        }
        val kernelSize = (header[8].toLong() and 0xFF) or
                ((header[9].toLong() and 0xFF) shl 8) or
                ((header[10].toLong() and 0xFF) shl 16) or
                ((header[11].toLong() and 0xFF) shl 24)
        return kernelSize.takeIf { it > 0 }
    }

    /** Decompresses the operation covering [target], then its neighbours, until the banner turns up. */
    private suspend fun probeForBanner(
        url: String,
        dataBase: Long,
        operations: List<PayloadOperation>,
        target: Long,
    ): String? {
        val start = operations.indexOfFirst { target >= it.destOffset && target < it.destOffset + it.destLength }
            .let { if (it < 0) 0 else it }
        val seen = HashMap<Int, ByteArray>(MAX_PROBES)

        for (probe in 0 until MAX_PROBES) {
            // Alternate outwards: the answer is usually next door to the estimate.
            var index = start + (probe + 1) / 2
            if (probe % 2 == 1) index = start - (probe + 1) / 2
            if (index < 0 || index >= operations.size) continue
            if (index in seen) continue

            val image = applyOperation(url, dataBase, operations[index]) ?: return null
            seen[index] = image

            // Search adjacent operations together so a banner straddling a boundary is found.
            findBanner(contiguous(seen, index))?.let { return it }
        }
        return null
    }

    private suspend fun applyOperation(url: String, dataBase: Long, op: PayloadOperation): ByteArray? {
        if (op.destLength > Int.MAX_VALUE) return null
        if (op.type == PayloadBinUtils.OP_ZERO) return ByteArray(op.destLength.toInt())
        if (op.dataLength <= 0 || op.dataLength > MAX_OP_DATA) return null
        val compressed = reader.read(url, dataBase + op.dataOffset, op.dataLength.toInt()) ?: return null
        return PayloadBinUtils.decompressOperation(op, compressed, op.destLength.toInt())
    }

    /** Joins the decompressed operations adjacent to [index]. */
    private fun contiguous(seen: Map<Int, ByteArray>, index: Int): ByteArray {
        var first = index
        while ((first - 1) in seen) first--
        var last = index
        while ((last + 1) in seen) last++

        if (first == last) return seen.getValue(index)
        var total = 0
        for (i in first..last) total += seen.getValue(i).size
        val joined = ByteArray(total)
        var pos = 0
        for (i in first..last) {
            val chunk = seen.getValue(i)
            chunk.copyInto(joined, pos)
            pos += chunk.size
        }
        return joined
    }

    private fun findBanner(bytes: ByteArray): String? {
        val marker = "Linux version ".encodeToByteArray()
        var i = 0
        outer@ while (i <= bytes.size - marker.size) {
            for (j in marker.indices) {
                if (bytes[i + j] != marker[j]) {
                    i++
                    continue@outer
                }
            }
            val window = bytes.decodeToString(i, min(bytes.size, i + 200), throwOnInvalidSequence = false)
            bannerRegex.find(window)?.let { return it.groupValues[1] }
            i++
        }
        return null
    }
}

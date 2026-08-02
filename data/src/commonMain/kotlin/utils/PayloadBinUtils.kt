package utils

import utils.xz.XzDecoder

/**
 * Minimal payload.bin reader: one partition's install operations plus their
 * decompression. The manifest is scanned as raw protobuf with unknown fields
 * skipped — safe against schema drift, no generated class needed.
 */
object PayloadBinUtils {
    const val PAYLOAD_ENTRY = "payload.bin"
    const val HEADER_SIZE = 24 // magic, version, manifest size, signature size

    // Full payloads only use the replace family; diff types have nothing to read from.
    const val OP_REPLACE = 0L
    const val OP_REPLACE_BZ = 1L
    const val OP_ZERO = 6L
    const val OP_REPLACE_XZ = 8L

    private const val PAYLOAD_MAGIC = "CrAU"
    private const val MAX_MANIFEST_SIZE = 32L shl 20

    data class PayloadHeader(val manifestSize: Long, val signatureSize: Long)

    /** One contiguous span of a partition image. */
    data class PayloadOperation(
        val type: Long,
        val dataOffset: Long, // relative to the start of the payload's data blobs
        val dataLength: Long,
        val destOffset: Long, // byte offset within the partition image
        val destLength: Long,
    )

    fun parseHeader(header: ByteArray): PayloadHeader? {
        if (header.size < HEADER_SIZE) return null
        for (i in 0 until 4) {
            if (header[i] != PAYLOAD_MAGIC[i].code.toByte()) return null
        }
        val manifestSize = header.u64be(12)
        val signatureSize = header.u32be(20)
        if (manifestSize <= 0 || manifestSize > MAX_MANIFEST_SIZE) return null
        return PayloadHeader(manifestSize, signatureSize)
    }

    /** The named partition's operations in image-write order; empty if absent. */
    fun partitionOperations(manifest: ByteArray, partition: String): List<PayloadOperation> {
        // block_size must be settled before the extents expressed in it are read.
        var blockSize = 4096L
        scanProto(manifest, 0, manifest.size) { field, wire, num, _, _ ->
            if (field == 3 && wire == 0) {
                blockSize = num
                false
            } else true
        }
        if (blockSize <= 0) return emptyList()

        var operations = emptyList<PayloadOperation>()
        scanProto(manifest, 0, manifest.size) { field, wire, _, dataStart, dataEnd ->
            if (field != 13 || wire != 2) return@scanProto true // partitions
            var name = ""
            val ops = ArrayList<PayloadOperation>()
            scanProto(manifest, dataStart, dataEnd) { pf, pw, _, pdStart, pdEnd ->
                when {
                    pf == 1 && pw == 2 -> name = manifest.decodeToString(pdStart, pdEnd)
                    pf == 8 && pw == 2 -> parseOperation(manifest, pdStart, pdEnd, blockSize)?.let { ops.add(it) }
                }
                true
            }
            if (name == partition) {
                operations = ops
                false
            } else true
        }
        return operations
    }

    /**
     * Decompresses at most [maxOutput] bytes of an operation's output; null
     * for types a full payload never carries (or bzip2, which nothing modern emits).
     */
    fun decompressOperation(
        op: PayloadOperation,
        compressed: ByteArray,
        maxOutput: Int,
        allowTruncated: Boolean = false,
    ): ByteArray? {
        return try {
            when (op.type) {
                OP_REPLACE -> if (compressed.size > maxOutput) compressed.copyOf(maxOutput) else compressed
                OP_REPLACE_XZ -> XzDecoder.decode(compressed, maxOutput, allowTruncated)
                OP_ZERO -> ByteArray(minOf(op.destLength, maxOutput.toLong()).toInt())
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseOperation(buf: ByteArray, start: Int, end: Int, blockSize: Long): PayloadOperation? {
        var type = 0L
        var dataOffset = 0L
        var dataLength = 0L
        var destOffset = 0L
        var destLength = 0L
        scanProto(buf, start, end) { field, wire, num, dStart, dEnd ->
            when {
                field == 1 && wire == 0 -> type = num
                field == 2 && wire == 0 -> dataOffset = num
                field == 3 && wire == 0 -> dataLength = num
                field == 6 && wire == 2 -> { // dst_extents
                    var startBlock = 0L
                    var numBlocks = 0L
                    scanProto(buf, dStart, dEnd) { ef, ew, en, _, _ ->
                        when {
                            ef == 1 && ew == 0 -> startBlock = en
                            ef == 2 && ew == 0 -> numBlocks = en
                        }
                        true
                    }
                    if (destLength == 0L) destOffset = startBlock * blockSize
                    destLength += numBlocks * blockSize
                }
            }
            true
        }
        return if (destLength > 0) PayloadOperation(type, dataOffset, dataLength, destOffset, destLength) else null
    }

    /**
     * Walks protobuf wire format; returning false from [visit] stops the
     * walk, malformed input just ends it.
     */
    private fun scanProto(
        buf: ByteArray,
        start: Int,
        end: Int,
        visit: (field: Int, wire: Int, num: Long, dataStart: Int, dataEnd: Int) -> Boolean,
    ) {
        var i = start
        while (i < end) {
            val key = readVarint(buf, i, end) ?: return
            i = key.second
            val field = (key.first ushr 3).toInt()
            val wire = (key.first and 7L).toInt()
            var num = 0L
            var dataStart = 0
            var dataEnd = 0

            when (wire) {
                0 -> {
                    val v = readVarint(buf, i, end) ?: return
                    num = v.first
                    i = v.second
                }
                1 -> {
                    if (i + 8 > end) return
                    num = buf.u64le(i)
                    i += 8
                }
                2 -> {
                    val len = readVarint(buf, i, end) ?: return
                    i = len.second
                    if (len.first < 0 || i + len.first > end) return
                    dataStart = i
                    dataEnd = i + len.first.toInt()
                    i = dataEnd
                }
                5 -> {
                    if (i + 4 > end) return
                    num = buf.u32le(i)
                    i += 4
                }
                else -> return
            }

            if (!visit(field, wire, num, dataStart, dataEnd)) return
        }
    }

    private fun readVarint(buf: ByteArray, start: Int, end: Int): Pair<Long, Int>? {
        var value = 0L
        var shift = 0
        var i = start
        while (i < end) {
            val b = buf[i++].toInt() and 0xFF
            value = value or ((b and 0x7F).toLong() shl shift)
            if (b and 0x80 == 0) return value to i
            shift += 7
            if (shift > 63) return null
        }
        return null
    }

    private fun ByteArray.u32le(pos: Int): Long =
        (this[pos].toLong() and 0xFF) or
                ((this[pos + 1].toLong() and 0xFF) shl 8) or
                ((this[pos + 2].toLong() and 0xFF) shl 16) or
                ((this[pos + 3].toLong() and 0xFF) shl 24)

    private fun ByteArray.u64le(pos: Int): Long =
        u32le(pos) or (u32le(pos + 4) shl 32)

    private fun ByteArray.u32be(pos: Int): Long =
        (this[pos + 3].toLong() and 0xFF) or
                ((this[pos + 2].toLong() and 0xFF) shl 8) or
                ((this[pos + 1].toLong() and 0xFF) shl 16) or
                ((this[pos].toLong() and 0xFF) shl 24)

    private fun ByteArray.u64be(pos: Int): Long =
        u32be(pos + 4) or (u32be(pos) shl 32)
}

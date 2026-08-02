import utils.PayloadBinUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The manifest bytes are hand-assembled protobuf wire format mirroring
 * DeltaArchiveManifest: block_size = 3, partitions = 13; PartitionUpdate:
 * partition_name = 1, operations = 8; InstallOperation: type = 1,
 * data_offset = 2, data_length = 3, dst_extents = 6; Extent: start_block = 1,
 * num_blocks = 2.
 */
class PayloadBinUtilsTest {

    private class ProtoWriter {
        val bytes = ArrayList<Byte>()

        fun varint(value: Long) {
            var v = value
            while (true) {
                val b = (v and 0x7F).toInt()
                v = v ushr 7
                if (v == 0L) {
                    bytes.add(b.toByte())
                    return
                }
                bytes.add((b or 0x80).toByte())
            }
        }

        fun field(number: Int, value: Long) {
            varint((number.toLong() shl 3) or 0L)
            varint(value)
        }

        fun message(number: Int, inner: ProtoWriter.() -> Unit) {
            val w = ProtoWriter().apply(inner)
            varint((number.toLong() shl 3) or 2L)
            varint(w.bytes.size.toLong())
            bytes.addAll(w.bytes)
        }

        fun string(number: Int, value: String) {
            val encoded = value.encodeToByteArray()
            varint((number.toLong() shl 3) or 2L)
            varint(encoded.size.toLong())
            encoded.forEach { bytes.add(it) }
        }

        fun toByteArray() = bytes.toByteArray()
    }

    private fun manifest(blockSize: Long = 4096): ByteArray = ProtoWriter().apply {
        field(3, blockSize)
        message(13) {
            string(1, "system")
            message(8) {
                field(1, 8) // REPLACE_XZ
                field(2, 0)
                field(3, 111)
                message(6) {
                    field(1, 0)
                    field(2, 10)
                }
            }
        }
        message(13) {
            string(1, "boot")
            message(8) {
                field(1, 8) // REPLACE_XZ
                field(2, 1000)
                field(3, 500)
                message(6) {
                    field(1, 0)
                    field(2, 512) // 2 MiB at 4K blocks
                }
            }
            message(8) {
                field(1, 0) // REPLACE
                field(2, 1500)
                field(3, 300)
                message(6) {
                    field(1, 512)
                    field(2, 512)
                }
            }
            // Unknown fields must be skipped, not trip the scanner.
            field(99, 12345)
        }
    }.toByteArray()

    @Test
    fun findsBootOperations() {
        val ops = PayloadBinUtils.partitionOperations(manifest(), "boot")
        assertEquals(2, ops.size)

        assertEquals(PayloadBinUtils.OP_REPLACE_XZ, ops[0].type)
        assertEquals(1000L, ops[0].dataOffset)
        assertEquals(500L, ops[0].dataLength)
        assertEquals(0L, ops[0].destOffset)
        assertEquals(512L * 4096, ops[0].destLength)

        assertEquals(PayloadBinUtils.OP_REPLACE, ops[1].type)
        assertEquals(512L * 4096, ops[1].destOffset)
        assertEquals(512L * 4096, ops[1].destLength)
    }

    @Test
    fun missingPartitionYieldsNothing() {
        assertTrue(PayloadBinUtils.partitionOperations(manifest(), "vendor_boot").isEmpty())
    }

    @Test
    fun blockSizeScalesExtents() {
        val ops = PayloadBinUtils.partitionOperations(manifest(blockSize = 8192), "boot")
        assertEquals(512L * 8192, ops[0].destLength)
    }

    @Test
    fun parsesPayloadHeader() {
        val header = ByteArray(24)
        "CrAU".forEachIndexed { i, c -> header[i] = c.code.toByte() }
        header[11] = 2 // version 2, big-endian u64 at offset 4
        // manifest size 0x0102 at offset 12, big-endian u64
        header[18] = 1
        header[19] = 2
        // signature size 0x30 at offset 20, big-endian u32
        header[23] = 0x30

        val parsed = assertNotNull(PayloadBinUtils.parseHeader(header))
        assertEquals(0x0102L, parsed.manifestSize)
        assertEquals(0x30L, parsed.signatureSize)
    }

    @Test
    fun rejectsForeignMagic() {
        val header = ByteArray(24)
        "NoPe".forEachIndexed { i, c -> header[i] = c.code.toByte() }
        assertNull(PayloadBinUtils.parseHeader(header))
    }
}

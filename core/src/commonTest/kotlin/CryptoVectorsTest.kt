import platform.crypto.AesCbc
import platform.crypto.md5
import platform.crypto.sha1
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CryptoVectorsTest {

    private fun hex(bytes: ByteArray): String =
        bytes.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }

    private fun bytes(hex: String): ByteArray =
        ByteArray(hex.length / 2) { ((hex[it * 2].digitToInt(16) shl 4) or hex[it * 2 + 1].digitToInt(16)).toByte() }

    // --- MD5 (RFC 1321 test suite) ---------------------------------------

    @Test
    fun md5KnownVectors() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", hex(md5("".encodeToByteArray())))
        assertEquals("900150983cd24fb0d6963f7d28e17f72", hex(md5("abc".encodeToByteArray())))
        assertEquals("f96b697d7cb7938d525a2f31aaf161d0", hex(md5("message digest".encodeToByteArray())))
        assertEquals(
            "9e107d9d372bb6826bd81d3542a419d6",
            hex(md5("The quick brown fox jumps over the lazy dog".encodeToByteArray())),
        )
        // Spans multiple 64-byte blocks (80 chars) to exercise chunk boundaries.
        assertEquals(
            "57edf4a22be3c955ac49da2e2107b67a",
            hex(md5("12345678901234567890123456789012345678901234567890123456789012345678901234567890".encodeToByteArray())),
        )
    }

    // --- SHA-1 (RFC 3174 / FIPS 180) -------------------------------------

    @Test
    fun sha1KnownVectors() {
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", hex(sha1("".encodeToByteArray())))
        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", hex(sha1("abc".encodeToByteArray())))
        assertEquals(
            "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12",
            hex(sha1("The quick brown fox jumps over the lazy dog".encodeToByteArray())),
        )
        assertEquals(
            "84983e441c3bd26ebaae4aa1f95129e5e54670f1",
            hex(sha1("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".encodeToByteArray())),
        )
    }

    // AES core via FIPS-197 vectors: with a zero IV the first CBC ciphertext block
    // equals the raw ECB block, so this checks the cipher + key schedule (128/192/256).

    private val zeroIv = ByteArray(16)

    private fun firstBlockEcb(key: String, plaintext: String): String {
        val out = AesCbc.encrypt(bytes(key), zeroIv, bytes(plaintext))
        return hex(out.copyOf(16))
    }

    @Test
    fun aesBlockFips197() {
        assertEquals(
            "69c4e0d86a7b0430d8cdb78070b4c55a",
            firstBlockEcb("000102030405060708090a0b0c0d0e0f", "00112233445566778899aabbccddeeff"),
        )
        assertEquals(
            "dda97ca4864cdfe06eaf70a0ec0d7191",
            firstBlockEcb("000102030405060708090a0b0c0d0e0f1011121314151617", "00112233445566778899aabbccddeeff"),
        )
        assertEquals(
            "8ea2b7ca516745bfeafc49904b496089",
            firstBlockEcb(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f",
                "00112233445566778899aabbccddeeff",
            ),
        )
    }

    // --- CBC + PKCS#7 round trips (also exercises multi-block decrypt) -----

    @Test
    fun cbcRoundTripAllKeySizesAndLengths() {
        val iv = "0102030405060708".encodeToByteArray() // matches the 16-byte miui IV
        for (keyLen in intArrayOf(16, 32)) {
            val key = ByteArray(keyLen) { (it * 7 + 1).toByte() }
            for (len in intArrayOf(0, 1, 15, 16, 17, 31, 32, 100)) {
                val plain = ByteArray(len) { (it * 3).toByte() }
                val cipher = AesCbc.encrypt(key, iv, plain)
                assertTrue(cipher.size % 16 == 0 && cipher.size > len, "ciphertext must be padded to a full block")
                assertEquals(hex(plain), hex(AesCbc.decrypt(key, iv, cipher)))
            }
        }
    }

    @Test
    fun cbcMatchesMiuiUsageShape() {
        // The default 16-byte key path used before login.
        val key = "miuiotavalided11".encodeToByteArray()
        val iv = "0102030405060708".encodeToByteArray()
        val plain = """{"b":"F","c":"1"}"""
        val cipher = AesCbc.encrypt(key, iv, plain.encodeToByteArray())
        assertEquals(plain, AesCbc.decrypt(key, iv, cipher).decodeToString())
    }
}

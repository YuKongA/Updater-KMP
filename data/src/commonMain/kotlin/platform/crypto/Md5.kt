package platform.crypto

// Per-round left-rotate amounts (RFC 1321).
private val MD5_SHIFTS = intArrayOf(
    7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
    5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
    4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
    6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21,
)

// K[i] = floor(2^32 * abs(sin(i+1))), from RFC 1321.
private val MD5_K = longArrayOf(
    0xd76aa478, 0xe8c7b756, 0x242070db, 0xc1bdceee,
    0xf57c0faf, 0x4787c62a, 0xa8304613, 0xfd469501,
    0x698098d8, 0x8b44f7af, 0xffff5bb1, 0x895cd7be,
    0x6b901122, 0xfd987193, 0xa679438e, 0x49b40821,
    0xf61e2562, 0xc040b340, 0x265e5a51, 0xe9b6c7aa,
    0xd62f105d, 0x02441453, 0xd8a1e681, 0xe7d3fbc8,
    0x21e1cde6, 0xc33707d6, 0xf4d50d87, 0x455a14ed,
    0xa9e3e905, 0xfcefa3f8, 0x676f02d9, 0x8d2a4c8a,
    0xfffa3942, 0x8771f681, 0x6d9d6122, 0xfde5380c,
    0xa4beea44, 0x4bdecfa9, 0xf6bb4b60, 0xbebfbc70,
    0x289b7ec6, 0xeaa127fa, 0xd4ef3085, 0x04881d05,
    0xd9d4d039, 0xe6db99e5, 0x1fa27cf8, 0xc4ac5665,
    0xf4292244, 0x432aff97, 0xab9423a7, 0xfc93a039,
    0x655b59c3, 0x8f0ccc92, 0xffeff47d, 0x85845dd1,
    0x6fa87e4f, 0xfe2ce6e0, 0xa3014314, 0x4e0811a1,
    0xf7537e82, 0xbd3af235, 0x2ad7d2bb, 0xeb86d391,
).map { it.toInt() }.toIntArray()

/** Dependency-free MD5 (RFC 1321); only used for the Xiaomi login `hash`. */
internal fun md5(message: ByteArray): ByteArray {
    var a0 = 0x67452301
    var b0 = -0x10325477 // 0xefcdab89
    var c0 = -0x67452302 // 0x98badcfe
    var d0 = 0x10325476

    val msgLen = message.size
    val bitLen = msgLen.toLong() * 8
    val padLen = ((56 - (msgLen + 1) % 64) + 64) % 64
    val padded = ByteArray(msgLen + 1 + padLen + 8)
    message.copyInto(padded)
    padded[msgLen] = 0x80.toByte()
    for (i in 0 until 8) padded[padded.size - 8 + i] = (bitLen ushr (8 * i)).toByte()

    val m = IntArray(16)
    var chunk = 0
    while (chunk < padded.size) {
        for (i in 0 until 16) {
            val j = chunk + i * 4
            m[i] = (padded[j].toInt() and 0xFF) or
                    (padded[j + 1].toInt() and 0xFF shl 8) or
                    (padded[j + 2].toInt() and 0xFF shl 16) or
                    (padded[j + 3].toInt() and 0xFF shl 24)
        }
        var a = a0
        var b = b0
        var c = c0
        var d = d0
        for (i in 0 until 64) {
            val f: Int
            val g: Int
            when (i / 16) {
                0 -> {
                    f = (b and c) or (b.inv() and d); g = i
                }

                1 -> {
                    f = (d and b) or (d.inv() and c); g = (5 * i + 1) % 16
                }

                2 -> {
                    f = b xor c xor d; g = (3 * i + 5) % 16
                }

                else -> {
                    f = c xor (b or d.inv()); g = (7 * i) % 16
                }
            }
            val rotated = (a + f + MD5_K[i] + m[g]).rotateLeft(MD5_SHIFTS[i])
            a = d
            d = c
            c = b
            b += rotated
        }
        a0 += a
        b0 += b
        c0 += c
        d0 += d
        chunk += 64
    }

    val out = ByteArray(16)
    intArrayOf(a0, b0, c0, d0).forEachIndexed { idx, v ->
        for (i in 0 until 4) out[idx * 4 + i] = (v ushr (8 * i)).toByte()
    }
    return out
}

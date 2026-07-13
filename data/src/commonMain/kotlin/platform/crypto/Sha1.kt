package platform.crypto

/** Dependency-free SHA-1 (RFC 3174); only used for the Xiaomi `clientSign`. */
internal fun sha1(message: ByteArray): ByteArray {
    var h0 = 0x67452301
    var h1 = -0x10325477 // 0xEFCDAB89
    var h2 = -0x67452302 // 0x98BADCFE
    var h3 = 0x10325476
    var h4 = -0x3c2d1e10 // 0xC3D2E1F0

    val msgLen = message.size
    val bitLen = msgLen.toLong() * 8
    val padLen = ((56 - (msgLen + 1) % 64) + 64) % 64
    val padded = ByteArray(msgLen + 1 + padLen + 8)
    message.copyInto(padded)
    padded[msgLen] = 0x80.toByte()
    for (i in 0 until 8) padded[padded.size - 1 - i] = (bitLen ushr (8 * i)).toByte()

    val w = IntArray(80)
    var chunk = 0
    while (chunk < padded.size) {
        for (i in 0 until 16) {
            val j = chunk + i * 4
            w[i] = (padded[j].toInt() and 0xFF shl 24) or
                    (padded[j + 1].toInt() and 0xFF shl 16) or
                    (padded[j + 2].toInt() and 0xFF shl 8) or
                    (padded[j + 3].toInt() and 0xFF)
        }
        for (i in 16 until 80) {
            w[i] = (w[i - 3] xor w[i - 8] xor w[i - 14] xor w[i - 16]).rotateLeft(1)
        }
        var a = h0
        var b = h1
        var c = h2
        var d = h3
        var e = h4
        for (i in 0 until 80) {
            val f: Int
            val k: Int
            when (i / 20) {
                0 -> {
                    f = (b and c) or (b.inv() and d); k = 0x5A827999
                }

                1 -> {
                    f = b xor c xor d; k = 0x6ED9EBA1
                }

                2 -> {
                    f = (b and c) or (b and d) or (c and d); k = -0x70e44324
                } // 0x8F1BBCDC
                else -> {
                    f = b xor c xor d; k = -0x359d3e2a
                } // 0xCA62C1D6
            }
            val tmp = a.rotateLeft(5) + f + e + k + w[i]
            e = d
            d = c
            c = b.rotateLeft(30)
            b = a
            a = tmp
        }
        h0 += a
        h1 += b
        h2 += c
        h3 += d
        h4 += e
        chunk += 64
    }

    val out = ByteArray(20)
    intArrayOf(h0, h1, h2, h3, h4).forEachIndexed { idx, v ->
        for (i in 0 until 4) out[idx * 4 + i] = (v ushr (24 - 8 * i)).toByte()
    }
    return out
}

package platform.crypto

/**
 * Dependency-free AES-CBC/PKCS#7 (128/192/256-bit keys), so native binaries drop the
 * static OpenSSL link. IV is explicit and never prefixed to the ciphertext, matching
 * the previous provider's encryptWithIv/decryptWithIv.
 */
internal object AesCbc {
    private const val BLOCK = 16

    fun encrypt(key: ByteArray, iv: ByteArray, plaintext: ByteArray): ByteArray {
        require(iv.size == BLOCK) { "AES-CBC IV must be 16 bytes" }
        val rk = RoundKeys(key)
        val padded = pkcs7Pad(plaintext)
        val out = ByteArray(padded.size)
        val prev = iv.copyOf()
        var offset = 0
        while (offset < padded.size) {
            for (i in 0 until BLOCK) prev[i] = (padded[offset + i].toInt() xor prev[i].toInt()).toByte()
            encryptBlock(prev, rk)
            prev.copyInto(out, offset)
            offset += BLOCK
        }
        return out
    }

    fun decrypt(key: ByteArray, iv: ByteArray, ciphertext: ByteArray): ByteArray {
        require(iv.size == BLOCK) { "AES-CBC IV must be 16 bytes" }
        require(ciphertext.size % BLOCK == 0 && ciphertext.isNotEmpty()) { "AES-CBC ciphertext must be a positive multiple of 16 bytes" }
        val rk = RoundKeys(key)
        val out = ByteArray(ciphertext.size)
        val prev = iv.copyOf()
        val work = ByteArray(BLOCK)
        var offset = 0
        while (offset < ciphertext.size) {
            ciphertext.copyInto(work, 0, offset, offset + BLOCK)
            decryptBlock(work, rk)
            for (i in 0 until BLOCK) {
                out[offset + i] = (work[i].toInt() xor prev[i].toInt()).toByte()
                prev[i] = ciphertext[offset + i]
            }
            offset += BLOCK
        }
        return pkcs7Unpad(out)
    }

    private fun pkcs7Pad(data: ByteArray): ByteArray {
        val padLen = BLOCK - (data.size % BLOCK)
        val out = data.copyOf(data.size + padLen)
        for (i in data.size until out.size) out[i] = padLen.toByte()
        return out
    }

    private fun pkcs7Unpad(data: ByteArray): ByteArray {
        val padLen = data.last().toInt() and 0xFF
        require(padLen in 1..BLOCK && padLen <= data.size) { "Invalid PKCS#7 padding" }
        for (i in data.size - padLen until data.size) {
            require((data[i].toInt() and 0xFF) == padLen) { "Invalid PKCS#7 padding" }
        }
        return data.copyOf(data.size - padLen)
    }

    // --- AES core ---------------------------------------------------------

    private class RoundKeys(key: ByteArray) {
        val rounds: Int
        val words: IntArray

        init {
            val nk = key.size / 4
            require(key.size == 16 || key.size == 24 || key.size == 32) { "AES key must be 16/24/32 bytes" }
            rounds = nk + 6
            val total = 4 * (rounds + 1)
            words = IntArray(total)
            for (i in 0 until nk) {
                words[i] = (key[4 * i].toInt() and 0xFF shl 24) or
                        (key[4 * i + 1].toInt() and 0xFF shl 16) or
                        (key[4 * i + 2].toInt() and 0xFF shl 8) or
                        (key[4 * i + 3].toInt() and 0xFF)
            }
            var rcon = 0x01
            for (i in nk until total) {
                var temp = words[i - 1]
                if (i % nk == 0) {
                    temp = subWord(rotWord(temp)) xor (rcon shl 24)
                    rcon = xtime(rcon)
                } else if (nk > 6 && i % nk == 4) {
                    temp = subWord(temp)
                }
                words[i] = words[i - nk] xor temp
            }
        }
    }

    private fun encryptBlock(state: ByteArray, rk: RoundKeys) {
        addRoundKey(state, rk, 0)
        for (round in 1 until rk.rounds) {
            subBytes(state)
            shiftRows(state)
            mixColumns(state)
            addRoundKey(state, rk, round)
        }
        subBytes(state)
        shiftRows(state)
        addRoundKey(state, rk, rk.rounds)
    }

    private fun decryptBlock(state: ByteArray, rk: RoundKeys) {
        addRoundKey(state, rk, rk.rounds)
        for (round in rk.rounds - 1 downTo 1) {
            invShiftRows(state)
            invSubBytes(state)
            addRoundKey(state, rk, round)
            invMixColumns(state)
        }
        invShiftRows(state)
        invSubBytes(state)
        addRoundKey(state, rk, 0)
    }

    private fun addRoundKey(s: ByteArray, rk: RoundKeys, round: Int) {
        for (c in 0 until 4) {
            val w = rk.words[round * 4 + c]
            s[4 * c] = (s[4 * c].toInt() xor (w ushr 24 and 0xFF)).toByte()
            s[4 * c + 1] = (s[4 * c + 1].toInt() xor (w ushr 16 and 0xFF)).toByte()
            s[4 * c + 2] = (s[4 * c + 2].toInt() xor (w ushr 8 and 0xFF)).toByte()
            s[4 * c + 3] = (s[4 * c + 3].toInt() xor (w and 0xFF)).toByte()
        }
    }

    private fun subBytes(s: ByteArray) {
        for (i in s.indices) s[i] = SBOX[s[i].toInt() and 0xFF]
    }

    private fun invSubBytes(s: ByteArray) {
        for (i in s.indices) s[i] = INV_SBOX[s[i].toInt() and 0xFF]
    }

    private fun shiftRows(s: ByteArray) {
        val t = ByteArray(16)
        for (r in 0 until 4) for (c in 0 until 4) t[r + 4 * c] = s[r + 4 * ((c + r) % 4)]
        t.copyInto(s)
    }

    private fun invShiftRows(s: ByteArray) {
        val t = ByteArray(16)
        for (r in 0 until 4) for (c in 0 until 4) t[r + 4 * c] = s[r + 4 * ((c - r + 4) % 4)]
        t.copyInto(s)
    }

    private fun mixColumns(s: ByteArray) {
        for (c in 0 until 4) {
            val i = 4 * c
            val a0 = s[i].toInt() and 0xFF
            val a1 = s[i + 1].toInt() and 0xFF
            val a2 = s[i + 2].toInt() and 0xFF
            val a3 = s[i + 3].toInt() and 0xFF
            s[i] = (gmul(a0, 2) xor gmul(a1, 3) xor a2 xor a3).toByte()
            s[i + 1] = (a0 xor gmul(a1, 2) xor gmul(a2, 3) xor a3).toByte()
            s[i + 2] = (a0 xor a1 xor gmul(a2, 2) xor gmul(a3, 3)).toByte()
            s[i + 3] = (gmul(a0, 3) xor a1 xor a2 xor gmul(a3, 2)).toByte()
        }
    }

    private fun invMixColumns(s: ByteArray) {
        for (c in 0 until 4) {
            val i = 4 * c
            val a0 = s[i].toInt() and 0xFF
            val a1 = s[i + 1].toInt() and 0xFF
            val a2 = s[i + 2].toInt() and 0xFF
            val a3 = s[i + 3].toInt() and 0xFF
            s[i] = (gmul(a0, 14) xor gmul(a1, 11) xor gmul(a2, 13) xor gmul(a3, 9)).toByte()
            s[i + 1] = (gmul(a0, 9) xor gmul(a1, 14) xor gmul(a2, 11) xor gmul(a3, 13)).toByte()
            s[i + 2] = (gmul(a0, 13) xor gmul(a1, 9) xor gmul(a2, 14) xor gmul(a3, 11)).toByte()
            s[i + 3] = (gmul(a0, 11) xor gmul(a1, 13) xor gmul(a2, 9) xor gmul(a3, 14)).toByte()
        }
    }

    private fun rotWord(w: Int): Int = (w shl 8) or (w ushr 24)

    private fun subWord(w: Int): Int =
        (SBOX[w ushr 24 and 0xFF].toInt() and 0xFF shl 24) or
                (SBOX[w ushr 16 and 0xFF].toInt() and 0xFF shl 16) or
                (SBOX[w ushr 8 and 0xFF].toInt() and 0xFF shl 8) or
                (SBOX[w and 0xFF].toInt() and 0xFF)

    private fun xtime(x: Int): Int {
        val shifted = x shl 1
        return (if (x and 0x80 != 0) shifted xor 0x1B else shifted) and 0xFF
    }

    private fun gmul(a: Int, b: Int): Int {
        var aa = a and 0xFF
        var bb = b and 0xFF
        var p = 0
        repeat(8) {
            if (bb and 1 != 0) p = p xor aa
            aa = xtime(aa)
            bb = bb shr 1
        }
        return p and 0xFF
    }

    // S-box + inverse, derived from GF(2^8) inverses and the affine map (no hand-typed tables).
    private val SBOX: ByteArray
    private val INV_SBOX: ByteArray

    init {
        val inv = IntArray(256)
        // GF(2^8) inverses via log/antilog tables (generator 3).
        val log = IntArray(256)
        val alog = IntArray(256)
        var x = 1
        for (i in 0 until 255) {
            alog[i] = x
            log[x] = i
            x = x xor xtime(x) // multiply by 3: x*2 xor x
        }
        inv[0] = 0
        for (i in 1 until 256) inv[i] = alog[(255 - log[i]) % 255]

        val sbox = ByteArray(256)
        val invSbox = ByteArray(256)
        for (i in 0 until 256) {
            val b = inv[i]
            var s = b
            s = s xor rotl8(b, 1) xor rotl8(b, 2) xor rotl8(b, 3) xor rotl8(b, 4) xor 0x63
            s = s and 0xFF
            sbox[i] = s.toByte()
            invSbox[s] = i.toByte()
        }
        SBOX = sbox
        INV_SBOX = invSbox
    }

    private fun rotl8(b: Int, n: Int): Int = ((b shl n) or (b ushr (8 - n))) and 0xFF
}

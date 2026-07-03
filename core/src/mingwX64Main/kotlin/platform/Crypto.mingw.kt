package platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.windows.CryptProtectData
import platform.windows.CryptUnprotectData
import platform.windows.DATA_BLOB
import platform.windows.LocalFree
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// DPAPI binds the ciphertext to the current Windows user; there is no key to manage.
actual suspend fun generateKey() = Unit

@OptIn(ExperimentalForeignApi::class, ExperimentalEncodingApi::class)
actual suspend fun ownEncrypt(string: String): Pair<String, String> = memScoped {
    val input = string.encodeToByteArray()
    if (input.isEmpty()) return@memScoped Pair("", "")
    input.usePinned { pinned ->
        val inBlob = alloc<DATA_BLOB>().apply {
            pbData = pinned.addressOf(0).reinterpret()
            cbData = input.size.toUInt()
        }
        val outBlob = alloc<DATA_BLOB>()
        check(CryptProtectData(inBlob.ptr, null, null, null, null, 0u, outBlob.ptr) != 0) {
            "CryptProtectData failed"
        }
        val encrypted = outBlob.pbData!!.readBytes(outBlob.cbData.toInt())
        LocalFree(outBlob.pbData)
        Pair(Base64.encode(encrypted), "")
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalEncodingApi::class)
actual suspend fun ownDecrypt(encryptedText: String, encodedIv: String): String = memScoped {
    val input = Base64.decode(encryptedText)
    if (input.isEmpty()) return@memScoped ""
    input.usePinned { pinned ->
        val inBlob = alloc<DATA_BLOB>().apply {
            pbData = pinned.addressOf(0).reinterpret()
            cbData = input.size.toUInt()
        }
        val outBlob = alloc<DATA_BLOB>()
        // Fails when the blob was protected by another user/machine; treat the
        // stored credential as absent instead of crashing the CLI.
        if (CryptUnprotectData(inBlob.ptr, null, null, null, null, 0u, outBlob.ptr) == 0) {
            return@memScoped ""
        }
        val decrypted = outBlob.pbData!!.readBytes(outBlob.cbData.toInt())
        LocalFree(outBlob.pbData)
        decrypted.decodeToString()
    }
}

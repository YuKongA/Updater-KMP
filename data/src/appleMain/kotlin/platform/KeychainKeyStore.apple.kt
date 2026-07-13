package platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecRandomDefault
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/** Device-scoped generic-password Keychain item holding the credential AES key (never synced to iCloud). */
@OptIn(ExperimentalForeignApi::class)
internal object KeychainKeyStore {
    private const val SERVICE = "top.yukonga.updater.kmp"
    private const val ACCOUNT = "credentialKey"

    fun randomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        bytes.usePinned { SecRandomCopyBytes(kSecRandomDefault, size.convert(), it.addressOf(0)) }
        return bytes
    }

    fun load(): ByteArray? = memScoped {
        val query = baseQuery().apply {
            CFDictionaryAddValue(this, kSecReturnData, kCFBooleanTrue)
            CFDictionaryAddValue(this, kSecMatchLimit, kSecMatchLimitOne)
        }
        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        CFRelease(query)
        if (status != errSecSuccess) {
            result.value?.let { CFRelease(it) }
            return@memScoped null
        }
        val data: CFDataRef = result.value?.reinterpret() ?: return@memScoped null
        val length = CFDataGetLength(data).toInt()
        val bytes = ByteArray(length)
        val src = CFDataGetBytePtr(data)
        if (length > 0 && src != null) {
            for (i in 0 until length) bytes[i] = src[i].toByte()
        }
        CFRelease(data)
        bytes
    }

    fun save(bytes: ByteArray) {
        CFRelease(baseQuery().also { SecItemDelete(it) })
        val cfData = bytes.usePinned {
            CFDataCreate(kCFAllocatorDefault, it.addressOf(0).reinterpret(), bytes.size.convert())
        }
        val query = baseQuery().apply {
            CFDictionaryAddValue(this, kSecValueData, cfData)
            CFDictionaryAddValue(this, kSecAttrAccessible, kSecAttrAccessibleWhenUnlockedThisDeviceOnly)
        }
        SecItemAdd(query, null)
        CFRelease(query)
        cfData?.let { CFRelease(it) }
    }

    private fun baseQuery() = memScoped {
        val dict = CFDictionaryCreateMutable(
            kCFAllocatorDefault, 0,
            kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr,
        )
        CFDictionaryAddValue(dict, kSecClass, kSecClassGenericPassword)
        // CFDictionaryAddValue retains the value, so release the CFStrings we
        // created here; the dict keeps them alive until it is itself released.
        val service = cfString(SERVICE)
        CFDictionaryAddValue(dict, kSecAttrService, service)
        service?.let { CFRelease(it) }
        val account = cfString(ACCOUNT)
        CFDictionaryAddValue(dict, kSecAttrAccount, account)
        account?.let { CFRelease(it) }
        dict
    }

    private fun cfString(value: String) =
        CFStringCreateWithCString(kCFAllocatorDefault, value, kCFStringEncodingUTF8)
}

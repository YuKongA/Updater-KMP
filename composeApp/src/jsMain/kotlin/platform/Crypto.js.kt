package platform

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.providers.webcrypto.WebCrypto

actual suspend fun provider() = CryptographyProvider.WebCrypto

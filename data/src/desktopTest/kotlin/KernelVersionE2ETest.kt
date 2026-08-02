import data.repository.KernelVersionFetcherImpl
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
import kotlin.test.Test

/**
 * Network-bound smoke test against a real ROM package on Xiaomi's CDN; run
 * manually with:
 *   gradlew :data:desktopTest --tests KernelVersionE2ETest
 */
@Ignore
class KernelVersionE2ETest {

    @Test
    fun readsKernelVersionFromRealRom() = runBlocking {
        val url = "https://cdnorg.d.miui.com/OS2.0.5.0.VNJCNXM/chenfeng-ota_full-OS2.0.5.0.VNJCNXM-user-15.0-759bd71420.zip"
        val version = KernelVersionFetcherImpl().getKernelVersion(url)
        println("kernel version = $version")
        kotlin.test.assertNotNull(version, "kernel version should be extracted")
        kotlin.test.assertTrue(version.startsWith("5.") || version.startsWith("6."), "implausible version: $version")
    }
}

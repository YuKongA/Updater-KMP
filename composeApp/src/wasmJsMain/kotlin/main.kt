import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.CanvasBasedWindow
import io.ktor.client.fetch.Response
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

private const val MiSanVF = "./MiSansVF.woff2"

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "Updater") {
        val fontFamilyResolver = LocalFontFamilyResolver.current
        val fontsLoaded = remember { mutableStateOf(false) }

        if (fontsLoaded.value) {
            App()
        }

        LaunchedEffect(Unit) {
            val miSanVFBytes = loadRes(MiSanVF).toByteArray()
            val fontFamily = FontFamily(Font("MiSans VF", miSanVFBytes))
            fontFamilyResolver.preload(fontFamily)
            fontsLoaded.value = true
        }
    }
}

suspend fun loadRes(url: String): ArrayBuffer {
    return window.fetch(url).await<Response>().arrayBuffer().await()
}

fun ArrayBuffer.toByteArray(): ByteArray {
    val source = Int8Array(this, 0, byteLength)
    return jsInt8ArrayToKotlinByteArray(source)
}

@JsFun(
    """ (src, size, dstAddr) => {
        const mem8 = new Int8Array(wasmExports.memory.buffer, dstAddr, size);
        mem8.set(src);
    }
"""
)
external fun jsExportInt8ArrayToWasm(src: Int8Array, size: Int, dstAddr: Int)

internal fun jsInt8ArrayToKotlinByteArray(x: Int8Array): ByteArray {
    val size = x.length

    @OptIn(UnsafeWasmMemoryApi::class)
    return withScopedMemoryAllocator { allocator ->
        val memBuffer = allocator.allocate(size)
        val dstAddress = memBuffer.address.toInt()
        jsExportInt8ArrayToWasm(x, size, dstAddress)
        ByteArray(size) { i -> (memBuffer + i).loadByte() }
    }
}
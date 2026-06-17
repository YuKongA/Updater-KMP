import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
        function hideLoading() {
            if (window.__updaterLoading) {
                window.__updaterLoading.finish();
            } else {
                const el = document.getElementById('loading');
                if (el) el.style.display = 'none';
            }
        }
    """,
)
private external fun hideLoading()

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
        function setFontProgress(done, total) {
            if (window.__updaterLoading && window.__updaterLoading.fontProgress) {
                window.__updaterLoading.fontProgress(done, total);
            }
        }
    """,
)
private external fun setFontProgress(done: Int, total: Int)

fun platformHideLoading() = hideLoading()

fun platformSetFontProgress(done: Int, total: Int) = setFontProgress(done, total)

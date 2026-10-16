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

fun platformHideLoading() = hideLoading()


import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement

actual fun downloadToLocal(url: String, fileName: String) {
    val anchorElement = document.createElement("a") as HTMLAnchorElement
    anchorElement.href = url
    anchorElement.download = fileName
    document.body?.appendChild(anchorElement)
    anchorElement.click()
    document.body?.removeChild(anchorElement)
}
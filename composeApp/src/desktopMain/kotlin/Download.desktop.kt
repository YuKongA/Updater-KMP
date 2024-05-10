import java.awt.Desktop
import java.net.URI

actual fun downloadToLocal(url: String, fileName: String) {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI(url))
    }
}
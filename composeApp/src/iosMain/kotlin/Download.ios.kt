import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun downloadToLocal(url: String, fileName: String) {
    val openUrl = NSURL(string = url)
    UIApplication.sharedApplication.openURL(openUrl)
}
package platform

import platform.AppKit.NSWorkspace
import platform.Foundation.NSURL

actual fun downloadToLocal(url: String, fileName: String) {
    val openUrl = NSURL(string = url)
    NSWorkspace.sharedWorkspace().openURL(openUrl)
}
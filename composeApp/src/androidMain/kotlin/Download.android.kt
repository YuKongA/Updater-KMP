import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import top.yukonga.updater.kmm.AndroidAppContext

actual fun downloadToLocal(url: String, fileName: String) {
    val request = DownloadManager.Request(url.toUri()).apply {
        setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        setTitle(fileName)
        setDescription(fileName)
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
    }
    val context = AndroidAppContext.getApplicationContext()
    val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)
}
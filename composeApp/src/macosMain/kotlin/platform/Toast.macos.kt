package platform

actual fun useToast(): Boolean = false
actual fun showToast(message: String, duration: Long) {}
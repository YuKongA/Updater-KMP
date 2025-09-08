package platform

import androidx.compose.ui.graphics.ImageBitmap

expect suspend fun loadImageFromUrl(url: String): ImageBitmap?
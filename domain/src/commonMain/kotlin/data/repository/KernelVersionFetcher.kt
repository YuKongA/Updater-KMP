package data.repository

interface KernelVersionFetcher {
    /**
     * The kernel release (e.g. "6.6.30-android15-8-g...") of the boot image
     * inside the ROM package at [url]; null when it cannot be determined.
     */
    suspend fun getKernelVersion(url: String): String?
}

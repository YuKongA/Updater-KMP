package data

import chromeos_update_engine.DeltaArchiveManifest

class PayloadHelper {

    data class PayloadInfo(
        val fileName: String,
        val header: PayloadHeader,
        val deltaArchiveManifest: DeltaArchiveManifest,
        val dataOffset: Long,
        val blockSize: Int,
        val archiveSize: Long,
        val isPath: Boolean
    )

    data class PayloadHeader(
        val fileFormatVersion: Long,
        val manifestSize: Long,
        val metadataSignatureSize: Int,
    )

    data class PartitionInfo(
        val partitionName: String,
        val size: Long,
        val rawSize: Long,
        val sha256: String,
        val isDownloading: Boolean = false,
        val progress: Float = 0f,
    )
}
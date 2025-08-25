package ui.components

import PayloadAnalyzer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.PayloadHelper
import kotlinx.coroutines.launch
import misc.MessageUtils.Companion.showMessage
import misc.PartitionDownloadManager
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Save
import top.yukonga.miuix.kmp.theme.MiuixTheme
import ui.MessageTextView

@Composable
fun PayloadDumperView(
    url: String,
    modifier: Modifier = Modifier
) {
    var payloadInfo by remember { mutableStateOf<PayloadHelper.PayloadInfo?>(null) }
    var partitionList by remember { mutableStateOf<List<PayloadHelper.PartitionInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(url) {
        if (url.isNotEmpty()) {
            isLoading = true
            errorMessage = ""
            try {
                payloadInfo = PayloadAnalyzer.analyzePayload(url)
                if (payloadInfo != null) {
                    partitionList = PartitionDownloadManager.getAllPartitionsInfo(payloadInfo!!)
                } else {
                    errorMessage = "Failed to analyze payload.bin"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    AnimatedVisibility(
        visible = url.isNotEmpty(),
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.padding(bottom = 12.dp),
            insideMargin = PaddingValues(16.dp)
        ) {
            Text(
                text = "PAYLOAD",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage.isNotEmpty() -> {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                payloadInfo != null -> {
                    PayloadInfoContent(
                        payloadInfo = payloadInfo!!,
                        partitionList = partitionList,
                        onPartitionDownload = { partitionName ->
                            coroutineScope.launch {
                                try {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMessage("Starting download of $partitionName partition...")

                                    partitionList = partitionList.map { partition ->
                                        if (partition.partitionName == partitionName) {
                                            partition.copy(isDownloading = true, progress = 0f)
                                        } else partition
                                    }

                                    PartitionDownloadManager.downloadPartitionToFile(
                                        url = url,
                                        payloadInfo = payloadInfo!!,
                                        partitionName = partitionName,
                                        onProgress = { progress ->
                                            partitionList = partitionList.map { partition ->
                                                if (partition.partitionName == partitionName) {
                                                    partition.copy(
                                                        isDownloading = !progress.isCompleted,
                                                        progress = progress.progress
                                                    )
                                                } else partition
                                            }
                                        }
                                    ).fold(
                                        onSuccess = { filePath ->
                                            showMessage("Successfully downloaded $partitionName to: $filePath")
                                        },
                                        onFailure = { error ->
                                            showMessage("Failed to download $partitionName: ${error.message}")

                                            partitionList = partitionList.map { partition ->
                                                if (partition.partitionName == partitionName) {
                                                    partition.copy(isDownloading = false, progress = 0f)
                                                } else partition
                                            }
                                        }
                                    )
                                } catch (e: Exception) {
                                    showMessage("Error downloading partition: ${e.message}")

                                    partitionList = partitionList.map { partition ->
                                        if (partition.partitionName == partitionName) {
                                            partition.copy(isDownloading = false, progress = 0f)
                                        } else partition
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PayloadInfoContent(
    payloadInfo: PayloadHelper.PayloadInfo,
    partitionList: List<PayloadHelper.PartitionInfo>,
    onPartitionDownload: (String) -> Unit
) {
    Column {
        MessageTextView("Format Version", payloadInfo.header.fileFormatVersion.toString())
        MessageTextView("Manifest Size", "${payloadInfo.header.manifestSize} bytes")
        MessageTextView("Block Size", "${payloadInfo.blockSize} bytes")
        MessageTextView("Data Offset", "${payloadInfo.dataOffset} bytes")
        MessageTextView("Archive Size", PartitionDownloadManager.formatFileSize(payloadInfo.archiveSize))
        MessageTextView("Partitions Count", "${partitionList.size}")

        Text(
            text = "Partitions",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(partitionList) { partition ->
                PartitionCard(
                    partition = partition,
                    onDownloadClick = { onPartitionDownload(partition.partitionName) }
                )
            }
        }
    }
}

@Composable
private fun PartitionCard(
    partition: PayloadHelper.PartitionInfo,
    onDownloadClick: () -> Unit
) {

    BasicComponent(
        title = partition.partitionName,
        summary = "Size: ${PartitionDownloadManager.formatFileSize(partition.size)}" +
                "\nRaw Size: ${PartitionDownloadManager.formatFileSize(partition.rawSize)}",
        rightActions = {
            if (partition.isDownloading) {
                CircularProgressIndicator(
                    progress = partition.progress
                )
            } else {
                IconButton(
                    onClick = onDownloadClick
                ) {
                    Icon(
                        imageVector = MiuixIcons.Useful.Save,
                        contentDescription = "Download partition",
                        tint = MiuixTheme.colorScheme.onSurface
                    )
                }
            }
        },
        insideMargin = PaddingValues(0.dp),
    )
}

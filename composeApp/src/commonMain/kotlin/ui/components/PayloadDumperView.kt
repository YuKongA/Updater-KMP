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
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
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
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.analysis
import updater.composeapp.generated.resources.analysis_failed
import updater.composeapp.generated.resources.archive_size
import updater.composeapp.generated.resources.download_error
import updater.composeapp.generated.resources.download_failed
import updater.composeapp.generated.resources.download_partition
import updater.composeapp.generated.resources.download_start
import updater.composeapp.generated.resources.download_successful
import updater.composeapp.generated.resources.partition_count
import updater.composeapp.generated.resources.partition_list

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PayloadDumperView(
    url: String,
    version: String,
    modifier: Modifier = Modifier
) {
    // 预先获取所有需要的字符串资源
    val analysisText = stringResource(Res.string.analysis)
    val analysisFailedText = stringResource(Res.string.analysis_failed)
    val downloadStartText = stringResource(Res.string.download_start)
    val downloadSuccessfulText = stringResource(Res.string.download_successful)
    val downloadFailedText = stringResource(Res.string.download_failed)
    val downloadErrorText = stringResource(Res.string.download_error)
    val archiveSizeText = stringResource(Res.string.archive_size)
    val partitionCountText = stringResource(Res.string.partition_count)
    val partitionListText = stringResource(Res.string.partition_list)
    val downloadPartitionText = stringResource(Res.string.download_partition)

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
                    errorMessage = analysisFailedText
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
                text = analysisText,
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
                        archiveSizeText = archiveSizeText,
                        partitionCountText = partitionCountText,
                        partitionListText = partitionListText,
                        downloadPartitionText = downloadPartitionText,
                        onPartitionDownload = { partitionName ->
                            coroutineScope.launch {
                                try {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMessage("$downloadStartText $partitionName ...")

                                    partitionList = partitionList.map { partition ->
                                        if (partition.partitionName == partitionName) {
                                            partition.copy(isDownloading = true, progress = 0f)
                                        } else partition
                                    }

                                    PartitionDownloadManager.downloadPartitionToFile(
                                        url = url,
                                        payloadInfo = payloadInfo!!,
                                        partitionName = partitionName,
                                        version = version,
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
                                            showMessage("$downloadSuccessfulText: $filePath")
                                        },
                                        onFailure = { error ->
                                            showMessage("$downloadFailedText: ${error.message}")

                                            partitionList = partitionList.map { partition ->
                                                if (partition.partitionName == partitionName) {
                                                    partition.copy(isDownloading = false, progress = 0f)
                                                } else partition
                                            }
                                        }
                                    )
                                } catch (e: Exception) {
                                    showMessage("$downloadErrorText: ${e.message}")

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
    archiveSizeText: String,
    partitionCountText: String,
    partitionListText: String,
    downloadPartitionText: String,
    onPartitionDownload: (String) -> Unit
) {
    Column {
        MessageTextView(archiveSizeText, PartitionDownloadManager.formatFileSize(payloadInfo.archiveSize))
        MessageTextView(partitionCountText, "${partitionList.size}")

        Text(
            text = partitionListText,
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
                    downloadPartitionText = downloadPartitionText,
                    onDownloadClick = { onPartitionDownload(partition.partitionName) }
                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun PartitionCard(
    partition: PayloadHelper.PartitionInfo,
    downloadPartitionText: String,
    onDownloadClick: () -> Unit
) {
    BasicComponent(
        title = partition.partitionName,
        summary = "Size: " + PartitionDownloadManager.formatFileSize(partition.size) +
                "\nRaw Size: " + PartitionDownloadManager.formatFileSize(partition.rawSize),
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
                        contentDescription = downloadPartitionText,
                        tint = MiuixTheme.colorScheme.onSurface
                    )
                }
            }
        },
        insideMargin = PaddingValues(0.dp),
    )
}

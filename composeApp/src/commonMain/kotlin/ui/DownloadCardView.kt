package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import copyToClipboard
import data.DataHelper
import downloadToLocal
import misc.MessageUtils.Companion.showMessage
import misc.bodyFontSize
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.MiuixCard
import top.yukonga.miuix.kmp.basic.MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.copy_button
import updater.composeapp.generated.resources.copy_successful
import updater.composeapp.generated.resources.download
import updater.composeapp.generated.resources.download_button
import updater.composeapp.generated.resources.download_start

@Composable
fun DownloadCardViews(
    romInfoState: MutableState<DataHelper.RomInfoData>
) {
    val isVisible = remember { mutableStateOf(false) }
    isVisible.value = romInfoState.value.official1Download.isNotEmpty()

    AnimatedVisibility(
        visible = isVisible.value,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        MiuixCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 16.dp),
        ) {
            Column {
                MiuixText(
                    text = stringResource(Res.string.download),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    fontSize = bodyFontSize,
                    fontWeight = FontWeight.SemiBold
                )
                DownloadTextView(
                    "Official (ultimateota)",
                    romInfoState.value.official1Download,
                    romInfoState.value.official1Download,
                    romInfoState.value.fileName
                )
                DownloadTextView("Official (superota)", romInfoState.value.official2Download, romInfoState.value.official2Download, romInfoState.value.fileName)
                DownloadTextView("CDN (cdnorg)", romInfoState.value.cdn1Download, romInfoState.value.cdn1Download, romInfoState.value.fileName)
                DownloadTextView("CDN (aliyuncs)", romInfoState.value.cdn2Download, romInfoState.value.cdn2Download, romInfoState.value.fileName, 0.dp)
            }
        }
    }
}

@Composable
fun DownloadTextView(
    title: String,
    copy: String,
    download: String,
    fileName: String,
    bottomPadding: Dp = 8.dp,
) {
    val hapticFeedback = LocalHapticFeedback.current

    val messageCopySuccessful = stringResource(Res.string.copy_successful)
    val messageDownloadStart = stringResource(Res.string.download_start)

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = bottomPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MiuixText(
            modifier = Modifier.fillMaxWidth(0.5f),
            text = title,
            fontSize = bodyFontSize,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (copy.isNotEmpty()) {
                MiuixText(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable(
                            enabled = true,
                            onClick = {
                                copyToClipboard(copy)
                                showMessage(messageCopySuccessful)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        ),
                    text = stringResource(Res.string.copy_button),
                    fontSize = bodyFontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.primary
                )
                MiuixText(
                    modifier = Modifier.clickable(
                        onClick = {
                            downloadToLocal(download, fileName)
                            showMessage(messageDownloadStart)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    ),
                    text = stringResource(Res.string.download_button),
                    fontSize = bodyFontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.primary
                )
            }
        }
    }
}
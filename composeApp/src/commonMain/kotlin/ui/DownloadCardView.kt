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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import misc.SnackbarUtils.Companion.showSnackbar
import org.jetbrains.compose.resources.stringResource
import updaterkmp.composeapp.generated.resources.Res
import updaterkmp.composeapp.generated.resources.copy_button
import updaterkmp.composeapp.generated.resources.copy_successful
import updaterkmp.composeapp.generated.resources.download
import updaterkmp.composeapp.generated.resources.download_button
import updaterkmp.composeapp.generated.resources.download_start

@Composable
fun DownloadCardViews(
    romInfoState: MutableState<DataHelper.RomInfoData>
) {
    val isVisible = remember { mutableStateOf(false) }
    isVisible.value = romInfoState.value.officialDownload.isNotEmpty()

    AnimatedVisibility(
        visible = isVisible.value,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Card(
            colors = CardDefaults.cardColors(
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(Res.string.download),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontWeight = FontWeight.SemiBold
                )
                DownloadTextView("Official (ultimateota)", romInfoState.value.officialDownload, romInfoState.value.officialDownload, romInfoState.value.fileName)
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
        Text(
            text = title,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(0.5f),
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (copy.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .clickable(
                            enabled = true,
                            onClick = {
                                copyToClipboard(copy)
                                showSnackbar(messageCopySuccessful)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        ),
                    text = stringResource(Res.string.copy_button),
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = ButtonDefaults.textButtonColors().contentColor
                )
                Text(
                    modifier = Modifier.clickable(
                        onClick = {
                            downloadToLocal(download, fileName)
                            showSnackbar(messageDownloadStart)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    ),
                    text = stringResource(Res.string.download_button),
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = ButtonDefaults.textButtonColors().contentColor
                )
            }
        }
    }
}
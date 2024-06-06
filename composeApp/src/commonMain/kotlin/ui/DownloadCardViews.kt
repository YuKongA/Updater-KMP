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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import copyToClipboard
import downloadToLocal
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import updaterkmm.composeapp.generated.resources.Res
import updaterkmm.composeapp.generated.resources.copy_button
import updaterkmm.composeapp.generated.resources.copy_successful
import updaterkmm.composeapp.generated.resources.download
import updaterkmm.composeapp.generated.resources.download_button
import updaterkmm.composeapp.generated.resources.download_start

@Composable
fun DownloadCardViews(
    officialDownload: MutableState<String>,
    cdn1Download: MutableState<String>,
    cdn2Download: MutableState<String>,
    fileName: MutableState<String>,
    snackBarHostState: SnackbarHostState
) {
    val isVisible = remember { mutableStateOf(false) }
    isVisible.value = officialDownload.value.isNotEmpty()

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
                DownloadTextView("Official (ultimateota)", officialDownload.value, officialDownload.value, fileName.value, snackBarHostState)
                DownloadTextView("CDN (cdnorg)", cdn1Download.value, cdn1Download.value, fileName.value, snackBarHostState)
                DownloadTextView("CDN (aliyuncs)", cdn2Download.value, cdn2Download.value, fileName.value, snackBarHostState, 0.dp)
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
    snackBarHostState: SnackbarHostState,
    bottomPadding: Dp = 8.dp,
) {
    val coroutineScope = rememberCoroutineScope()

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
                                coroutineScope.launch { snackBarHostState.showSnackbar(message = messageCopySuccessful) }
                            }
                        ),
                    text = stringResource(Res.string.copy_button),
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = ButtonDefaults.textButtonColors().contentColor
                )
                Text(
                    modifier = Modifier
                        .clickable(
                            enabled = true,
                            onClick = {
                                downloadToLocal(download, fileName)
                                coroutineScope.launch { snackBarHostState.showSnackbar(message = messageDownloadStart) }
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
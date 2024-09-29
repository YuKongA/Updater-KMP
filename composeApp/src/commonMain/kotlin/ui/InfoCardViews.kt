package ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import copyToClipboard
import data.DataHelper
import downloadToLocal
import misc.MessageUtils.Companion.showMessage
import misc.bodyFontSize
import misc.bodySmallFontSize
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import ui.components.TextWithIcon
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.android_version
import updater.composeapp.generated.resources.big_version
import updater.composeapp.generated.resources.branch
import updater.composeapp.generated.resources.changelog
import updater.composeapp.generated.resources.code_name
import updater.composeapp.generated.resources.copy_successful
import updater.composeapp.generated.resources.download
import updater.composeapp.generated.resources.download_start
import updater.composeapp.generated.resources.filename
import updater.composeapp.generated.resources.filesize
import updater.composeapp.generated.resources.system_version

@Composable
fun InfoCardViews(
    romInfoState: MutableState<DataHelper.RomInfoData>,
    iconInfo: MutableState<List<DataHelper.IconInfoData>>
) {
    val isVisible = remember { mutableStateOf(false) }
    isVisible.value = romInfoState.value.type.isNotEmpty()

    AnimatedVisibility(
        visible = isVisible.value,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Card(
            modifier = Modifier.padding(vertical = 6.dp),
            insideMargin = DpSize(16.dp, 16.dp)
        ) {
            Text(
                text = romInfoState.value.type.uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            BaseMessageView(
                romInfoState.value.device,
                romInfoState.value.version,
                romInfoState.value.bigVersion,
                romInfoState.value.codebase,
                romInfoState.value.branch
            )
            MessageTextView(
                stringResource(Res.string.filename),
                romInfoState.value.fileName
            )
            MessageTextView(
                stringResource(Res.string.filesize),
                romInfoState.value.fileSize
            )

            Text(
                text = stringResource(Res.string.download),
                color = MiuixTheme.colorScheme.onSecondaryVariant,
                fontSize = bodySmallFontSize
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                DownloadInfoView(
                    modifier = Modifier.weight(1f),
                    "ultimateota",
                    romInfoState.value.official1Download,
                    romInfoState.value.official1Download,
                    romInfoState.value.fileName
                )
                DownloadInfoView(
                    modifier = Modifier.weight(1f),
                    "superota",
                    romInfoState.value.official2Download,
                    romInfoState.value.official2Download,
                    romInfoState.value.fileName
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                DownloadInfoView(
                    modifier = Modifier.weight(1f),
                    "cdnorg",
                    romInfoState.value.cdn1Download,
                    romInfoState.value.cdn1Download,
                    romInfoState.value.fileName
                )
                DownloadInfoView(
                    modifier = Modifier.weight(1f),
                    "aliyuncs",
                    romInfoState.value.cdn2Download,
                    romInfoState.value.cdn2Download,
                    romInfoState.value.fileName
                )
            }

            ChangelogView(
                iconInfo,
                romInfoState.value.changelog
            )
        }
    }
}

@Composable
fun BaseMessageView(
    device: String,
    version: String,
    bigVersion: String,
    codebase: String,
    branch: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        MessageTextView(stringResource(Res.string.code_name), device)
        MessageTextView(stringResource(Res.string.system_version), version)
        MessageTextView(stringResource(Res.string.big_version), bigVersion)
        MessageTextView(stringResource(Res.string.android_version), codebase)
        MessageTextView(stringResource(Res.string.branch), branch)
    }
}

@Composable
fun MessageTextView(
    title: String,
    text: String
) {
    val content = remember { mutableStateOf("") }
    content.value = text

    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Text(
            text = title,
            color = MiuixTheme.colorScheme.onSecondaryVariant,
            fontSize = bodySmallFontSize
        )
        AnimatedContent(
            targetState = content.value,
            transitionSpec = {
                fadeIn(animationSpec = tween(1500)) togetherWith fadeOut(animationSpec = tween(300))
            }
        ) {
            Text(
                text = it,
                fontSize = bodyFontSize,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DownloadInfoView(
    modifier: Modifier = Modifier,
    title: String,
    copy: String,
    download: String,
    fileName: String
) {
    val hapticFeedback = LocalHapticFeedback.current

    val messageCopySuccessful = stringResource(Res.string.copy_successful)
    val messageDownloadStart = stringResource(Res.string.download_start)

    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            fontSize = bodyFontSize,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Row {
            if (copy.isNotEmpty()) {
                IconButton(
                    onClick = {
                        copyToClipboard(copy)
                        showMessage(messageCopySuccessful)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = {
                        downloadToLocal(download, fileName)
                        showMessage(messageDownloadStart)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ChangelogView(
    iconInfo: MutableState<List<DataHelper.IconInfoData>>,
    changelog: String
) {
    val hapticFeedback = LocalHapticFeedback.current

    val messageCopySuccessful = stringResource(Res.string.copy_successful)

    Column {
        Row(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(end = 6.dp),
                text = stringResource(Res.string.changelog),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(
                onClick = {
                    copyToClipboard(changelog)
                    showMessage(messageCopySuccessful)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurface
                )
            }
        }
        iconInfo.value.forEachIndexed { index, it ->
            TextWithIcon(
                changelog = it.changelog,
                iconName = it.iconName,
                iconLink = it.iconLink,
                padding = if (index == iconInfo.value.size - 1) 0.dp else 16.dp
            )
        }
    }
}
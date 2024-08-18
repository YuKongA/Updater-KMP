package ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import copyToClipboard
import data.DataHelper
import downloadToLocal
import misc.MessageUtils.Companion.showMessage
import misc.bodyFontSize
import misc.bodySmallFontSize
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.MiuixCard
import top.yukonga.miuix.kmp.basic.MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme
import ui.components.TextWithIcon
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.android_version
import updater.composeapp.generated.resources.big_version
import updater.composeapp.generated.resources.branch
import updater.composeapp.generated.resources.changelog
import updater.composeapp.generated.resources.code_name
import updater.composeapp.generated.resources.copy_button
import updater.composeapp.generated.resources.copy_successful
import updater.composeapp.generated.resources.download
import updater.composeapp.generated.resources.download_button
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
        Column {
            MiuixCard(
                isSecondary = true,
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                MiuixText(
                    text = romInfoState.value.type.uppercase(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                MessageCardView(
                    romInfoState.value.device,
                    romInfoState.value.version,
                    romInfoState.value.bigVersion,
                    romInfoState.value.codebase,
                    romInfoState.value.branch
                )

                MoreTextView(stringResource(Res.string.filename), romInfoState.value.fileName)
                MoreTextView(stringResource(Res.string.filesize), romInfoState.value.fileSize)
                ChangelogView(iconInfo, romInfoState.value.changelog)

                MiuixText(
                    text = stringResource(Res.string.download),
                    fontSize = bodyFontSize,
                    fontWeight = FontWeight.SemiBold
                )
                DownloadTextView(
                    "Official (ultimateota)",
                    romInfoState.value.official1Download,
                    romInfoState.value.official1Download,
                    romInfoState.value.fileName
                )
                DownloadTextView(
                    "Official (superota)",
                    romInfoState.value.official2Download,
                    romInfoState.value.official2Download,
                    romInfoState.value.fileName
                )
                DownloadTextView(
                    "CDN (cdnorg)",
                    romInfoState.value.cdn1Download,
                    romInfoState.value.cdn1Download,
                    romInfoState.value.fileName
                )
                DownloadTextView(
                    "CDN (aliyuncs)",
                    romInfoState.value.cdn2Download,
                    romInfoState.value.cdn2Download,
                    romInfoState.value.fileName, 0.dp
                )
            }
        }
    }
}

@Composable
fun MessageCardView(
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
    val scrollState = rememberScrollState()
    val content = remember { mutableStateOf("") }
    content.value = text

    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        MiuixText(
            text = title,
            fontSize = bodyFontSize,
            fontWeight = FontWeight.SemiBold
        )
        AnimatedContent(
            targetState = content.value,
            transitionSpec = {
                fadeIn(animationSpec = tween(1500)) togetherWith fadeOut(animationSpec = tween(300))
            }
        ) {
            MiuixText(
                text = it,
                modifier = Modifier.horizontalScroll(scrollState),
                color = MiuixTheme.colorScheme.subTextMain,
                fontSize = bodySmallFontSize,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MoreTextView(
    title: String,
    text: String
) {
    val content = remember { mutableStateOf("") }
    content.value = text

    MiuixText(
        text = title,
        fontSize = bodyFontSize,
        fontWeight = FontWeight.SemiBold
    )
    AnimatedContent(
        targetState = content.value,
        transitionSpec = {
            fadeIn(animationSpec = tween(1500)) togetherWith fadeOut(animationSpec = tween(300))
        }
    ) {
        MiuixText(
            text = it,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MiuixTheme.colorScheme.subTextMain,
            fontSize = bodySmallFontSize,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ChangelogView(
    iconInfo: MutableState<List<DataHelper.IconInfoData>>,
    changelog: String
) {
    val hapticFeedback = LocalHapticFeedback.current

    val messageCopySuccessful = stringResource(Res.string.copy_successful)

    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MiuixText(
                text = stringResource(Res.string.changelog),
                fontSize = bodyFontSize,
                fontWeight = FontWeight.SemiBold
            )
            MiuixText(
                modifier = Modifier.clickable(
                    onClick = {
                        copyToClipboard(changelog)
                        showMessage(messageCopySuccessful)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                ),
                text = stringResource(Res.string.copy_button),
                fontSize = bodyFontSize,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.primary
            )
        }
        iconInfo.value.forEachIndexed { index, it ->
            TextWithIcon(
                changelog = it.changelog,
                iconName = it.iconName,
                iconLink = it.iconLink,
                padding = if (index == iconInfo.value.size - 1) 0.dp else 8.dp
            )
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
            color = MiuixTheme.colorScheme.subTextMain,
            fontSize = bodySmallFontSize,
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
                    fontSize = bodySmallFontSize,
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
                    fontSize = bodySmallFontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.primary
                )
            }
        }
    }
}
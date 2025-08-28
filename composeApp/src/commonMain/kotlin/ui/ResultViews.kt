package ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.DataHelper
import kotlinx.coroutines.launch
import misc.MessageUtils.Companion.showMessage
import misc.bodyFontSize
import misc.bodySmallFontSize
import org.jetbrains.compose.resources.stringResource
import platform.copyToClipboard
import platform.downloadToLocal
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Copy
import top.yukonga.miuix.kmp.icon.icons.useful.Save
import top.yukonga.miuix.kmp.theme.MiuixTheme
import ui.components.TextWithIcon
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.android_version
import updater.composeapp.generated.resources.attention
import updater.composeapp.generated.resources.big_version
import updater.composeapp.generated.resources.branch
import updater.composeapp.generated.resources.build_time
import updater.composeapp.generated.resources.changelog
import updater.composeapp.generated.resources.code_name
import updater.composeapp.generated.resources.copy_successful
import updater.composeapp.generated.resources.download
import updater.composeapp.generated.resources.download_start
import updater.composeapp.generated.resources.filemd5
import updater.composeapp.generated.resources.filename
import updater.composeapp.generated.resources.filesize
import updater.composeapp.generated.resources.fingerprint
import updater.composeapp.generated.resources.security_patch_level
import updater.composeapp.generated.resources.system_version
import updater.composeapp.generated.resources.tags

@Composable
fun InfoCardViews(
    romInfoData: MutableState<DataHelper.RomInfoData>,
    iconInfoData: MutableState<List<DataHelper.IconInfoData>>,
    updateRomInfoState: MutableState<Int>
) {
    val romInfo = romInfoData.value
    val iconInfo = iconInfoData.value

    val isVisible by remember(updateRomInfoState.value, romInfoData.value) {
        derivedStateOf {
            romInfo.fileName.isNotEmpty()
        }
    }
    val hasTimestamp by remember(romInfo) {
        derivedStateOf {
            romInfo.timestamp.isNotEmpty() || romInfo.fingerprint.isNotEmpty() || romInfo.securityPatchLevel.isNotEmpty()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier.padding(bottom = 12.dp),
            insideMargin = PaddingValues(16.dp)
        ) {
            Text(
                text = romInfo.type.uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            BaseMessageView(
                romInfo.device,
                romInfo.version,
                romInfo.bigVersion,
                romInfo.codebase,
                romInfo.branch
            )

            if (romInfo.isBeta) {
                MessageTextView(
                    stringResource(Res.string.tags),
                    "Beta"
                )
            }

            if (romInfo.isGov) {
                MessageTextView(
                    stringResource(Res.string.tags),
                    "Government"
                )
            }

            AnimatedVisibility(
                visible = hasTimestamp
            ) {
                MetadataView(
                    romInfo.fingerprint,
                    romInfo.securityPatchLevel,
                    romInfo.timestamp
                )
            }

            RomFileInfoSection(
                fileName = romInfo.fileName,
                md5 = romInfo.md5,
                fileSize = romInfo.fileSize
            )

            Text(
                text = stringResource(Res.string.download),
                color = MiuixTheme.colorScheme.onSecondaryVariant,
                fontSize = bodySmallFontSize
            )

            if (romInfo.official1Download.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    DownloadInfoView(
                        modifier = Modifier.weight(1f),
                        "ultimateota",
                        romInfo.official1Download,
                        romInfo.fileName
                    )
                    DownloadInfoView(
                        modifier = Modifier.weight(1f),
                        "superota",
                        romInfo.official2Download,
                        romInfo.fileName
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                DownloadInfoView(
                    modifier = Modifier.weight(1f),
                    "aliyuncs",
                    romInfo.cdn1Download,
                    romInfo.fileName
                )
                DownloadInfoView(
                    modifier = Modifier.weight(1f),
                    "cdnorg",
                    romInfo.cdn2Download,
                    romInfo.fileName
                )
            }

            if (romInfo.changelog.isNotEmpty()) {
                ChangelogView(
                    iconInfo,
                    romInfo.changelog
                )
            }

            if (romInfo.gentleNotice.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = stringResource(Res.string.attention),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = romInfo.gentleNotice,
                    color = MiuixTheme.colorScheme.onSecondaryVariant,
                    fontSize = 14.5.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
fun MetadataView(
    fingerprint: String,
    securityPatchLevel: String,
    buildTime: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        MessageTextView(stringResource(Res.string.fingerprint), fingerprint)
        MessageTextView(stringResource(Res.string.security_patch_level), securityPatchLevel)
        MessageTextView(stringResource(Res.string.build_time), buildTime)
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
    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val hapticFeedback = LocalHapticFeedback.current
    val content = remember { mutableStateOf("") }
    val messageCopySuccessful = stringResource(Res.string.copy_successful)
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
                fontWeight = FontWeight.Medium,
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        coroutineScope.launch {
                            clipboard.copyToClipboard(it)
                        }
                        showMessage(messageCopySuccessful)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
            )
        }
    }
}

@Composable
fun DownloadInfoView(
    modifier: Modifier = Modifier,
    title: String,
    url: String,
    fileName: String
) {
    val hapticFeedback = LocalHapticFeedback.current
    val clipboard = LocalClipboard.current

    val coroutineScope = rememberCoroutineScope()

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
            if (url.isNotEmpty()) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            clipboard.copyToClipboard(url)
                        }
                        showMessage(messageCopySuccessful)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                ) {
                    Icon(
                        imageVector = MiuixIcons.Useful.Copy,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = {
                        downloadToLocal(url, fileName)
                        showMessage(messageDownloadStart)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                ) {
                    Icon(
                        imageVector = MiuixIcons.Useful.Save,
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
    iconInfo: List<DataHelper.IconInfoData>,
    changelog: String
) {
    val hapticFeedback = LocalHapticFeedback.current
    val clipboard = LocalClipboard.current

    val coroutineScope = rememberCoroutineScope()

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
                    coroutineScope.launch {
                        clipboard.copyToClipboard(changelog)
                    }
                    showMessage(messageCopySuccessful)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            ) {
                Icon(
                    imageVector = MiuixIcons.Useful.Copy,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurface
                )
            }
        }
        iconInfo.forEachIndexed { index, it ->
            TextWithIcon(
                changelog = it.changelog,
                iconName = it.iconName,
                iconLink = it.iconLink,
                padding = if (index == iconInfo.size - 1) 0.dp else 16.dp
            )
        }
    }
}

@Composable
fun RomFileInfoSection(
    fileName: String,
    md5: String,
    fileSize: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        MessageTextView(stringResource(Res.string.filename), fileName)
        MessageTextView(stringResource(Res.string.filemd5), md5)
        MessageTextView(stringResource(Res.string.filesize), fileSize)
    }
}

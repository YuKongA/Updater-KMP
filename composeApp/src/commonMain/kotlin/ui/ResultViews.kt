package ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import copyToClipboard
import data.DataHelper
import downloadToLocal
import kotlinx.coroutines.launch
import misc.MessageUtils.Companion.showMessage
import misc.bodyFontSize
import misc.bodySmallFontSize
import org.jetbrains.compose.resources.stringResource
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
import updater.composeapp.generated.resources.filename
import updater.composeapp.generated.resources.filesize
import updater.composeapp.generated.resources.security_patch_level
import updater.composeapp.generated.resources.system_version
import updater.composeapp.generated.resources.tags

@Composable
fun InfoCardViews(
    romInfoState: MutableState<DataHelper.RomInfoData>,
    iconInfo: MutableState<List<DataHelper.IconInfoData>>,
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
            insideMargin = PaddingValues(16.dp)
        ) {
            Text(
                text = romInfoState.value.type.uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            BaseMessageView(
                romInfoState.value.device,
                romInfoState.value.version,
                romInfoState.value.bigVersion,
                romInfoState.value.codebase,
                romInfoState.value.branch
            )

            if (romInfoState.value.isBeta) {
                MessageTextView(
                    stringResource(Res.string.tags),
                    "Beta"
                )
            }

            if (romInfoState.value.isGov) {
                MessageTextView(
                    stringResource(Res.string.tags),
                    "Government"
                )
            }

            AnimatedVisibility(
                visible = romInfoState.value.securityPatchLevel.isNotEmpty()
                        && romInfoState.value.timestamp.isNotEmpty()
            ) {
                MetadataView(
                    romInfoState.value.securityPatchLevel,
                    romInfoState.value.timestamp
                )
            }
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

            if (romInfoState.value.official1Download.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    DownloadInfoView(
                        modifier = Modifier.weight(1f),
                        "ultimateota",
                        romInfoState.value.official1Download,
                        romInfoState.value.fileName
                    )
                    DownloadInfoView(
                        modifier = Modifier.weight(1f),
                        "superota",
                        romInfoState.value.official2Download,
                        romInfoState.value.fileName
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
                    romInfoState.value.cdn1Download,
                    romInfoState.value.fileName
                )
                DownloadInfoView(
                    modifier = Modifier.weight(1f),
                    "cdnorg",
                    romInfoState.value.cdn2Download,
                    romInfoState.value.fileName
                )
            }

            if (romInfoState.value.changelog.isNotEmpty()) {
                ChangelogView(
                    iconInfo,
                    romInfoState.value.changelog
                )
            }

            if (romInfoState.value.gentleNotice.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = stringResource(Res.string.attention),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = romInfoState.value.gentleNotice,
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
    securityPatchLevel: String,
    buildTime: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
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
    iconInfo: MutableState<List<DataHelper.IconInfoData>>,
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
package ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seiko.imageloader.rememberImagePainter
import data.DataHelper
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import platform.copyToClipboard
import platform.downloadToLocal
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.squircle.squircleBackground
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import updater.shared.generated.resources.Res
import updater.shared.generated.resources.android_version
import updater.shared.generated.resources.attention
import updater.shared.generated.resources.big_version
import updater.shared.generated.resources.branch
import updater.shared.generated.resources.build_time
import updater.shared.generated.resources.changelog
import updater.shared.generated.resources.code_name
import updater.shared.generated.resources.download
import updater.shared.generated.resources.filemd5
import updater.shared.generated.resources.filename
import updater.shared.generated.resources.filesize
import updater.shared.generated.resources.fingerprint
import updater.shared.generated.resources.sdk_level
import updater.shared.generated.resources.security_patch_level
import updater.shared.generated.resources.system_version
import updater.shared.generated.resources.tags
import updater.shared.generated.resources.xms_current_version
import updater.shared.generated.resources.xms_package_count
import updater.shared.generated.resources.xms_package_name
import updater.shared.generated.resources.xms_packages
import updater.shared.generated.resources.xms_target_version
import updater.shared.generated.resources.xms_update
import utils.LinkUtils

private val CardCornerRadius = 16.dp
private val CardInsideMargin = 16.dp

private class CardSegment(
    val key: String,
    val insideHorizontal: Dp = CardInsideMargin,
    val content: @Composable ColumnScope.() -> Unit,
)

private fun LazyListScope.segmentedCardItems(
    keyPrefix: String,
    segments: List<CardSegment>,
    outerHorizontalPadding: Dp,
    bottomSpacing: Dp,
) {
    if (segments.isEmpty()) return
    val lastIndex = segments.lastIndex
    segments.forEachIndexed { index, segment ->
        item(key = "$keyPrefix:${segment.key}") {
            SegmentContainer(
                modifier = Modifier.animateItem(placementSpec = null),
                isFirst = index == 0,
                isLast = index == lastIndex,
                outerHorizontalPadding = outerHorizontalPadding,
                bottomSpacing = bottomSpacing,
                insideHorizontal = segment.insideHorizontal,
                content = segment.content,
            )
        }
    }
}

@Composable
private fun SegmentContainer(
    modifier: Modifier,
    isFirst: Boolean,
    isLast: Boolean,
    outerHorizontalPadding: Dp,
    bottomSpacing: Dp,
    insideHorizontal: Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardColor = MiuixTheme.colorScheme.surfaceContainer
    val contentColor = MiuixTheme.colorScheme.onSurfaceContainer
    val topCorner = if (isFirst) CardCornerRadius else 0.dp
    val bottomCorner = if (isLast) CardCornerRadius else 0.dp
    val backgroundModifier = if (topCorner == 0.dp && bottomCorner == 0.dp) {
        Modifier.background(cardColor)
    } else {
        Modifier.squircleBackground(cardColor, topCorner, topCorner, bottomCorner, bottomCorner)
    }
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    start = outerHorizontalPadding,
                    end = outerHorizontalPadding,
                    bottom = if (isLast) bottomSpacing else 0.dp,
                )
                .then(backgroundModifier)
                .padding(
                    start = insideHorizontal,
                    end = insideHorizontal,
                    top = if (isFirst) CardInsideMargin else 0.dp,
                    bottom = if (isLast) CardInsideMargin else 0.dp,
                ),
            content = content,
        )
    }
}

fun LazyListScope.infoCardItems(
    keyPrefix: String,
    romInfo: DataHelper.RomInfoData,
    iconInfo: List<DataHelper.IconInfoData>,
    imageInfo: List<DataHelper.ImageInfoData>,
    outerHorizontalPadding: Dp,
    bottomSpacing: Dp,
    onCopySuccess: () -> Unit,
    onDownloadStart: () -> Unit,
) {
    if (romInfo.fileName.isEmpty()) return
    val segments = buildList {
        add(CardSegment("header") { InfoHeaderContent(romInfo, onCopySuccess, onDownloadStart) })
        if (romInfo.changelog.isNotEmpty()) {
            addChangelogSegments(iconInfo, imageInfo, romInfo.changelog, onCopySuccess)
        }
        if (romInfo.gentleNotice.isNotEmpty()) {
            add(CardSegment("attention") { AttentionSection(romInfo.gentleNotice) })
        }
    }
    segmentedCardItems(keyPrefix, segments, outerHorizontalPadding, bottomSpacing)
}

@Composable
private fun InfoHeaderContent(
    romInfo: DataHelper.RomInfoData,
    onCopySuccess: () -> Unit,
    onDownloadStart: () -> Unit,
) {
    val hasTimestamp = romInfo.timestamp.isNotEmpty() ||
            romInfo.fingerprint.isNotEmpty() ||
            romInfo.securityPatchLevel.isNotEmpty()

    Text(
        text = romInfo.type.uppercase(),
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    BaseMessageView(
        romInfo.device,
        romInfo.branch,
        romInfo.version,
        romInfo.bigVersion,
        romInfo.codebase,
    )

    if (romInfo.isBeta) {
        MessageTextView(
            stringResource(Res.string.tags),
            "Beta",
        )
    }

    if (romInfo.isGov) {
        MessageTextView(
            stringResource(Res.string.tags),
            "Government",
        )
    }

    AnimatedVisibility(
        visible = hasTimestamp
    ) {
        MetadataView(romInfo)
    }

    RomFileInfoSection(
        fileName = romInfo.fileName,
        md5 = romInfo.md5,
        fileSize = romInfo.fileSize
    )

    Text(
        text = stringResource(Res.string.download),
        color = MiuixTheme.colorScheme.onSecondaryVariant,
        fontSize = 13.sp,
    )

    if (romInfo.official1Download.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            DownloadInfoView(
                modifier = Modifier.weight(1f),
                title = "ultimateota",
                url = romInfo.official1Download,
                fileName = romInfo.fileName,
                onCopySuccess = onCopySuccess,
                onDownloadStart = onDownloadStart,
            )
            DownloadInfoView(
                modifier = Modifier.weight(1f),
                title = "superota",
                url = romInfo.official2Download,
                fileName = romInfo.fileName,
                onCopySuccess = onCopySuccess,
                onDownloadStart = onDownloadStart,
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        DownloadInfoView(
            modifier = Modifier.weight(1f),
            title = "aliyuncs",
            url = romInfo.cdn1Download,
            fileName = romInfo.fileName,
            onCopySuccess = onCopySuccess,
            onDownloadStart = onDownloadStart,
        )
        DownloadInfoView(
            modifier = Modifier.weight(1f),
            title = "cdnorg",
            url = romInfo.cdn2Download,
            fileName = romInfo.fileName,
            onCopySuccess = onCopySuccess,
            onDownloadStart = onDownloadStart,
        )
    }
}

fun LazyListScope.xmsCardItems(
    keyPrefix: String,
    xmsInfo: DataHelper.XmsInfoData,
    outerHorizontalPadding: Dp,
    bottomSpacing: Dp,
    onCopySuccess: () -> Unit,
    onDownloadStart: () -> Unit,
) {
    val hasContent = xmsInfo.hasUpdate ||
            xmsInfo.curVer.isNotEmpty() ||
            xmsInfo.changelogItems.isNotEmpty() ||
            xmsInfo.gentleNotice.isNotEmpty()
    if (!hasContent) return
    val segments = buildList {
        add(CardSegment("header") { XmsHeaderContent(xmsInfo) })
        xmsInfo.apps.forEachIndexed { index, app ->
            val appKey = app.packName.ifEmpty { app.fileName }
            add(CardSegment("app:$index:$appKey", insideHorizontal = 0.dp) {
                XmsAppRow(
                    app = app,
                    onCopySuccess = onCopySuccess,
                    onDownloadStart = onDownloadStart,
                )
            })
        }
        if (xmsInfo.changelogItems.isNotEmpty()) {
            addChangelogSegments(emptyList(), xmsInfo.changelogItems, xmsInfo.changelogText, onCopySuccess)
        }
        if (xmsInfo.gentleNotice.isNotEmpty()) {
            add(CardSegment("attention") { AttentionSection(xmsInfo.gentleNotice) })
        }
    }
    segmentedCardItems(keyPrefix, segments, outerHorizontalPadding, bottomSpacing)
}

@Composable
private fun XmsHeaderContent(xmsInfo: DataHelper.XmsInfoData) {
    Text(
        text = stringResource(Res.string.xms_update),
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp),
    )
    if (xmsInfo.curVer.isNotEmpty()) {
        MessageTextView(stringResource(Res.string.xms_current_version), xmsInfo.curVer)
    }
    if (xmsInfo.lstVer.isNotEmpty()) {
        MessageTextView(stringResource(Res.string.xms_target_version), xmsInfo.lstVer)
    }
    if (xmsInfo.pkgCnt > 0) {
        MessageTextView(stringResource(Res.string.xms_package_count), xmsInfo.pkgCnt.toString())
    }
    if (xmsInfo.apps.isNotEmpty()) {
        Text(
            text = stringResource(Res.string.xms_packages),
            color = MiuixTheme.colorScheme.onSecondaryVariant,
            fontSize = 13.sp,
        )
    }
}

private fun MutableList<CardSegment>.addChangelogSegments(
    iconInfo: List<DataHelper.IconInfoData>,
    imageInfo: List<DataHelper.ImageInfoData>,
    changelog: String,
    onCopySuccess: () -> Unit,
) {
    add(CardSegment("changelogHeader") { ChangelogHeader(changelog, onCopySuccess) })
    if (iconInfo.isNotEmpty()) {
        val lastIndex = iconInfo.lastIndex
        iconInfo.forEachIndexed { index, entry ->
            add(CardSegment("clIcon$index") {
                TextWithIcon(
                    changelog = entry.changelog,
                    iconName = entry.iconName,
                    iconLink = entry.iconLink,
                    padding = if (index == lastIndex) 0.dp else 16.dp
                )
            })
        }
    }
    if (imageInfo.isNotEmpty()) {
        val groupedEntries = imageInfo.groupBy { it.title }.entries.toList()
        val lastIndex = groupedEntries.lastIndex
        groupedEntries.forEachIndexed { index, entry ->
            val title = entry.key
            val lines = entry.value
            val hasSpacer = index < lastIndex
            add(CardSegment("clImg$index") {
                TextWithImage(
                    title = title,
                    lines = lines,
                )
                if (hasSpacer) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            })
        }
    }
}

@Composable
private fun ChangelogHeader(
    changelog: String,
    onCopySuccess: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
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
                onCopySuccess()
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        ) {
            Icon(
                imageVector = MiuixIcons.Copy,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AttentionSection(notice: String) {
    Text(
        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp),
        text = stringResource(Res.string.attention),
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
    )
    SelectionContainer {
        val gentleLines = remember(notice) { notice.lines() }
        val textColor = MiuixTheme.colorScheme.onSecondaryVariant

        Column {
            gentleLines.forEachIndexed { idx, line ->
                if (line.isNotBlank()) {
                    Text(
                        text = line,
                        color = textColor,
                        fontSize = 14.5.sp,
                        modifier = Modifier.padding(vertical = if (idx == 0) 6.dp else 4.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun XmsAppRow(
    app: DataHelper.XmsAppInfo,
    onCopySuccess: () -> Unit,
    onDownloadStart: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f, label = "xmsRowChevron")
    val hapticFeedback = LocalHapticFeedback.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val displayTitle = app.name.ifEmpty { app.packName }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                if (displayTitle.isNotEmpty()) {
                    Text(
                        text = displayTitle,
                        fontSize = MiuixTheme.textStyles.headline1.fontSize,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                }
                if (app.versionCode.isNotEmpty()) {
                    Text(
                        text = "v${app.versionCode}",
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
            app.downloadUrls.firstOrNull()?.let { url ->
                IconButton(
                    onClick = {
                        coroutineScope.launch { clipboard.copyToClipboard(url) }
                        onCopySuccess()
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    }
                ) {
                    Icon(
                        imageVector = MiuixIcons.Copy,
                        contentDescription = "Copy",
                        tint = MiuixTheme.colorScheme.onSurface,
                    )
                }
                IconButton(
                    onClick = {
                        downloadToLocal(url, app.fileName)
                        onDownloadStart()
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    }
                ) {
                    Icon(
                        imageVector = MiuixIcons.Download,
                        contentDescription = "Download",
                        tint = MiuixTheme.colorScheme.onSurface,
                    )
                }
            }
            Icon(
                imageVector = MiuixIcons.Basic.ArrowRight,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .rotate(rotation),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
                if (app.packName.isNotEmpty()) {
                    MessageTextView(stringResource(Res.string.xms_package_name), app.packName)
                }
                if (app.fileName.isNotEmpty()) {
                    MessageTextView(stringResource(Res.string.filename), app.fileName)
                }
                if (app.fileSize.isNotEmpty()) {
                    MessageTextView(stringResource(Res.string.filesize), app.fileSize)
                }
                if (app.md5.isNotEmpty()) {
                    MessageTextView(stringResource(Res.string.filemd5), app.md5)
                }
            }
        }
    }
}

@Composable
fun MetadataView(romInfo: DataHelper.RomInfoData) {
    Column {
        if (romInfo.sdkLevel.isNotEmpty()) {
            MessageTextView(stringResource(Res.string.sdk_level), romInfo.sdkLevel)
        }
        if (romInfo.securityPatchLevel.isNotEmpty()) {
            MessageTextView(stringResource(Res.string.security_patch_level), romInfo.securityPatchLevel)
        }
        if (romInfo.fingerprint.isNotEmpty()) {
            MessageTextView(stringResource(Res.string.fingerprint), romInfo.fingerprint)
        }
        if (romInfo.timestamp.isNotEmpty()) {
            MessageTextView(stringResource(Res.string.build_time), romInfo.timestamp)
        }
    }
}

@Composable
fun BaseMessageView(
    device: String,
    branch: String,
    version: String,
    bigVersion: String,
    codebase: String,
) {
    MessageTextView(stringResource(Res.string.code_name), device)
    MessageTextView(stringResource(Res.string.branch), branch)
    MessageTextView(stringResource(Res.string.system_version), version)
    MessageTextView(stringResource(Res.string.big_version), bigVersion)
    MessageTextView(stringResource(Res.string.android_version), codebase)
}

@Composable
fun MessageTextView(
    title: String,
    text: String
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Text(
            text = title,
            color = MiuixTheme.colorScheme.onSecondaryVariant,
            fontSize = 13.sp,
        )
        AnimatedContent(targetState = text) {
            SelectionContainer {
                Text(
                    text = it,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DownloadInfoView(
    modifier: Modifier = Modifier,
    title: String,
    url: String,
    fileName: String,
    onCopySuccess: () -> Unit,
    onDownloadStart: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val clipboard = LocalClipboard.current

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
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
                        onCopySuccess()
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    }
                ) {
                    Icon(
                        imageVector = MiuixIcons.Copy,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = {
                        downloadToLocal(url, fileName)
                        onDownloadStart()
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    }
                ) {
                    Icon(
                        imageVector = MiuixIcons.Download,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun TextWithIcon(
    changelog: String,
    iconName: String,
    iconLink: String,
    padding: Dp
) {
    val imagePainter = rememberImagePainter(iconLink)

    AnimatedContent(targetState = changelog) {
        Column {
            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconLink.isNotEmpty()) {
                    Image(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(24.dp),
                        painter = imagePainter,
                        contentDescription = iconName,
                    )
                    Text(
                        text = iconName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (it.isNotEmpty() && it != " ") {
                    Text(
                        text = iconName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (it.isNotBlank()) {
                val lines = remember(it) { it.lines() }
                SelectionContainer {
                    Column {
                        lines.forEach { line ->
                            if (line.isNotBlank()) {
                                Text(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    text = line,
                                    color = MiuixTheme.colorScheme.onSecondaryVariant,
                                    fontSize = 14.5.sp
                                )
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(padding))
            }
        }
    }
}

@Composable
fun TextWithImage(
    title: String,
    lines: List<DataHelper.ImageInfoData>
) {
    Column {
        Text(
            modifier = Modifier.padding(vertical = 6.dp),
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        SelectionContainer {
            Column {
                lines.forEach { line ->
                    if (line.changelog.isNotEmpty()) {
                        ChangelogText(
                            text = line.changelog,
                        )
                    }

                    if (line.imageUrl != "" && line.imageWidth != null && line.imageHeight != null && line.imageHeight > 0) {
                        val aspectRatio = line.imageWidth.toFloat() / line.imageHeight.toFloat()
                        Image(
                            painter = rememberImagePainter(line.imageUrl),
                            modifier = Modifier
                                .padding(top = 4.dp, bottom = 8.dp)
                                .fillMaxWidth()
                                .aspectRatio(aspectRatio)
                                .clip(RoundedCornerShape(10.dp)),
                            alignment = Alignment.Center,
                            contentScale = ContentScale.FillWidth,
                            contentDescription = line.changelog,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RomFileInfoSection(
    fileName: String,
    md5: String,
    fileSize: String
) {
    Column {
        MessageTextView(stringResource(Res.string.filename), fileName)
        MessageTextView(stringResource(Res.string.filemd5), md5)
        MessageTextView(stringResource(Res.string.filesize), fileSize)
    }
}

@Composable
fun ChangelogText(
    text: String
) {
    val links = remember(text) { LinkUtils.extractLinks(text) }
    val uriHandler = LocalUriHandler.current
    val primary = MiuixTheme.colorScheme.primary

    val annotatedString = remember(text, links) {
        buildAnnotatedString {
            append(text)
            links.forEach { (url, range) ->
                addStyle(
                    style = SpanStyle(
                        color = primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    start = range.first,
                    end = range.last + 1
                )
                addLink(
                    url = LinkAnnotation.Url(
                        url = url,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = primary,
                                textDecoration = TextDecoration.Underline
                            )
                        ),
                        linkInteractionListener = {
                            uriHandler.openUri(url)
                        }
                    ),
                    start = range.first,
                    end = range.last + 1
                )
            }
        }
    }

    Text(
        modifier = Modifier.padding(vertical = 6.dp),
        text = annotatedString,
        style = TextStyle(
            color = MiuixTheme.colorScheme.onSecondaryVariant,
            fontSize = 14.5.sp
        )
    )
}

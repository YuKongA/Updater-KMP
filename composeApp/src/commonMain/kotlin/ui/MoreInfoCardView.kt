package ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import copyToClipboard
import data.DataHelper
import misc.MessageUtils.Companion.showMessage
import misc.bodyFontSize
import org.jetbrains.compose.resources.stringResource
import ui.components.TextWithIcon
import updaterkmp.composeapp.generated.resources.Res
import updaterkmp.composeapp.generated.resources.changelog
import updaterkmp.composeapp.generated.resources.copy_button
import updaterkmp.composeapp.generated.resources.copy_successful
import updaterkmp.composeapp.generated.resources.filename
import updaterkmp.composeapp.generated.resources.filesize

@Composable
fun MoreInfoCardViews(
    romInfoState: MutableState<DataHelper.RomInfoData>,
    iconInfo: MutableState<List<DataHelper.IconInfoData>>
) {
    val isVisible = remember { mutableStateOf(false) }
    isVisible.value = romInfoState.value.fileName.isNotEmpty()

    AnimatedVisibility(
        visible = isVisible.value,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Card(
            colors = CardDefaults.cardColors(
                contentColor = MaterialTheme.colorScheme.onBackground,
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                MoreTextView(stringResource(Res.string.filename), romInfoState.value.fileName)
                MoreTextView(stringResource(Res.string.filesize), romInfoState.value.fileSize)
                ChangelogView(iconInfo, romInfoState.value.changelog)
            }
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

    Text(
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
        Text(
            text = it,
            modifier = Modifier.padding(bottom = 8.dp),
            fontSize = bodyFontSize
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

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(Res.string.changelog),
                fontSize = bodyFontSize,
                fontWeight = FontWeight.SemiBold
            )
            Text(
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
                color = ButtonDefaults.textButtonColors().contentColor
            )
        }
        iconInfo.value.forEach {
            TextWithIcon(
                changelog = it.changelog,
                iconName = it.iconName,
                iconLink = it.iconLink
            )
        }
    }
}
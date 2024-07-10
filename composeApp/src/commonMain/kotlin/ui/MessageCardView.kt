package ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import data.DataHelper
import misc.bodyFontSize
import org.jetbrains.compose.resources.stringResource
import updaterkmp.composeapp.generated.resources.Res
import updaterkmp.composeapp.generated.resources.android_version
import updaterkmp.composeapp.generated.resources.big_version
import updaterkmp.composeapp.generated.resources.branch
import updaterkmp.composeapp.generated.resources.code_name
import updaterkmp.composeapp.generated.resources.system_version

@Composable
fun MessageCardViews(
    romInfoState: MutableState<DataHelper.RomInfoData>
) {
    val isVisible = remember { mutableStateOf(false) }
    isVisible.value = romInfoState.value.type.isNotEmpty()

    AnimatedVisibility(
        visible = isVisible.value,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Column {
            AnimatedContent(
                targetState = romInfoState.value.type.uppercase(),
                transitionSpec = {
                    fadeIn(animationSpec = tween(1500)) togetherWith fadeOut(animationSpec = tween(300))
                }
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                    fontSize = MaterialTheme.typography.titleSmall.fontSize
                )
            }
            Card(
                colors = CardDefaults.cardColors(
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                MessageCardView(
                    romInfoState.value.device,
                    romInfoState.value.version,
                    romInfoState.value.bigVersion,
                    romInfoState.value.codebase,
                    romInfoState.value.branch
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
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        MessageTextView(stringResource(Res.string.code_name), device)
        MessageTextView(stringResource(Res.string.system_version), version)
        MessageTextView(stringResource(Res.string.big_version), bigVersion)
        MessageTextView(stringResource(Res.string.android_version), codebase)
        MessageTextView(stringResource(Res.string.branch), branch, 0.dp)
    }
}

@Composable
fun MessageTextView(
    title: String,
    text: String,
    bottomPadding: Dp = 8.dp
) {
    val scrollState = rememberScrollState()
    val content = remember { mutableStateOf("") }
    content.value = text

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = bottomPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                modifier = Modifier.horizontalScroll(scrollState),
                fontSize = bodyFontSize,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}
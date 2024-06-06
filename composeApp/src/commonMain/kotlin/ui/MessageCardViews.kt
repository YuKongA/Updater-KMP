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
import org.jetbrains.compose.resources.stringResource
import updaterkmm.composeapp.generated.resources.Res
import updaterkmm.composeapp.generated.resources.android_version
import updaterkmm.composeapp.generated.resources.big_version
import updaterkmm.composeapp.generated.resources.branch
import updaterkmm.composeapp.generated.resources.code_name
import updaterkmm.composeapp.generated.resources.system_version

@Composable
fun MessageCardViews(
    type: MutableState<String>,
    codeName: MutableState<String>,
    systemVersion: MutableState<String>,
    xiaomiVersion: MutableState<String>,
    androidVersion: MutableState<String>,
    branchVersion: MutableState<String>
) {
    val isVisible = remember { mutableStateOf(false) }
    isVisible.value = codeName.value.isNotEmpty()

    AnimatedVisibility(
        visible = isVisible.value,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Column {
            AnimatedContent(
                targetState = type.value.uppercase(),
                transitionSpec = {
                    fadeIn(animationSpec = tween(1500)) togetherWith fadeOut(animationSpec = tween(300))
                }
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                    fontSize = MaterialTheme.typography.titleSmall.fontSize
                )
            }
            Card(
                colors = CardDefaults.cardColors(
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                MessageCardView(
                    codeName.value,
                    systemVersion.value,
                    xiaomiVersion.value,
                    androidVersion.value,
                    branchVersion.value
                )
            }
        }
    }
}

@Composable
fun MessageCardView(
    codeName: String,
    systemVersion: String,
    xiaomiVersion: String,
    androidVersion: String,
    branchVersion: String
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        MessageTextView(stringResource(Res.string.code_name), codeName)
        MessageTextView(stringResource(Res.string.system_version), systemVersion)
        MessageTextView(stringResource(Res.string.big_version), xiaomiVersion)
        MessageTextView(stringResource(Res.string.android_version), androidVersion)
        MessageTextView(stringResource(Res.string.branch), branchVersion, 0.dp)
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
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
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
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}
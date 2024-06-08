package ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import copyToClipboard
import misc.SnackbarUtil.Companion.showSnackbar
import org.jetbrains.compose.resources.stringResource
import updaterkmm.composeapp.generated.resources.Res
import updaterkmm.composeapp.generated.resources.changelog
import updaterkmm.composeapp.generated.resources.copy_successful
import updaterkmm.composeapp.generated.resources.filename
import updaterkmm.composeapp.generated.resources.filesize

@Composable
fun MoreInfoCardViews(
    fileName: MutableState<String>,
    fileSize: MutableState<String>,
    changeLog: MutableState<String>
) {
    val isVisible = remember { mutableStateOf(false) }
    isVisible.value = fileName.value.isNotEmpty()

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
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                MoreTextView(stringResource(Res.string.filename), fileName.value)
                MoreTextView(stringResource(Res.string.filesize), fileSize.value)
                MoreTextView(stringResource(Res.string.changelog), changeLog.value, true, 0.dp)
            }
        }
    }
}

@Composable
fun MoreTextView(
    title: String,
    text: String,
    copy: Boolean = false,
    bottomPadding: Dp = 8.dp
) {
    val content = remember { mutableStateOf("") }
    content.value = text

    val messageCopySuccessful = stringResource(Res.string.copy_successful)

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
            modifier = Modifier
                .padding(bottom = bottomPadding)
                .clickable(
                    enabled = copy,
                    onClick = {
                        copyToClipboard(it)
                        showSnackbar(messageCopySuccessful)
                    }
                ),
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            fontFamily = FontFamily.Monospace
        )
    }
}
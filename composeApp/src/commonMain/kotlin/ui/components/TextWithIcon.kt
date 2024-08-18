package ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seiko.imageloader.rememberImagePainter
import misc.bodyFontSize
import top.yukonga.miuix.kmp.basic.MiuixText

@Composable
fun TextWithIcon(
    changelog: String,
    iconName: String,
    iconLink: String
) {
    val imagePainter = rememberImagePainter(iconLink)
    val content = remember { mutableStateOf("") }
    content.value = changelog

    AnimatedContent(
        targetState = content.value,
        transitionSpec = {
            fadeIn(animationSpec = tween(1500)) togetherWith fadeOut(animationSpec = tween(300))
        }
    ) {
        Column {
            Spacer(modifier = Modifier.size(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(18.dp),
                    painter = imagePainter,
                    contentDescription = null,
                )
                MiuixText(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = iconName,
                    fontSize = bodyFontSize,
                    fontWeight = FontWeight.Medium
                )
            }
            MiuixText(
                text = it,
                fontSize = bodyFontSize
            )
        }
    }
}
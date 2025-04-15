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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seiko.imageloader.rememberImagePainter
import misc.bodyFontSize
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun TextWithIcon(
    changelog: String,
    iconName: String,
    iconLink: String,
    padding: Dp
) {
    val imagePainter = rememberImagePainter(iconLink)

    AnimatedContent(
        targetState = changelog,
        transitionSpec = {
            fadeIn(animationSpec = tween(1500)) togetherWith fadeOut(animationSpec = tween(300))
        }
    ) { content ->
        Column {
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconLink.isNotEmpty()) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = imagePainter,
                        contentDescription = iconName,
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        text = iconName,
                        fontSize = bodyFontSize,
                    )
                } else if (content.isNotEmpty() && content != " ") {
                    Text(
                        text = iconName,
                        fontSize = bodyFontSize,
                    )
                }
            }
            if (content.isNotEmpty() && content != " ") {
                Text(
                    text = content,
                    color = MiuixTheme.colorScheme.onSecondaryVariant,
                    fontSize = 14.5.sp
                )
                Spacer(modifier = Modifier.height(padding))
            }
        }
    }
}
package ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seiko.imageloader.rememberImagePainter
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.G2RoundedCornerShape

@Composable
fun TextWithImage(
    changelog: String,
    imageName: String,
    imageLink: String,
    padding: Dp
) {
    AnimatedContent(targetState = changelog) {
        Column {
            if (it.isNotEmpty() && it != " ") {
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = imageName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (imageLink.isNotEmpty()) {
                    Image(
                        painter = rememberImagePainter(imageLink),
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clip(G2RoundedCornerShape(10.dp))
                            .width(512.dp),
                        alignment = Alignment.TopStart,
                        contentScale = ContentScale.Fit,
                        contentDescription = imageName,
                    )
                }
                SelectionContainer {
                    Text(
                        text = it,
                        color = MiuixTheme.colorScheme.onSecondaryVariant,
                        fontSize = 14.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(padding))
            }
        }
    }
}

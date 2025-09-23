package ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seiko.imageloader.rememberImagePainter
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

    AnimatedContent(targetState = changelog) {
        Column {
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
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
            if (it.isNotEmpty() && it != " ") {
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

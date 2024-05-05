package top.yukonga.updater.kmm

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import top.yukonga.updater.kmm.theme.UpdaterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            UpdaterTheme {
                App()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
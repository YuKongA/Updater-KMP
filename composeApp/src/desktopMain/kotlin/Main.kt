import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.jetbrains.compose.resources.painterResource
import updaterkmm.composeapp.generated.resources.Icon
import updaterkmm.composeapp.generated.resources.Res

fun main() = application {
    val state = rememberWindowState(
        size = DpSize(420.dp, 820.dp),
        position = WindowPosition.Aligned(Alignment.Center)
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "UpdaterKMM",
        state = state,
        icon = painterResource(Res.drawable.Icon),
    ) {
        App()
    }
}
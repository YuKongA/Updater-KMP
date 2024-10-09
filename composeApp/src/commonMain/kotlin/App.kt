import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import data.DataHelper
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import misc.MessageUtils.Companion.Snackbar
import misc.json
import misc.updateRomInfo
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Box
import top.yukonga.miuix.kmp.basic.LazyColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.getWindowSize
import ui.AboutDialog
import ui.InfoCardViews
import ui.LoginCardView
import ui.TextFieldViews
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.app_name

@Composable
fun App() {
    val deviceName = remember { mutableStateOf(perfGet("deviceName") ?: "") }
    val codeName = remember { mutableStateOf(perfGet("codeName") ?: "") }
    val deviceRegion = remember { mutableStateOf(perfGet("deviceRegion") ?: "") }
    val androidVersion = remember { mutableStateOf(perfGet("androidVersion") ?: "") }
    val systemVersion = remember { mutableStateOf(perfGet("systemVersion") ?: "") }

    val loginData = perfGet("loginInfo")?.let { json.decodeFromString<DataHelper.LoginData>(it) }
    val isLogin = remember { mutableStateOf(loginData?.authResult?.toInt() ?: 0) }

    val curRomInfo = remember { mutableStateOf(DataHelper.RomInfoData()) }
    val incRomInfo = remember { mutableStateOf(DataHelper.RomInfoData()) }

    val curIconInfo: MutableState<List<DataHelper.IconInfoData>> = remember { mutableStateOf(listOf()) }
    val incIconInfo: MutableState<List<DataHelper.IconInfoData>> = remember { mutableStateOf(listOf()) }

    val updateRomInfo = remember { mutableStateOf(0) }
    updateRomInfo(
        deviceName, codeName, deviceRegion, androidVersion, systemVersion, loginData,
        isLogin, curRomInfo, incRomInfo, curIconInfo, incIconInfo, updateRomInfo
    )

    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())

    val hazeState = remember { HazeState() }

    AppTheme {
        Surface {
            Scaffold(
                modifier = Modifier
                    .imePadding()
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    TopAppBar(
                        modifier = Modifier.hazeChild(
                            state = hazeState,
                            style = HazeStyle(
                                backgroundColor = MiuixTheme.colorScheme.background,
                                tint = HazeTint.Color(MiuixTheme.colorScheme.background.copy(0.8f)),
                                blurRadius = 20.dp,
                                noiseFactor = 0f
                            )
                        ),
                        color = Color.Transparent,
                        title = stringResource(Res.string.app_name),
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            AboutDialog()
                        }
                    )
                }
            ) {
                BoxWithConstraints(
                    modifier = Modifier.haze(state = hazeState)
                ) {
                    if (maxWidth < 600.dp) {
                        LazyColumn(
                            modifier = Modifier
                                .height(getWindowSize().height.dp)
                                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
                            contentPadding = it,
                            enableOverScroll = true,
                            topAppBarScrollBehavior = scrollBehavior
                        ) {
                            item {
                                Box(
                                    modifier = Modifier.navigationBarsPadding()
                                ) {
                                    Column {
                                        LoginCardView(isLogin)
                                        TextFieldViews(deviceName, codeName, deviceRegion, androidVersion, systemVersion, updateRomInfo)
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        ) {
                                            InfoCardViews(curRomInfo, curIconInfo)
                                            InfoCardViews(incRomInfo, incIconInfo)
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .height(getWindowSize().height.dp)
                                    .weight(0.88f),
                                contentPadding = it,
                                topAppBarScrollBehavior = scrollBehavior
                            ) {
                                item {
                                    Column(
                                        modifier = Modifier.navigationBarsPadding()
                                    ) {
                                        LoginCardView(isLogin)
                                        TextFieldViews(deviceName, codeName, deviceRegion, androidVersion, systemVersion, updateRomInfo)
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .weight(1f),
                                contentPadding = it,
                                topAppBarScrollBehavior = scrollBehavior
                            ) {
                                item {
                                    Column(
                                        modifier = Modifier.navigationBarsPadding()
                                    ) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        InfoCardViews(curRomInfo, curIconInfo)
                                        InfoCardViews(incRomInfo, incIconInfo)
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                Snackbar()
            }
        }
    }
}
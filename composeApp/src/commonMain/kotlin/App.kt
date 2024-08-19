import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import data.DataHelper
import misc.MessageUtils.Companion.Snackbar
import misc.json
import misc.updateRomInfo
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.MiuixScrollBehavior
import top.yukonga.miuix.kmp.MiuixTopAppBar
import top.yukonga.miuix.kmp.basic.MiuixBox
import top.yukonga.miuix.kmp.basic.MiuixLazyColumn
import top.yukonga.miuix.kmp.basic.MiuixScaffold
import top.yukonga.miuix.kmp.basic.MiuixSurface
import top.yukonga.miuix.kmp.rememberMiuixTopAppBarState
import top.yukonga.miuix.kmp.theme.MiuixTheme
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

    val scrollBehavior = MiuixScrollBehavior(rememberMiuixTopAppBarState())

    AppTheme {
        MiuixSurface {
            MiuixScaffold(
                containerColor = MiuixTheme.colorScheme.secondaryBackground,
                modifier = Modifier
                    .imePadding()
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    MiuixTopAppBar(
                        title = stringResource(Res.string.app_name),
                        color = Color.Transparent,
                        scrollBehavior = scrollBehavior
                    )
                }
            ) {
                MiuixBox {
                    MiuixLazyColumn(
                        modifier = Modifier.height(getWindowSize().height.dp),
                        contentPadding = it,
                        enableOverScroll = true,
                        topAppBarScrollBehavior = scrollBehavior
                    ) {
                        item {
                            BoxWithConstraints(
                                modifier = Modifier.navigationBarsPadding()
                            ) {
                                if (maxWidth < 768.dp) {
                                    Column {
                                        LoginCardView(isLogin)
                                        TextFieldViews(deviceName, codeName, deviceRegion, androidVersion, systemVersion, updateRomInfo)
                                        Column(
                                            modifier = Modifier.padding(horizontal = 20.dp)
                                        ) {
                                            InfoCardViews(curRomInfo, curIconInfo)
                                            InfoCardViews(incRomInfo, incIconInfo)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                } else {
                                    Column {
                                        Row {
                                            Column(modifier = Modifier.weight(0.8f)) {
                                                LoginCardView(isLogin)
                                                TextFieldViews(deviceName, codeName, deviceRegion, androidVersion, systemVersion, updateRomInfo)
                                            }
                                            Column(modifier = Modifier.weight(1.0f).padding(end = 20.dp)) {
                                                InfoCardViews(curRomInfo, curIconInfo)
                                                InfoCardViews(incRomInfo, incIconInfo)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
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
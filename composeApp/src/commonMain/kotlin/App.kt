import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import data.DataHelper
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import misc.MessageUtils.Companion.Snackbar
import misc.json
import misc.updateRomInfo
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.extra.DropdownImpl
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.ImmersionMore
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical
import ui.AboutDialog
import ui.BasicViews
import ui.InfoCardViews
import ui.LoginCardView
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.app_name
import updater.composeapp.generated.resources.clear_search_history

@Composable
fun App(
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    AppTheme(
        isDarkTheme = isDarkTheme
    ) {
        val deviceName = remember { mutableStateOf(perfGet("deviceName") ?: "") }
        val codeName = remember { mutableStateOf(perfGet("codeName") ?: "") }
        val deviceRegion = remember { mutableStateOf(perfGet("deviceRegion") ?: "CN") }
        val androidVersion = remember { mutableStateOf(perfGet("androidVersion") ?: "15.0") }
        val systemVersion = remember { mutableStateOf(perfGet("systemVersion") ?: "") }

        val loginData = perfGet("loginInfo")?.let { json.decodeFromString<DataHelper.LoginData>(it) }
        val isLogin = remember { mutableStateOf(loginData?.authResult?.toInt() ?: 0) }

        val curRomInfo = remember { mutableStateOf(DataHelper.RomInfoData()) }
        val incRomInfo = remember { mutableStateOf(DataHelper.RomInfoData()) }

        val curIconInfo: MutableState<List<DataHelper.IconInfoData>> = remember { mutableStateOf(listOf()) }
        val incIconInfo: MutableState<List<DataHelper.IconInfoData>> = remember { mutableStateOf(listOf()) }

        val updateRomInfo = remember { mutableStateOf(0) }
        val searchKeywords = remember { mutableStateOf(json.decodeFromString<List<String>>(perfGet("searchKeywords") ?: "[]")) }
        val searchKeywordsSelected = remember { mutableStateOf(0) }
        LaunchedEffect(updateRomInfo.value) { searchKeywordsSelected.value = 0 }

        updateRomInfo(
            deviceName, codeName, deviceRegion, androidVersion, systemVersion, loginData,
            isLogin, curRomInfo, incRomInfo, curIconInfo, incIconInfo, updateRomInfo, searchKeywords
        )

        val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())

        val focusManager = LocalFocusManager.current

        val hazeState = remember { HazeState() }

        val hazeStyle = HazeStyle(
            backgroundColor = MiuixTheme.colorScheme.background,
            tint = HazeTint(
                MiuixTheme.colorScheme.background.copy(
                    if (scrollBehavior.state.collapsedFraction <= 0f) 1f
                    else lerp(1f, 0.67f, (scrollBehavior.state.collapsedFraction))
                )
            )
        )

        var isTopPopupExpanded by remember { mutableStateOf(false) }
        val showMenuPopup = remember { mutableStateOf(false) }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = {
                TopAppBar(
                    color = Color.Transparent,
                    title = stringResource(Res.string.app_name),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        AboutDialog()
                    },
                    actions = {
                        if (isTopPopupExpanded) {
                            ListPopup(
                                show = showMenuPopup,
                                popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
                                alignment = PopupPositionProvider.Align.TopRight,
                                onDismissRequest = {
                                    showMenuPopup.value = false
                                    isTopPopupExpanded = false
                                }
                            ) {
                                ListPopupColumn {
                                    DropdownImpl(
                                        text = stringResource(Res.string.clear_search_history),
                                        optionSize = 1,
                                        isSelected = false,
                                        onSelectedIndexChange = {
                                            showMenuPopup.value = false
                                            isTopPopupExpanded = false
                                            searchKeywords.value = listOf()
                                            perfRemove("searchKeywords")
                                        },
                                        index = 0
                                    )
                                }
                            }
                            showMenuPopup.value = true
                        }
                        if (searchKeywords.value != emptyList<String>()) {
                            IconButton(
                                modifier = Modifier.padding(end = 20.dp).size(40.dp),
                                onClick = {
                                    isTopPopupExpanded = true
                                    focusManager.clearFocus()
                                },
                                holdDownState = isTopPopupExpanded
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.Useful.ImmersionMore,
                                    tint = MiuixTheme.colorScheme.onBackground,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .hazeEffect(hazeState) {
                            style = hazeStyle
                            blurRadius = 25.dp
                            noiseFactor = 0f
                        }
                )
            }
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .hazeSource(state = hazeState)
            ) {
                if (maxWidth < 840.dp) {
                    LazyColumn(
                        modifier = Modifier
                            .height(getWindowSize().height.dp)
                            .overScrollVertical()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
                        contentPadding = it,
                        overscrollEffect = null
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .navigationBarsPadding()
                            ) {
                                Column {
                                    LoginCardView(isLogin)
                                    BasicViews(
                                        deviceName, codeName, androidVersion, deviceRegion, systemVersion,
                                        updateRomInfo, searchKeywords, searchKeywordsSelected
                                    )
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp)
                                    ) {
                                        InfoCardViews(curRomInfo, curIconInfo)
                                        InfoCardViews(incRomInfo, incIconInfo)
                                    }
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
                                .overScrollVertical()
                                .nestedScroll(scrollBehavior.nestedScrollConnection)
                                .weight(0.88f),
                            contentPadding = it,
                            overscrollEffect = null
                        ) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .navigationBarsPadding()
                                ) {
                                    LoginCardView(isLogin)
                                    BasicViews(
                                        deviceName, codeName, androidVersion, deviceRegion, systemVersion,
                                        updateRomInfo, searchKeywords, searchKeywordsSelected
                                    )
                                }
                            }
                        }
                        LazyColumn(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .overScrollVertical()
                                .nestedScroll(scrollBehavior.nestedScrollConnection)
                                .weight(1f),
                            contentPadding = it,
                            overscrollEffect = null
                        ) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .navigationBarsPadding()
                                        .padding(top = 12.dp)
                                ) {
                                    InfoCardViews(curRomInfo, curIconInfo)
                                    InfoCardViews(incRomInfo, incIconInfo)
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
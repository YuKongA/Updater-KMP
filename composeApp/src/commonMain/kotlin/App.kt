import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import data.DataHelper
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import misc.MessageUtils.Companion.Snackbar
import misc.UpdateRomInfo
import misc.json
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import platform.prefGet
import platform.prefRemove
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.VerticalDivider
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.extra.DropdownImpl
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.Platform
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.platform
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import ui.AboutDialog
import ui.BasicViews
import ui.InfoCardViews
import ui.LoginCardView
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.app_name
import updater.composeapp.generated.resources.clear_search_history
import updater.composeapp.generated.resources.icon

@Composable
fun App(
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    AppTheme(
        isDarkTheme = isDarkTheme
    ) {
        val deviceName = remember { mutableStateOf(prefGet("deviceName") ?: "") }
        val codeName = remember { mutableStateOf(prefGet("codeName") ?: "") }
        val deviceRegion = remember { mutableStateOf(prefGet("deviceRegion") ?: "Default (CN)") }
        val deviceCarrier = remember { mutableStateOf(prefGet("deviceCarrier") ?: "Default (Xiaomi)") }
        val androidVersion = remember { mutableStateOf(prefGet("androidVersion") ?: "15.0") }
        val systemVersion = remember { mutableStateOf(prefGet("systemVersion") ?: "") }

        val loginData = prefGet("loginInfo")?.let { json.decodeFromString<DataHelper.LoginData>(it) }
        val isLogin = remember { mutableStateOf(loginData?.authResult?.toInt() ?: 0) }

        val curRomInfo = remember { mutableStateOf(DataHelper.RomInfoData()) }
        val incRomInfo = remember { mutableStateOf(DataHelper.RomInfoData()) }

        val curIconInfo: MutableState<List<DataHelper.IconInfoData>> = remember { mutableStateOf(listOf()) }
        val incIconInfo: MutableState<List<DataHelper.IconInfoData>> = remember { mutableStateOf(listOf()) }

        val updateRomInfoState = remember { mutableStateOf(0) }
        val searchKeywords = remember { mutableStateOf(json.decodeFromString<List<String>>(prefGet("searchKeywords") ?: "[]")) }
        val searchKeywordsSelected = remember { mutableStateOf(0) }

        UpdateRomInfo(
            deviceName, codeName, deviceRegion, deviceCarrier, androidVersion, systemVersion, loginData,
            isLogin, curRomInfo, incRomInfo, curIconInfo, incIconInfo, updateRomInfoState, searchKeywords, searchKeywordsSelected
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

        val showMenuPopup = remember { mutableStateOf(false) }

        val onClearSearchHistory = {
            searchKeywords.value = listOf()
            prefRemove("searchKeywords")
        }

        BoxWithConstraints(
            modifier = Modifier
                .scrollEndHaptic()
        ) {
            if (maxWidth < 768.dp) {
                PortraitAppView(
                    hazeState = hazeState,
                    hazeStyle = hazeStyle,
                    scrollBehavior = scrollBehavior,
                    focusManager = focusManager,
                    showMenuPopup = showMenuPopup,
                    searchKeywords = searchKeywords,
                    onClearSearchHistory = onClearSearchHistory,
                    isLogin = isLogin,
                    deviceName = deviceName,
                    codeName = codeName,
                    androidVersion = androidVersion,
                    deviceRegion = deviceRegion,
                    deviceCarrier = deviceCarrier,
                    systemVersion = systemVersion,
                    updateRomInfoState = updateRomInfoState,
                    searchKeywordsSelected = searchKeywordsSelected,
                    curRomInfo = curRomInfo,
                    curIconInfo = curIconInfo,
                    incRomInfo = incRomInfo,
                    incIconInfo = incIconInfo
                )
            } else {
                LandscapeAppView(
                    scrollBehavior = scrollBehavior,
                    focusManager = focusManager,
                    showMenuPopup = showMenuPopup,
                    searchKeywords = searchKeywords,
                    onClearSearchHistory = onClearSearchHistory,
                    isLogin = isLogin,
                    deviceName = deviceName,
                    codeName = codeName,
                    androidVersion = androidVersion,
                    deviceRegion = deviceRegion,
                    deviceCarrier = deviceCarrier,
                    systemVersion = systemVersion,
                    updateRomInfoState = updateRomInfoState,
                    searchKeywordsSelected = searchKeywordsSelected,
                    curRomInfo = curRomInfo,
                    curIconInfo = curIconInfo,
                    incRomInfo = incRomInfo,
                    incIconInfo = incIconInfo
                )
            }
        }
        Snackbar()
    }
}

@Composable
private fun MenuActions(
    searchKeywordsState: MutableState<List<String>>,
    showMenuPopup: MutableState<Boolean>,
    focusManager: FocusManager,
    onClearSearchHistory: () -> Unit
) {
    ListPopup(
        show = showMenuPopup,
        popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
        alignment = PopupPositionProvider.Align.TopRight,
        onDismissRequest = {
            showMenuPopup.value = false
        }
    ) {
        ListPopupColumn {
            DropdownImpl(
                text = stringResource(Res.string.clear_search_history),
                optionSize = 1,
                isSelected = false,
                onSelectedIndexChange = {
                    showMenuPopup.value = false
                    onClearSearchHistory()
                },
                index = 0
            )
        }
    }

    if (searchKeywordsState.value.isNotEmpty()) {
        IconButton(
            modifier = Modifier
                .padding(end = if (platform() != Platform.IOS && platform() != Platform.Android) 10.dp else 20.dp)
                .size(40.dp),
            onClick = {
                showMenuPopup.value = true
                focusManager.clearFocus()
            },
            holdDownState = showMenuPopup.value
        ) {
            Icon(
                imageVector = MiuixIcons.Useful.Settings,
                tint = MiuixTheme.colorScheme.onBackground,
                contentDescription = "Menu"
            )
        }
    }
}

@Composable
private fun PortraitAppView(
    hazeState: HazeState,
    hazeStyle: HazeStyle,
    scrollBehavior: ScrollBehavior,
    focusManager: FocusManager,
    showMenuPopup: MutableState<Boolean>,
    searchKeywords: MutableState<List<String>>,
    onClearSearchHistory: () -> Unit,
    isLogin: MutableState<Int>,
    deviceName: MutableState<String>,
    codeName: MutableState<String>,
    androidVersion: MutableState<String>,
    deviceRegion: MutableState<String>,
    deviceCarrier: MutableState<String>,
    systemVersion: MutableState<String>,
    updateRomInfoState: MutableState<Int>,
    searchKeywordsSelected: MutableState<Int>,
    curRomInfo: MutableState<DataHelper.RomInfoData>,
    curIconInfo: MutableState<List<DataHelper.IconInfoData>>,
    incRomInfo: MutableState<DataHelper.RomInfoData>,
    incIconInfo: MutableState<List<DataHelper.IconInfoData>>
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                color = Color.Transparent,
                title = stringResource(Res.string.app_name),
                scrollBehavior = scrollBehavior,
                navigationIcon = { AboutDialog() },
                actions = {
                    MenuActions(
                        searchKeywordsState = searchKeywords,
                        showMenuPopup = showMenuPopup,
                        focusManager = focusManager,
                        onClearSearchHistory = onClearSearchHistory
                    )
                },
                modifier = Modifier
                    .hazeEffect(hazeState) {
                        style = hazeStyle
                        blurRadius = 25.dp
                        noiseFactor = 0f
                    }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .height(getWindowSize().height.dp)
                .hazeSource(state = hazeState)
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .imePadding(),
            contentPadding = paddingValues,
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
                            deviceName, codeName, androidVersion, deviceRegion, deviceCarrier, systemVersion,
                            updateRomInfoState, searchKeywords, searchKeywordsSelected
                        )
                        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                            InfoCardViews(curRomInfo, curIconInfo, updateRomInfoState)
                            InfoCardViews(incRomInfo, incIconInfo, updateRomInfoState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LandscapeAppView(
    scrollBehavior: ScrollBehavior,
    focusManager: FocusManager,
    showMenuPopup: MutableState<Boolean>,
    searchKeywords: MutableState<List<String>>,
    onClearSearchHistory: () -> Unit,
    isLogin: MutableState<Int>,
    deviceName: MutableState<String>,
    codeName: MutableState<String>,
    androidVersion: MutableState<String>,
    deviceRegion: MutableState<String>,
    deviceCarrier: MutableState<String>,
    systemVersion: MutableState<String>,
    updateRomInfoState: MutableState<Int>,
    searchKeywordsSelected: MutableState<Int>,
    curRomInfo: MutableState<DataHelper.RomInfoData>,
    curIconInfo: MutableState<List<DataHelper.IconInfoData>>,
    incRomInfo: MutableState<DataHelper.RomInfoData>,
    incIconInfo: MutableState<List<DataHelper.IconInfoData>>
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { scaffoldPaddingValues ->
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Start))
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Start))
        ) {
            Column(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight()
            ) {
                SmallTopAppBar(
                    title = stringResource(Res.string.app_name),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = { AboutDialog() },
                    actions = {
                        MenuActions(
                            searchKeywordsState = searchKeywords,
                            showMenuPopup = showMenuPopup,
                            focusManager = focusManager,
                            onClearSearchHistory = onClearSearchHistory
                        )
                    },
                    defaultWindowInsetsPadding = false,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Start))
                        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Start))
                )
                HorizontalDivider(
                    thickness = 1.dp
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .overScrollVertical()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .imePadding(),
                    overscrollEffect = null
                ) {
                    item {
                        LoginCardView(isLogin)
                        BasicViews(
                            deviceName, codeName, androidVersion, deviceRegion, deviceCarrier, systemVersion,
                            updateRomInfoState, searchKeywords, searchKeywordsSelected
                        )
                        Spacer(
                            Modifier.height(
                                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                            )
                        )
                    }
                }
            }
            VerticalDivider(
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxHeight()
                    .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f - 0.42f)
            ) {
                if (curIconInfo.value.isEmpty()) {
                    Image(
                        painter = painterResource(Res.drawable.icon),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(128.dp),
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp)
                        .overScrollVertical(),
                    contentPadding = PaddingValues(
                        top = scaffoldPaddingValues.calculateTopPadding(),
                        bottom = scaffoldPaddingValues.calculateBottomPadding(),
                        end = scaffoldPaddingValues.calculateEndPadding(LocalLayoutDirection.current)
                    ),
                    overscrollEffect = null
                ) {
                    item {
                        Column(
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            InfoCardViews(curRomInfo, curIconInfo, updateRomInfoState)
                            InfoCardViews(incRomInfo, incIconInfo, updateRomInfoState)
                        }
                        Spacer(
                            Modifier.height(
                                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                            )
                        )
                    }
                }
            }
        }
    }
}

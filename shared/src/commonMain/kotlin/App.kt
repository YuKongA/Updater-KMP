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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
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
import top.yukonga.miuix.kmp.extra.SuperListPopup
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.Platform
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.platform
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import ui.AboutDialog
import ui.BasicViews
import ui.DeviceListDialog
import ui.InfoCardViews
import ui.LoginCardView
import updater.shared.generated.resources.Res
import updater.shared.generated.resources.app_name
import updater.shared.generated.resources.clear_search_history
import updater.shared.generated.resources.device_list_settings
import updater.shared.generated.resources.icon
import utils.MessageUtils.Companion.Snackbar
import viewmodel.AppUiState
import viewmodel.AppViewModel

@Composable
fun App(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    appViewModel: AppViewModel = viewModel { AppViewModel() }
) {
    AppTheme(
        isDarkTheme = isDarkTheme
    ) {
        val uiState by appViewModel.uiState.collectAsState()

        val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
        val focusManager = LocalFocusManager.current

        val hazeState = remember { HazeState() }
        val hazeStyle = HazeStyle(
            backgroundColor = MiuixTheme.colorScheme.surface,
            tint = HazeTint(MiuixTheme.colorScheme.surface.copy(0.8f))
        )

        // Wrapper for DeviceListDialog
        val showDeviceSettingsDialogState = remember(uiState.showDeviceSettingsDialog) {
            object : MutableState<Boolean> {
                override var value: Boolean
                    get() = uiState.showDeviceSettingsDialog
                    set(v) {
                        appViewModel.updateShowDeviceSettingsDialog(v)
                    }

                override fun component1() = value
                override fun component2(): (Boolean) -> Unit = { value = it }
            }
        }
        DeviceListDialog(showDeviceSettingsDialogState)

        BoxWithConstraints(
            modifier = Modifier.scrollEndHaptic()
        ) {
            if (maxWidth < 768.dp) {
                PortraitAppView(
                    hazeState = hazeState,
                    hazeStyle = hazeStyle,
                    scrollBehavior = scrollBehavior,
                    focusManager = focusManager,
                    isDarkTheme = isDarkTheme,
                    uiState = uiState,
                    viewModel = appViewModel
                )
            } else {
                LandscapeAppView(
                    scrollBehavior = scrollBehavior,
                    focusManager = focusManager,
                    isDarkTheme = isDarkTheme,
                    uiState = uiState,
                    viewModel = appViewModel
                )
            }
        }
        Snackbar()
    }
}

@Composable
private fun MenuActions(
    searchKeywords: List<String>,
    showMenuPopup: Boolean,
    focusManager: FocusManager,
    onClearSearchHistory: () -> Unit,
    onShowDeviceSettings: () -> Unit,
    onShowMenuPopupChange: (Boolean) -> Unit
) {
    val showMenuPopupState = remember(showMenuPopup) {
        mutableStateOf(showMenuPopup)
    }

    SuperListPopup(
        show = showMenuPopupState,
        popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
        alignment = PopupPositionProvider.Align.TopEnd,
        onDismissRequest = {
            onShowMenuPopupChange(false)
        }
    ) {
        ListPopupColumn {
            DropdownImpl(
                text = stringResource(Res.string.device_list_settings),
                optionSize = if (searchKeywords.isNotEmpty()) 2 else 1,
                isSelected = false,
                onSelectedIndexChange = {
                    onShowMenuPopupChange(false)
                    onShowDeviceSettings()
                },
                index = 0
            )

            if (searchKeywords.isNotEmpty()) {
                DropdownImpl(
                    text = stringResource(Res.string.clear_search_history),
                    optionSize = 2,
                    isSelected = false,
                    onSelectedIndexChange = {
                        onShowMenuPopupChange(false)
                        onClearSearchHistory()
                    },
                    index = 1
                )
            }
        }
    }

    IconButton(
        modifier = Modifier
            .padding(end = if (platform() != Platform.IOS && platform() != Platform.Android) 10.dp else 20.dp)
            .size(40.dp),
        onClick = {
            onShowMenuPopupChange(true)
            focusManager.clearFocus()
        },
        holdDownState = showMenuPopup
    ) {
        Icon(
            imageVector = MiuixIcons.Settings,
            tint = MiuixTheme.colorScheme.onBackground,
            contentDescription = "Menu"
        )
    }
}

@Composable
private fun PortraitAppView(
    hazeState: HazeState,
    hazeStyle: HazeStyle,
    scrollBehavior: ScrollBehavior,
    focusManager: FocusManager,
    isDarkTheme: Boolean,
    uiState: AppUiState,
    viewModel: AppViewModel
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
                        searchKeywords = uiState.searchKeywords,
                        showMenuPopup = uiState.showMenuPopup,
                        focusManager = focusManager,
                        onClearSearchHistory = { viewModel.clearSearchHistory() },
                        onShowDeviceSettings = { viewModel.updateShowDeviceSettingsDialog(true) },
                        onShowMenuPopupChange = { viewModel.updateShowMenuPopup(it) }
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
                .fillMaxHeight()
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
                        LoginCardView(uiState.isLogin, isDarkTheme, onLoginChange = { viewModel.updateLoginState(it) })
                        BasicViews(
                            deviceName = uiState.deviceName,
                            codeName = uiState.codeName,
                            androidVersion = uiState.androidVersion,
                            deviceRegion = uiState.deviceRegion,
                            deviceCarrier = uiState.deviceCarrier,
                            systemVersion = uiState.systemVersion,
                            searchKeywords = uiState.searchKeywords,
                            searchKeywordsSelected = uiState.searchKeywordsSelected,
                            onDeviceNameChange = { viewModel.updateDeviceName(it) },
                            onCodeNameChange = { viewModel.updateCodeName(it) },
                            onAndroidVersionChange = { viewModel.updateAndroidVersion(it) },
                            onDeviceRegionChange = { viewModel.updateDeviceRegion(it) },
                            onDeviceCarrierChange = { viewModel.updateDeviceCarrier(it) },
                            onSystemVersionChange = { viewModel.updateSystemVersion(it) },
                            onSearchKeywordsSelectedChange = { viewModel.updateSearchKeywordsSelected(it) },
                            onHistorySelect = { viewModel.loadSearchHistory(it) },
                            onSubmit = { viewModel.fetchRomInfo() }
                        )
                        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                            InfoCardViews(uiState.curRomInfo, uiState.curIconInfo, uiState.curImageInfo, 0)
                            InfoCardViews(uiState.incRomInfo, uiState.incIconInfo, uiState.incImageInfo, 0)
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
    isDarkTheme: Boolean,
    uiState: AppUiState,
    viewModel: AppViewModel
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
                            searchKeywords = uiState.searchKeywords,
                            showMenuPopup = uiState.showMenuPopup,
                            focusManager = focusManager,
                            onClearSearchHistory = { viewModel.clearSearchHistory() },
                            onShowDeviceSettings = { viewModel.updateShowDeviceSettingsDialog(true) },
                            onShowMenuPopupChange = { viewModel.updateShowMenuPopup(it) }
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
                        LoginCardView(uiState.isLogin, isDarkTheme, onLoginChange = { viewModel.updateLoginState(it) })
                        BasicViews(
                            deviceName = uiState.deviceName,
                            codeName = uiState.codeName,
                            androidVersion = uiState.androidVersion,
                            deviceRegion = uiState.deviceRegion,
                            deviceCarrier = uiState.deviceCarrier,
                            systemVersion = uiState.systemVersion,
                            searchKeywords = uiState.searchKeywords,
                            searchKeywordsSelected = uiState.searchKeywordsSelected,
                            onDeviceNameChange = { viewModel.updateDeviceName(it) },
                            onCodeNameChange = { viewModel.updateCodeName(it) },
                            onAndroidVersionChange = { viewModel.updateAndroidVersion(it) },
                            onDeviceRegionChange = { viewModel.updateDeviceRegion(it) },
                            onDeviceCarrierChange = { viewModel.updateDeviceCarrier(it) },
                            onSystemVersionChange = { viewModel.updateSystemVersion(it) },
                            onSearchKeywordsSelectedChange = { viewModel.updateSearchKeywordsSelected(it) },
                            onHistorySelect = { viewModel.loadSearchHistory(it) },
                            onSubmit = { viewModel.fetchRomInfo() }
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
                if (uiState.curRomInfo.version.isEmpty() && uiState.incRomInfo.version.isEmpty()) {
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
                            InfoCardViews(uiState.curRomInfo, uiState.curIconInfo, uiState.curImageInfo, 0)
                            InfoCardViews(uiState.incRomInfo, uiState.incIconInfo, uiState.incImageInfo, 0)
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

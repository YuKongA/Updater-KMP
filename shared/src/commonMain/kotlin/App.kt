import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
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

        val backdrop = rememberLayerBackdrop()
        val surfaceColor = MiuixTheme.colorScheme.surface

        val density = LocalDensity.current
        val isMobile = platform() == Platform.Android || platform() == Platform.IOS
        var containerWidth by remember { mutableStateOf(if (isMobile) 0.dp else 1280.dp) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scrollEndHaptic()
                .onSizeChanged { size ->
                    containerWidth = with(density) { size.width.toDp() }
                }
        ) {
            if (containerWidth < 768.dp) {
                PortraitAppView(
                    backdrop = backdrop,
                    surfaceColor = surfaceColor,
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
    OverlayListPopup(
        show = showMenuPopup,
        popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
        alignment = PopupPositionProvider.Align.TopEnd,
        onDismissRequest = {
            onShowMenuPopupChange(false)
        },
        content = {
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
    )

    IconButton(
        modifier = Modifier.size(40.dp),
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
    backdrop: LayerBackdrop,
    surfaceColor: Color,
    scrollBehavior: ScrollBehavior,
    focusManager: FocusManager,
    isDarkTheme: Boolean,
    uiState: AppUiState,
    viewModel: AppViewModel
) {
    val blurSupported = isRenderEffectSupported()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                color = if (blurSupported) Color.Transparent else MiuixTheme.colorScheme.surface,
                title = stringResource(Res.string.app_name),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    AboutDialog(
                        show = uiState.showAboutDialog,
                        onShow = { viewModel.updateShowAboutDialog(true) },
                        onDismissRequest = { viewModel.updateShowAboutDialog(false) }
                    )
                },
                actions = {
                    MenuActions(
                        searchKeywords = uiState.searchKeywords,
                        showMenuPopup = uiState.showMenuPopup,
                        focusManager = focusManager,
                        onClearSearchHistory = { viewModel.clearSearchHistory() },
                        onShowDeviceSettings = { viewModel.updateShowDeviceSettingsDialog(true) },
                        onShowMenuPopupChange = { viewModel.updateShowMenuPopup(it) }
                    )
                    DeviceListDialog(
                        show = uiState.showDeviceSettingsDialog,
                        onDismissRequest = { viewModel.updateShowDeviceSettingsDialog(false) }
                    )
                },
                modifier = if (blurSupported) {
                    Modifier.textureBlur(
                        backdrop = backdrop,
                        shape = RectangleShape,
                        blurRadius = 25f * LocalDensity.current.density,
                        colors = BlurColors(
                            blendColors = listOf(BlendColorEntry(color = surfaceColor.copy(0.8f)))
                        ),
                        noiseCoefficient = 0f
                    )
                } else {
                    Modifier
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .then(if (blurSupported) Modifier.layerBackdrop(backdrop) else Modifier)
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
        DeviceListDialog(
            show = uiState.showDeviceSettingsDialog,
            onDismissRequest = { viewModel.updateShowDeviceSettingsDialog(false) }
        )
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
                    navigationIcon = {
                        AboutDialog(
                            show = uiState.showAboutDialog,
                            onShow = { viewModel.updateShowAboutDialog(true) },
                            onDismissRequest = { viewModel.updateShowAboutDialog(false) }
                        )
                    },
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

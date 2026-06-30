@file:OptIn(ExperimentalScrollBarApi::class)

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import data.DataHelper
import kotlinx.coroutines.flow.merge
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.VerticalDivider
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.SearchDevice
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.interfaces.ExperimentalScrollBarApi
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
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
import ui.XmsInfoCardView
import updater.shared.generated.resources.Res
import updater.shared.generated.resources.app_name
import updater.shared.generated.resources.clear_search_history
import updater.shared.generated.resources.device_list_settings
import updater.shared.generated.resources.icon
import utils.MessageUtils
import utils.MessageUtils.Companion.Snackbar
import viewmodel.DeviceListUiState
import viewmodel.DeviceListViewModel
import viewmodel.LoginUiState
import viewmodel.LoginViewModel
import viewmodel.RomQueryUiState
import viewmodel.RomQueryViewModel
import viewmodel.UiEvent

@Composable
fun App(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    loginViewModel: LoginViewModel = viewModel { LoginViewModel() },
    romQueryViewModel: RomQueryViewModel = viewModel { RomQueryViewModel() },
    deviceListViewModel: DeviceListViewModel = viewModel { DeviceListViewModel() },
) {
    AppTheme(
        isDarkTheme = isDarkTheme
    ) {
        val loginUi by loginViewModel.uiState.collectAsState()
        val queryUi by romQueryViewModel.uiState.collectAsState()
        val deviceListUi by deviceListViewModel.uiState.collectAsState()

        LaunchedEffect(loginViewModel, romQueryViewModel) {
            merge(loginViewModel.uiEvent, romQueryViewModel.uiEvent).collect { event ->
                when (event) {
                    is UiEvent.ShowMessage -> MessageUtils.showMessage(getString(event.resource), event.duration)
                }
            }
        }

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
                    loginUi = loginUi,
                    queryUi = queryUi,
                    deviceListUi = deviceListUi,
                    loginViewModel = loginViewModel,
                    romQueryViewModel = romQueryViewModel,
                    deviceListViewModel = deviceListViewModel,
                )
            } else {
                LandscapeAppView(
                    scrollBehavior = scrollBehavior,
                    focusManager = focusManager,
                    isDarkTheme = isDarkTheme,
                    loginUi = loginUi,
                    queryUi = queryUi,
                    deviceListUi = deviceListUi,
                    loginViewModel = loginViewModel,
                    romQueryViewModel = romQueryViewModel,
                    deviceListViewModel = deviceListViewModel,
                )
            }
        }
        Snackbar()
    }
}

@Composable
private fun MenuActions(
    searchHistory: List<DataHelper.SearchHistoryEntry>,
    focusManager: FocusManager,
    onClearSearchHistory: () -> Unit,
    onShowDeviceSettings: () -> Unit
) {
    val entry = DropdownEntry(
        items = listOfNotNull(
            DropdownItem(
                text = stringResource(Res.string.device_list_settings),
                onClick = onShowDeviceSettings
            ),
            if (searchHistory.isNotEmpty()) {
                DropdownItem(
                    text = stringResource(Res.string.clear_search_history),
                    onClick = onClearSearchHistory
                )
            } else null
        )
    )
    OverlayIconDropdownMenu(
        entry = entry,
        modifier = Modifier.size(40.dp),
        onExpandedChange = { expanded -> if (expanded) focusManager.clearFocus() }
    ) {
        Icon(
            imageVector = MiuixIcons.Settings,
            tint = MiuixTheme.colorScheme.onBackground,
            contentDescription = "Menu"
        )
    }
}

@Composable
private fun FillFormCurrentDeviceAction(
    focusManager: FocusManager,
    onReadCurrentDevice: () -> Unit,
) {
    IconButton(
        modifier = Modifier.size(40.dp),
        onClick = {
            focusManager.clearFocus()
            onReadCurrentDevice()
        }
    ) {
        Icon(
            imageVector = MiuixIcons.SearchDevice,
            tint = MiuixTheme.colorScheme.onBackground,
            contentDescription = "Fill from current device"
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
    loginUi: LoginUiState,
    queryUi: RomQueryUiState,
    deviceListUi: DeviceListUiState,
    loginViewModel: LoginViewModel,
    romQueryViewModel: RomQueryViewModel,
    deviceListViewModel: DeviceListViewModel,
) {
    val blurSupported = isRuntimeShaderSupported()
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    var showDeviceSettingsDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                color = if (blurSupported) Color.Transparent else MiuixTheme.colorScheme.surface,
                title = stringResource(Res.string.app_name),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    AboutDialog(
                        show = showAboutDialog,
                        onShow = { showAboutDialog = true },
                        onDismissRequest = { showAboutDialog = false }
                    )
                },
                actions = {
                    if (queryUi.currentDeviceInfo != null) {
                        FillFormCurrentDeviceAction(
                            focusManager = focusManager,
                            onReadCurrentDevice = { romQueryViewModel.fillWithCurrent() }
                        )
                    }
                    MenuActions(
                        searchHistory = queryUi.searchHistory,
                        focusManager = focusManager,
                        onClearSearchHistory = { romQueryViewModel.clearSearchHistory() },
                        onShowDeviceSettings = {
                            deviceListViewModel.resetUpdateState()
                            showDeviceSettingsDialog = true
                        }
                    )
                    DeviceListDialog(
                        show = showDeviceSettingsDialog,
                        source = deviceListUi.source,
                        version = deviceListUi.version,
                        updateState = deviceListUi.updateState,
                        onSourceChange = { deviceListViewModel.setSource(it) },
                        onRefresh = { deviceListViewModel.refresh() },
                        onDismissRequest = { showDeviceSettingsDialog = false }
                    )
                },
                modifier = if (blurSupported) {
                    Modifier.textureBlur(
                        backdrop = backdrop,
                        shape = RectangleShape,
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
                        LoginCardView(
                            loginUi = loginUi,
                            isDarkTheme = isDarkTheme,
                            accountState = loginViewModel.accountState,
                            passwordState = loginViewModel.passwordState,
                            ticketState = loginViewModel.ticketState,
                            onShowLoginDialog = { loginViewModel.showLoginDialog() },
                            onLoginEvent = { loginViewModel.onLoginEvent(it) }
                        )
                        BasicViews(
                            deviceName = queryUi.deviceName,
                            codeName = queryUi.codeName,
                            androidVersion = queryUi.androidVersion,
                            deviceRegion = queryUi.deviceRegion,
                            deviceCarrier = queryUi.deviceCarrier,
                            systemVersion = queryUi.systemVersion,
                            deviceNames = queryUi.deviceNames,
                            codeNames = queryUi.codeNames,
                            searchHistory = queryUi.searchHistory,
                            searchHistorySelected = queryUi.searchHistorySelected,
                            onDeviceNameChange = { romQueryViewModel.updateDeviceName(it) },
                            onCodeNameChange = { romQueryViewModel.updateCodeName(it) },
                            onAndroidVersionChange = { romQueryViewModel.updateAndroidVersion(it) },
                            onDeviceRegionChange = { romQueryViewModel.updateDeviceRegion(it) },
                            onDeviceCarrierChange = { romQueryViewModel.updateDeviceCarrier(it) },
                            onSystemVersionChange = { romQueryViewModel.updateSystemVersion(it) },
                            onSearchHistorySelectedChange = { romQueryViewModel.updateSearchHistorySelected(it) },
                            onHistorySelect = { romQueryViewModel.loadSearchHistory(it) },
                            onSubmit = { romQueryViewModel.fetchRomInfo() }
                        )
                        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                            InfoCardViews(
                                romInfo = queryUi.curRomInfo,
                                iconInfo = queryUi.curIconInfo,
                                imageInfo = queryUi.curImageInfo,
                                onCopySuccess = romQueryViewModel::notifyCopySuccess,
                                onDownloadStart = romQueryViewModel::notifyDownloadStart,
                            )
                            InfoCardViews(
                                romInfo = queryUi.incRomInfo,
                                iconInfo = queryUi.incIconInfo,
                                imageInfo = queryUi.incImageInfo,
                                onCopySuccess = romQueryViewModel::notifyCopySuccess,
                                onDownloadStart = romQueryViewModel::notifyDownloadStart,
                            )
                            XmsInfoCardView(
                                xmsInfo = queryUi.xmsInfo,
                                onCopySuccess = romQueryViewModel::notifyCopySuccess,
                                onDownloadStart = romQueryViewModel::notifyDownloadStart,
                            )
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
    loginUi: LoginUiState,
    queryUi: RomQueryUiState,
    deviceListUi: DeviceListUiState,
    loginViewModel: LoginViewModel,
    romQueryViewModel: RomQueryViewModel,
    deviceListViewModel: DeviceListViewModel,
) {
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    var showDeviceSettingsDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { scaffoldPaddingValues ->
        DeviceListDialog(
            show = showDeviceSettingsDialog,
            source = deviceListUi.source,
            version = deviceListUi.version,
            updateState = deviceListUi.updateState,
            onSourceChange = { deviceListViewModel.setSource(it) },
            onRefresh = { deviceListViewModel.refresh() },
            onDismissRequest = { showDeviceSettingsDialog = false }
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
                            show = showAboutDialog,
                            onShow = { showAboutDialog = true },
                            onDismissRequest = { showAboutDialog = false }
                        )
                    },
                    actions = {
                        if (queryUi.currentDeviceInfo != null) {
                            FillFormCurrentDeviceAction(
                                focusManager = focusManager,
                                onReadCurrentDevice = { romQueryViewModel.fillWithCurrent() }
                            )
                        }
                        MenuActions(
                            searchHistory = queryUi.searchHistory,
                            focusManager = focusManager,
                            onClearSearchHistory = { romQueryViewModel.clearSearchHistory() },
                            onShowDeviceSettings = {
                                deviceListViewModel.resetUpdateState()
                                showDeviceSettingsDialog = true
                            }
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
                        LoginCardView(
                            loginUi = loginUi,
                            isDarkTheme = isDarkTheme,
                            accountState = loginViewModel.accountState,
                            passwordState = loginViewModel.passwordState,
                            ticketState = loginViewModel.ticketState,
                            onShowLoginDialog = { loginViewModel.showLoginDialog() },
                            onLoginEvent = { loginViewModel.onLoginEvent(it) }
                        )
                        BasicViews(
                            deviceName = queryUi.deviceName,
                            codeName = queryUi.codeName,
                            androidVersion = queryUi.androidVersion,
                            deviceRegion = queryUi.deviceRegion,
                            deviceCarrier = queryUi.deviceCarrier,
                            systemVersion = queryUi.systemVersion,
                            deviceNames = queryUi.deviceNames,
                            codeNames = queryUi.codeNames,
                            searchHistory = queryUi.searchHistory,
                            searchHistorySelected = queryUi.searchHistorySelected,
                            onDeviceNameChange = { romQueryViewModel.updateDeviceName(it) },
                            onCodeNameChange = { romQueryViewModel.updateCodeName(it) },
                            onAndroidVersionChange = { romQueryViewModel.updateAndroidVersion(it) },
                            onDeviceRegionChange = { romQueryViewModel.updateDeviceRegion(it) },
                            onDeviceCarrierChange = { romQueryViewModel.updateDeviceCarrier(it) },
                            onSystemVersionChange = { romQueryViewModel.updateSystemVersion(it) },
                            onSearchHistorySelectedChange = { romQueryViewModel.updateSearchHistorySelected(it) },
                            onHistorySelect = { romQueryViewModel.loadSearchHistory(it) },
                            onSubmit = { romQueryViewModel.fetchRomInfo() }
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
                if (queryUi.curRomInfo.version.isEmpty() && queryUi.incRomInfo.version.isEmpty()) {
                    Image(
                        painter = painterResource(Res.drawable.icon),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(128.dp),
                    )
                }
                val lazyListState = rememberLazyListState()
                LazyColumn(
                    state = lazyListState,
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
                            InfoCardViews(
                                romInfo = queryUi.curRomInfo,
                                iconInfo = queryUi.curIconInfo,
                                imageInfo = queryUi.curImageInfo,
                                onCopySuccess = romQueryViewModel::notifyCopySuccess,
                                onDownloadStart = romQueryViewModel::notifyDownloadStart,
                            )
                            InfoCardViews(
                                romInfo = queryUi.incRomInfo,
                                iconInfo = queryUi.incIconInfo,
                                imageInfo = queryUi.incImageInfo,
                                onCopySuccess = romQueryViewModel::notifyCopySuccess,
                                onDownloadStart = romQueryViewModel::notifyDownloadStart,
                            )
                            XmsInfoCardView(
                                xmsInfo = queryUi.xmsInfo,
                                onCopySuccess = romQueryViewModel::notifyCopySuccess,
                                onDownloadStart = romQueryViewModel::notifyDownloadStart,
                            )
                        }
                        Spacer(
                            Modifier.height(
                                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                            )
                        )
                    }
                }
                VerticalScrollBar(
                    adapter = rememberScrollBarAdapter(lazyListState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    trackPadding = PaddingValues(
                        top = scaffoldPaddingValues.calculateTopPadding(),
                        bottom = scaffoldPaddingValues.calculateBottomPadding()
                    )
                )
            }
        }
    }
}

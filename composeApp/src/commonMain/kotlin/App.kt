import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import data.DataHelper
import misc.MessageUtils.Companion.Snackbar
import misc.json
import misc.updateRomInfo
import org.jetbrains.compose.resources.stringResource
import ui.AboutDialog
import ui.DownloadCardViews
import ui.LoginCardView
import ui.LoginDialog
import ui.MessageCardViews
import ui.MoreInfoCardViews
import ui.TextFieldViews
import ui.TuneDialog
import updaterkmp.composeapp.generated.resources.Res
import updaterkmp.composeapp.generated.resources.app_name
import updaterkmp.composeapp.generated.resources.submit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    colorMode: MutableState<Int> = remember { mutableStateOf(perfGet("colorMode")?.toInt() ?: 0) }
) {
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val listState = rememberLazyListState()
    var fabVisible by remember { mutableStateOf(true) }
    var scrollDistance by remember { mutableStateOf(0f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val isScrolledToEnd =
                    (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == listState.layoutInfo.totalItemsCount - 1 && (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.size
                        ?: 0) < listState.layoutInfo.viewportEndOffset)

                val delta = available.y
                if (!isScrolledToEnd) {
                    scrollDistance += delta
                    if (scrollDistance < -50f) {
                        if (fabVisible) fabVisible = false
                        scrollDistance = 0f
                    } else if (scrollDistance > 50f) {
                        if (!fabVisible) fabVisible = true
                        scrollDistance = 0f
                    }
                }
                return Offset.Zero
            }
        }
    }

    val offsetHeight by animateDpAsState(
        targetValue = if (fabVisible) 0.dp else 74.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(), // 74.dp = FAB + FAB Padding
        animationSpec = tween(durationMillis = 350)
    )

    AppTheme(colorMode = colorMode.value) {
        Surface {
            Scaffold(
                modifier = Modifier
                    .displayCutoutPadding()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    TopAppBar(scrollBehavior, colorMode, isLogin)
                }
            ) {
                Box(
                    modifier = Modifier.nestedScroll(nestedScrollConnection)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .padding(top = it.calculateTopPadding())
                            .padding(horizontal = 20.dp)
                    ) {
                        item {
                            BoxWithConstraints(
                                modifier = Modifier.navigationBarsPadding()
                            ) {
                                if (maxWidth < 768.dp) {
                                    Column {
                                        LoginCardView(isLogin)
                                        TextFieldViews(deviceName, codeName, deviceRegion, androidVersion, systemVersion, updateRomInfo)
                                        MessageCardViews(curRomInfo)
                                        MoreInfoCardViews(curRomInfo, curIconInfo)
                                        DownloadCardViews(curRomInfo)
                                        MessageCardViews(incRomInfo)
                                        MoreInfoCardViews(incRomInfo, incIconInfo)
                                        DownloadCardViews(incRomInfo)
                                    }
                                } else {
                                    Column {
                                        Row {
                                            Column(modifier = Modifier.weight(0.8f).padding(end = 20.dp)) {
                                                LoginCardView(isLogin)
                                                TextFieldViews(deviceName, codeName, deviceRegion, androidVersion, systemVersion, updateRomInfo)
                                            }
                                            Column(modifier = Modifier.weight(1.0f)) {
                                                MessageCardViews(curRomInfo)
                                                MoreInfoCardViews(curRomInfo, curIconInfo)
                                                DownloadCardViews(curRomInfo)
                                                MessageCardViews(incRomInfo)
                                                MoreInfoCardViews(incRomInfo, incIconInfo)
                                                DownloadCardViews(incRomInfo)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    FloatActionButton(offsetHeight, updateRomInfo)
                    Snackbar(offsetHeight)
                    updateRomInfo(
                        deviceName, codeName, deviceRegion, androidVersion, systemVersion, loginData,
                        isLogin, curRomInfo, incRomInfo, curIconInfo, incIconInfo, updateRomInfo
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(scrollBehavior: TopAppBarScrollBehavior, colorMode: MutableState<Int>, isLogin: MutableState<Int>) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
            )
        },
        colors = TopAppBarColors(
            containerColor = TopAppBarDefaults.topAppBarColors().containerColor,
            scrolledContainerColor = TopAppBarDefaults.topAppBarColors().containerColor,
            actionIconContentColor = TopAppBarDefaults.topAppBarColors().actionIconContentColor,
            navigationIconContentColor = TopAppBarDefaults.topAppBarColors().navigationIconContentColor,
            titleContentColor = TopAppBarDefaults.topAppBarColors().titleContentColor
        ),
        navigationIcon = {
            AboutDialog()
        },
        actions = {
            TuneDialog(colorMode)
            if (!isWasm()) LoginDialog(isLogin)
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun FloatActionButton(fabOffsetHeight: Dp, updateRomInfo: MutableState<Int>) {
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(18.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        ExtendedFloatingActionButton(
            modifier = Modifier.offset(y = fabOffsetHeight),
            text = { Text(text = stringResource(Res.string.submit)) },
            icon = { Icon(imageVector = Icons.Filled.Check, contentDescription = stringResource(Res.string.submit)) },
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                updateRomInfo.value++
            }
        )
    }
}
package com.theeasiestway.stereoar.ui.screens.choose_model

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.theeasiestway.stereoar.BuildConfig
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.ui.screens.choose_model.viewmodel.ChooseArModelViewModel
import com.theeasiestway.stereoar.ui.screens.choose_model.viewmodel.ChooseArModelViewModelStub
import com.theeasiestway.stereoar.ui.theme.Colors
import kotlinx.coroutines.launch

/**
 * Created by Alexey Loboda on 17.07.2022
 */

@Composable
@Preview
fun ChooseArModelPreview() {
    val viewModel = ChooseArModelViewModelStub()
    val pagerState = rememberPagerState()
    val pages by viewModel.observePages().collectAsStateWithLifecycle(emptyList())
    val coroutineScope = rememberCoroutineScope()
    AppScaffold(
        appBarTitle = stringResource(R.string.app_name),
        appBarActions = {
            ChooseModelAppBarActions {}
        }
    ) {
        ChooseModel(
            pagerState = pagerState,
            pages = pages,
            onTabClicked = { coroutineScope.launch { pagerState.animateScrollToPage(it) } },
            onFileClicked = { viewModel.onFileClicked(it) },
            onBackClicked = { viewModel.onBackClicked(it) },
            onModelClicked = { viewModel.onModelClicked(it) }
        )
    }
}

@Composable
fun ChooseArModel(
    viewModel: ChooseArModelViewModel,
    onModelChosen: (ModelItem) -> Unit,
    onBackPressed: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val pages by viewModel.observePages().collectAsStateWithLifecycle(emptyList())
    val arModel by viewModel.observeArModel().collectAsStateWithLifecycle(null)
    var needToAskPermissions by remember { mutableStateOf(true) }

    if (needToAskPermissions) {
        RequestFilesPermissions {
            needToAskPermissions = false
            viewModel.onFilesPermissionsGranted()
        }
    }

    arModel?.let {
        onModelChosen(it)
        return
    }

    AppScaffold(
        appBarTitle = stringResource(R.string.app_name),
        appBarActions = {
            ChooseModelAppBarActions {

            }
        }
    ) {
        ChooseModel(
            pagerState = pagerState,
            pages = pages,
            onTabClicked = { coroutineScope.launch { pagerState.animateScrollToPage(it) } },
            onFileClicked = { viewModel.onFileClicked(it) },
            onBackClicked = { viewModel.onBackClicked(it) },
            onModelClicked = { viewModel.onModelClicked(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    appBarTitle: String,
    appBarActions: @Composable RowScope.() -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appBarTitle) },
                actions = appBarActions
            )
        },
        content = content
    )
}

@Composable
fun ChooseModelAppBarActions(onOptionsClicked: () -> Unit) {
    Row {
        IconButton(onClick = onOptionsClicked) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                tint = Color.White,
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ChooseModel(
    pagerState: PagerState,
    pages: List<PagerPage>,
    onTabClicked: (Int) -> Unit,
    onFileClicked: (FileItem) -> Unit,
    onBackClicked: (String) -> Unit,
    onModelClicked: (ModelItem) -> Unit
) {
    Column {
        if (pages.isNotEmpty()) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                pages.forEachIndexed { index, page ->
                    Tab(
                        text = { Text(stringResource(page.title)) },
                        selected = pagerState.currentPage == index,
                        onClick = { onTabClicked(index) }
                    )
                }
            }
        }
        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) {
            PageContent(pages[it], onFileClicked, onBackClicked, onModelClicked)
        }
    }
}

@Composable
fun PageContent(
    page: PagerPage,
    onFileClicked: (FileItem) -> Unit,
    onBackClicked: (String) -> Unit,
    onModelClicked: (ModelItem) -> Unit
) {
    when(page) {
        is PagerPage.FilesExplorerState -> FilesExplorer(page, onFileClicked, onBackClicked)
        is PagerPage.FavoritesModelsState -> FavoritesModels(page, onModelClicked)
    }
}

@Composable
fun FilesExplorer(
    model: PagerPage.FilesExplorerState,
    onFileClicked: (FileItem) -> Unit,
    onBackClicked: (String) -> Unit
) {
    BackHandler(enabled = model.canMoveBack) {
        onBackClicked(model.currentPath)
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 12.dp),
                text = model.displayablePath
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(model.files) { index, item ->
                FileListItem(
                    item,
                    index == model.files.lastIndex,
                    onFileClicked
                )
            }
        }
    }
}

@Composable
fun FileListItem(
    item: FileItem,
    isLastItem: Boolean,
    onClicked: (FileItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val tint: ColorFilter
    val subtitle: String?
    val isFolder: Boolean
    when(item) {
        is FileItem.Folder -> {
            tint = ColorFilter.tint(Colors.primary_blue)
            subtitle = if (item.itemsCount != null && item.creationDate != null) "${item.itemsCount} | ${item.creationDate}" else null
            isFolder = true
        }
        is FileItem.File -> {
            tint = ColorFilter.tint(Colors.light_blue)
            subtitle = "${item.size} | ${item.creationDate}"
            isFolder = false
        }
    }
    Row(modifier = modifier
        .fillMaxWidth()
        .height(60.dp)
        .background(color = MaterialTheme.colorScheme.surface)
        .clickable { onClicked(item) }
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.CenterVertically),
            imageVector = ImageVector.vectorResource(item.icon),
            colorFilter = tint,
            contentDescription = null
        )
        Column(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(start = 8.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(text = item.title)
            if (subtitle != null) Text(text = subtitle)
        }
        if (isFolder) {
            Image(
                modifier = Modifier.align(Alignment.CenterVertically),
                imageVector = Icons.Default.KeyboardArrowRight,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                contentDescription = null
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
    if (!isLastItem) {
        Spacer(modifier = Modifier.height(1.dp))
    }
}

@Composable
fun FavoritesModels(model: PagerPage.FavoritesModelsState, onModelClicked: (ModelItem) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(model.models) { FavoriteModelListItem(it, onModelClicked) }
    }
}

@Composable
fun FavoriteModelListItem(item: ModelItem, onClicked: (ModelItem) -> Unit, modifier: Modifier = Modifier) {
    val icon = Icons.Default.Star
    Row(modifier = modifier
        .fillMaxWidth()
        .height(48.dp)
        .clickable { onClicked(item) }
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.CenterVertically),
            imageVector = icon,
            colorFilter = ColorFilter.tint(Colors.primary_orange),
            contentDescription = null
        )
        Column(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(start = 8.dp)) {
            Text(modifier = Modifier.weight(1f), text = item.title)
            Text(modifier = Modifier.weight(1f), text = "${item.size} | ${item.creationDate}")
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestFilesPermissions(
    onGranted: @Composable () -> Unit
) {
    var shouldShowRational by remember { mutableStateOf(true) }
    var requestResultReceived by remember { mutableStateOf(false) }
    val filesPermissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE) {
        requestResultReceived = true
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isFirstStart by remember { mutableStateOf(true) }
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val eventObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    Log.d("wdqwdqw", "permission requested less api 30")
                    filesPermissionState.launchPermissionRequest()
                } else if (!Environment.isExternalStorageManager()) {
                    Log.d("wdqwdqw", "permission requested for api 30")
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                        )
                    )
                    shouldShowRational = isFirstStart
                    isFirstStart = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(eventObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(eventObserver)
        }
    })

    if (shouldShowRational) {
        Log.d("wdqwdqw", "rational shown")

        val showDialog = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) filesPermissionState.status.shouldShowRationale
        else Environment.isExternalStorageManager()

        if (showDialog) {
            FilesPermissionRationaleDialog {
                Log.d("wdqwdqw", "rational closed")
                shouldShowRational = false

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    Log.d("wdqwdqw", "permission requested less api 30 [2]")
                    filesPermissionState.launchPermissionRequest()
                } else {
                    Log.d("wdqwdqw", "permission requested for api 30 [2]")
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                        )
                    )
                }
            }
        }
    } else {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (!filesPermissionState.status.isGranted && !filesPermissionState.status.shouldShowRationale) {
                Log.d("wdqwdqw", "permission denied less api 30")
                FilesPermissionDeniedDialog()
            } else if (filesPermissionState.status.isGranted) {
                Log.d("wdqwdqw", "permission granted less api 30")
                onGranted()
            }
        } else {
            if (!Environment.isExternalStorageManager()) {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                    )
                )
            } else {
                Log.d("wdqwdqw", "permission granted for api 30")
                onGranted()
            }
        }
    }
}
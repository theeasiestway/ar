package com.theeasiestway.stereoar.ui.screens.models_explorer

import android.text.format.Formatter.formatFileSize
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.scope.resultRecipient
import com.theeasiestway.domain.model.CollectedModel
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.di.is24TimeFormatQualifier
import com.theeasiestway.stereoar.di.modelsExplorerScopeId
import com.theeasiestway.stereoar.ui.screens.common.compose.images.ImageDrawable
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.Permission
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.RequestPermissionResult
import com.theeasiestway.stereoar.ui.screens.common.compose.scaffold.TopBarAction
import com.theeasiestway.stereoar.ui.screens.common.compose.text.*
import com.theeasiestway.stereoar.ui.screens.common.ext.resource
import com.theeasiestway.stereoar.ui.screens.common.ext.showSnackBar
import com.theeasiestway.stereoar.ui.screens.common.koin.createScopeIfNull
import com.theeasiestway.stereoar.ui.screens.common.onSideEffect
import com.theeasiestway.stereoar.ui.screens.destinations.ModelViewScreenDestination
import com.theeasiestway.stereoar.ui.screens.destinations.ModelsExplorerScreenDestination
import com.theeasiestway.stereoar.ui.screens.destinations.RequestFilesPermissionScreenDestination
import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelsExplorerViewModel.Intent
import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelsExplorerViewModel.SideEffect
import com.theeasiestway.stereoar.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun ManualComposableCallsBuilder.modelsExplorerScreenFactory(
    snackBarHostState: SnackbarHostState,
    topBarActionsClickListener: Flow<TopBarAction>,
    onCloseApp: () -> Unit
) {
    composable(ModelsExplorerScreenDestination) {
        createScopeIfNull(scopeId = modelsExplorerScopeId)
        ModelsExplorerScreen(
            snackBarHostState = snackBarHostState,
            navigator = destinationsNavigator,
            topBarActionsClickListener = topBarActionsClickListener,
            requestPermissionHandler = resultRecipient(),
            onCloseApp = onCloseApp
        )
    }
}

@RootNavGraph(start = true)
@Destination
@Composable
fun ModelsExplorerScreen(
    snackBarHostState: SnackbarHostState,
    navigator: DestinationsNavigator,
    topBarActionsClickListener: Flow<TopBarAction>,
    onCloseApp: () -> Unit,
    requestPermissionHandler: ResultRecipient<RequestFilesPermissionScreenDestination, RequestPermissionResult>
) {
    val viewModel: ModelsExplorerViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState(initial = UiState()).value
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val is24TimeFormat: Boolean = get(named(is24TimeFormatQualifier))
    val dateFormat by remember(is24TimeFormat) {
        mutableStateOf(if (is24TimeFormat) "dd.MM.yyyy HH:mm" else "dd.MM.yyyy h:mm a")
    }
    val dateFormatter by remember(Locale.getDefault(), dateFormat) {
        mutableStateOf(SimpleDateFormat(dateFormat, Locale.getDefault()))
    }

    LaunchedEffect(Unit) {
        topBarActionsClickListener.collect {
            viewModel.handleIntent(Intent.ShowOptions)
        }
    }

    requestPermissionHandler.onNavResult { result ->
        if (result is NavResult.Value && result.value.permission == Permission.ReadFiles) {
            viewModel.handleIntent(Intent.HandlePermissionResult(result.value.result))
        }
    }

    viewModel.onSideEffect { effect ->
        when(effect) {
            is SideEffect.RequestPermissions -> {
                navigator.navigate(RequestFilesPermissionScreenDestination)
            }
            is SideEffect.ErrorLoadingData -> {
                showSnackBar(
                    coroutineScope = coroutineScope,
                    snackBarHostState = snackBarHostState,
                    message = context.getString(R.string.error_loading_models_explorer)
                )
            }
            is SideEffect.ErrorOpeningFile -> {
                val fileOrFolder = context.getString(
                    if (effect.isFolder) R.string.general_folder else R.string.general_file
                ).lowercase()
                showSnackBar(
                    coroutineScope = coroutineScope,
                    snackBarHostState = snackBarHostState,
                    message = context.getString(R.string.error_opening_file_or_folder, fileOrFolder)
                )
            }
            is SideEffect.OpenModelScreen -> {
                navigator.navigate(ModelViewScreenDestination)
            }
            is SideEffect.CloseApp -> {
                onCloseApp()
            }
        }
    }
    Content(
        uiState = uiState,
        coroutineScope = coroutineScope,
        dateFormatter = dateFormatter,
        onFileClick = { file ->
            viewModel.handleIntent(Intent.OpenFile(file))
        },
        onModelClick = { model ->
            viewModel.handleIntent(Intent.ShowModel(model))
        },
        onBackClick = {
            viewModel.handleIntent(Intent.GoBack)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Content(
    uiState: UiState,
    coroutineScope: CoroutineScope,
    dateFormatter: DateFormat,
    onFileClick: (FileItem) -> Unit,
    onModelClick: (CollectedModel) -> Unit,
    onBackClick: () -> Unit
) {
    if (uiState.permissionsGranted) {
        val pagerState = rememberPagerState()
        Pages(
            coroutineScope = coroutineScope,
            dateFormatter = dateFormatter,
            pagerState = pagerState,
            pages = uiState.pages,
            onFileClick = onFileClick,
            onModelClick = onModelClick,
            onBackClick = onBackClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Pages(
    coroutineScope: CoroutineScope,
    dateFormatter: DateFormat,
    pagerState: PagerState,
    pages: List<PagerPage>,
    onFileClick: (FileItem) -> Unit,
    onModelClick: (CollectedModel) -> Unit,
    onBackClick: () -> Unit
) {
    Column {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = AppTheme.colors.primary,
            indicator = { tabs ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabs[pagerState.currentPage]),
                    color = AppTheme.colors.accent
                )
            },
            divider = {
                Divider(
                    color = AppTheme.colors.primary
                )
            }
        ) {
            pages.forEachIndexed { index, page ->
                Tab(
                    text = { PagerTitle(page = page) },
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            pageCount = pages.size,
            modifier = Modifier.fillMaxWidth()
        ) { pageIndex ->
            when (val page = pages[pageIndex]) {
                is PagerPage.FilesExplorer -> FilesExplorer(
                    page = page,
                    dateFormatter = dateFormatter,
                    onFileClick = onFileClick,
                    onBackClick = onBackClick
                )
                is PagerPage.ModelsCollection -> ModelsCollection(
                    page = page,
                    dateFormatter = dateFormatter,
                    onModelClick = onModelClick
                )
            }
        }
    }
}

@Composable
private fun PagerTitle(page: PagerPage) {
    val text = when(page) {
        is PagerPage.FilesExplorer -> R.string.models_explorer_title.resource()
        is PagerPage.ModelsCollection -> R.string.models_collection_title.resource()
    }
    BodyMedium(
        text = text,
        color = AppTheme.colors.white
    )
}

@Composable
private fun FilesExplorer(
    page: PagerPage.FilesExplorer,
    dateFormatter: DateFormat,
    onFileClick: (FileItem) -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler(enabled = page.canMoveBack) {
        onBackClick()
    }
    Column(modifier = Modifier.fillMaxSize()) {
        FilePath(page.displayablePath)
        Divider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = DividerDefaults.color.copy(alpha = 0.1f)
        )
        if (page.files.isNotEmpty()) {
            FilesList(
                page = page,
                dateFormatter = dateFormatter,
                onFileClick = onFileClick
            )
        } else {
            EmptyFiles()
        }
    }
}

@Composable
private fun FilePath(
    displayablePath: DisplayablePath
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = AppTheme.colors.surface)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        BodySmall(
            textAlign = TextAlign.Center,
            text = when(displayablePath) {
                is DisplayablePath.ExternalStorage -> "${R.string.models_explorer_sd_card.resource()}${displayablePath.path}"
                is DisplayablePath.InternalStorage -> "${R.string.models_explorer_internal_storage.resource()}${displayablePath.path}"
                is DisplayablePath.Root -> R.string.models_explorer_storage.resource()
            }
        )
    }
}

@Composable
private fun FilesList(
    page: PagerPage.FilesExplorer,
    dateFormatter: DateFormat,
    onFileClick: (FileItem) -> Unit,
) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .background(color = AppTheme.colors.surface)
    ) {
        itemsIndexed(page.files) { index, file ->
            FileListItem(
                file = file,
                dateFormatter = dateFormatter,
                onClick = onFileClick
            )
            if (index != page.files.lastIndex) {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = DividerDefaults.color.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
private fun FileListItem(
    file: FileItem,
    dateFormatter: DateFormat,
    onClick: (FileItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val icon: Int
    val tint: Color
    val title: String
    val subtitle: String?
    val isFolder: Boolean
    when(file) {
        is FileItem.Root -> {
            icon = if (file.isExternalStorage) R.drawable.ic_sd_card else R.drawable.ic_storage
            tint = AppTheme.colors.accent
            title = if (file.isExternalStorage) R.string.models_explorer_sd_card.resource() else R.string.models_explorer_internal_storage.resource()
            subtitle = null
            isFolder = true
        }
        is FileItem.NotRoot -> {
            val creationDate = runCatching { dateFormatter.format(file.creationDateMillis) }.getOrNull()
            when(file) {
                is FileItem.NotRoot.File -> {
                    icon = if (file.isArModel()) R.drawable.ic_ar else R.drawable.ic_file
                    tint = AppTheme.colors.primaryLight
                    title = file.title
                    subtitle = formatFileSize(context, file.sizeBytes).plus(if (creationDate != null) " | $creationDate" else "")
                    isFolder = false
                }
                is FileItem.NotRoot.Folder -> {
                    val filesLabel = context.resources.getQuantityString(R.plurals.models_explorer_files_count, file.filesCount)
                    icon = R.drawable.ic_folder
                    tint = AppTheme.colors.accent
                    title = file.title
                    subtitle = "${file.filesCount} $filesLabel".plus(if (creationDate != null) " | $creationDate" else "")
                    isFolder = true
                }
            }
        }
    }
    Row(modifier = modifier
        .fillMaxWidth()
        .height(60.dp)
        .background(color = AppTheme.colors.surface)
        .clickable { onClick(file) }
        .padding(horizontal = 16.dp)
    ) {
        ImageDrawable(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.CenterVertically),
            resId = icon,
            tint = tint
        )
        Column(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(start = 8.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            BodyMedium(text = title)
            if (subtitle != null) LabelSmall(text = subtitle)
        }
        if (isFolder) {
            ImageDrawable(
                modifier = Modifier.align(Alignment.CenterVertically),
                resId = R.drawable.ic_arrow_right_mini,
                tint = AppTheme.colors.primaryText,
            )
        }
    }
}

@Composable
private fun EmptyFiles() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageDrawable(
                modifier = Modifier.size(44.dp),
                resId = R.drawable.ic_folder_empty,
                tint = AppTheme.colors.accent
            )
            BodyMedium(text = R.string.general_empty.resource())
        }
    }
}

@Composable
private fun ModelsCollection(
    page: PagerPage.ModelsCollection,
    dateFormatter: DateFormat,
    onModelClick: (CollectedModel) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surface)
    ) {
        items(page.models) { model ->
            CollectedModelListItem(
                model = model,
                dateFormatter = dateFormatter,
                onClick = onModelClick
            )
        }
    }
}

@Composable
private fun CollectedModelListItem(
    model: CollectedModel,
    dateFormatter: DateFormat,
    onClick: (CollectedModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val creationDate = runCatching { dateFormatter.format(model.creationDateMillis) }.getOrNull()
    Row(modifier = modifier
        .fillMaxWidth()
        .height(48.dp)
        .background(AppTheme.colors.surface)
        .clickable { onClick(model) }
        .padding(horizontal = 16.dp)
    ) {
        ImageDrawable(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.CenterVertically),
            resId = R.drawable.ic_ar,
            tint = AppTheme.colors.accent
        )
        Column(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(start = 8.dp)
        ) {
            BodyMedium(
                modifier = Modifier.weight(1f),
                text = model.name
            )
            BodyMedium(
                modifier = Modifier.weight(1f),
                text = formatFileSize(context, model.sizeBytes).plus(if (creationDate != null) " | $creationDate" else "")
            )
        }
    }
}
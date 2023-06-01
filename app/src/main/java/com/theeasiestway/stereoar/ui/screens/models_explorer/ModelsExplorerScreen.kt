package com.theeasiestway.stereoar.ui.screens.models_explorer

import android.text.format.Formatter.formatFileSize
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
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
import androidx.compose.ui.window.Popup
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.scope.resultRecipient
import com.theeasiestway.domain.repositories.files.models.CollectedModel
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.di.is24TimeFormatQualifier
import com.theeasiestway.stereoar.ui.screens.common.compose.buttons.ImageButton
import com.theeasiestway.stereoar.ui.screens.common.compose.buttons.TopBarButton
import com.theeasiestway.stereoar.ui.screens.common.compose.custom.MenuContainer
import com.theeasiestway.stereoar.ui.screens.common.compose.dialogs.InputDialog
import com.theeasiestway.stereoar.ui.screens.common.compose.dialogs.InputRegexps
import com.theeasiestway.stereoar.ui.screens.common.compose.dialogs.TextDialog
import com.theeasiestway.stereoar.ui.screens.common.compose.images.ImageDrawable
import com.theeasiestway.stereoar.ui.screens.common.compose.items.PopupItem
import com.theeasiestway.stereoar.ui.screens.common.compose.modifiers.shimmerEffect
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.PermissionResult
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.ReadFilesPermission
import com.theeasiestway.stereoar.ui.screens.common.compose.scaffold.TopBar
import com.theeasiestway.stereoar.ui.screens.common.compose.text.*
import com.theeasiestway.stereoar.ui.screens.common.ext.onSideEffect
import com.theeasiestway.stereoar.ui.screens.common.ext.resource
import com.theeasiestway.stereoar.ui.screens.common.ext.showSnackBar
import com.theeasiestway.stereoar.ui.screens.destinations.ModelViewScreenDestination
import com.theeasiestway.stereoar.ui.screens.destinations.ModelsExplorerScreenDestination
import com.theeasiestway.stereoar.ui.screens.model_view.ModelViewResult
import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelsExplorerViewModel.Intent
import com.theeasiestway.stereoar.ui.screens.models_explorer.ModelsExplorerViewModel.SideEffect
import com.theeasiestway.stereoar.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun ManualComposableCallsBuilder.modelsExplorerScreenFactory(
    onCloseApp: () -> Unit
) {
    composable(ModelsExplorerScreenDestination) {
        ModelsExplorerScreen(
            navigator = destinationsNavigator,
            modelViewResult = resultRecipient(),
            onCloseApp = onCloseApp
        )
    }
}

@RootNavGraph(start = true)
@Destination
@Composable
fun ModelsExplorerScreen(
    navigator: DestinationsNavigator,
    modelViewResult: ResultRecipient<ModelViewScreenDestination, ModelViewResult>,
    onCloseApp: () -> Unit,
) {
    val viewModel: ModelsExplorerViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState(initial = UiState()).value
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }

    val is24TimeFormat: Boolean = get(named(is24TimeFormatQualifier))
    val dateFormat by remember(is24TimeFormat) {
        mutableStateOf(if (is24TimeFormat) "dd.MM.yyyy HH:mm" else "dd.MM.yyyy h:mm a")
    }
    val dateFormatter by remember(Locale.getDefault(), dateFormat) {
        mutableStateOf(SimpleDateFormat(dateFormat, Locale.getDefault()))
    }

    modelViewResult.onNavResult { result ->
        if (result is NavResult.Value && result.value == ModelViewResult.CollectedModelsChanged) {
            viewModel.handleIntent(Intent.UpdateCollectedModels)
        }
    }

    viewModel.onSideEffect { effect ->
        when(effect) {
            is SideEffect.OpenModelScreen -> {
                navigator.navigate(ModelViewScreenDestination(effect.modelUri))
            }
            is SideEffect.OpenAppSettings -> {
                showSnackBar(
                    coroutineScope = coroutineScope,
                    snackBarHostState = snackBarHostState,
                    message = "TODO"
                )
            }
            is SideEffect.CloseApp -> {
                onCloseApp()
            }
            is SideEffect.ErrorLoadingData -> {
                showSnackBar(
                    coroutineScope = coroutineScope,
                    snackBarHostState = snackBarHostState,
                    message = context.getString(R.string.models_explorer_error_loading_files)
                )
            }
            is SideEffect.ErrorRenameModel -> {
                showSnackBar(
                    coroutineScope = coroutineScope,
                    snackBarHostState = snackBarHostState,
                    message = context.getString(R.string.models_explorer_error_rename_model)
                )
            }
            is SideEffect.ErrorDeleteModel -> {
                showSnackBar(
                    coroutineScope = coroutineScope,
                    snackBarHostState = snackBarHostState,
                    message = context.getString(R.string.models_explorer_error_delete_model)
                )
            }
            is SideEffect.ErrorOpeningFile -> {
                val fileOrFolder = context.getString(
                    if (effect.isFolder) R.string.general_folder else R.string.general_file
                ).lowercase()
                showSnackBar(
                    coroutineScope = coroutineScope,
                    snackBarHostState = snackBarHostState,
                    message = context.getString(R.string.models_explorer_error_opening_file_or_folder, fileOrFolder)
                )
            }
        }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        topBar = {
            TopBar(
                title = R.string.app_name.resource(),
                actions = {
                    TopBarButton(
                        icon = R.drawable.ic_more,
                        tint = AppTheme.colors.surface
                    ) {
                        viewModel.handleIntent(Intent.HandleTopBarActionClick)
                    }
                }
            )
        },
    ) { paddingValues ->
        Content(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            coroutineScope = coroutineScope,
            dateFormatter = dateFormatter,
            onRequestPermissionsResult = { result ->
                viewModel.handleIntent(Intent.HandlePermissionResult(result))
            },
            onFileClick = { file ->
                viewModel.handleIntent(Intent.OpenFile(file))
            },
            onModelClick = { model ->
                viewModel.handleIntent(Intent.ShowModel(model))
            },
            onShowModelOptionsClick = { model ->
                viewModel.handleIntent(Intent.ShowModelOptions(model))
            },
            onModelOptionsClick = { option ->
                viewModel.handleIntent(Intent.HandleModelOptionsClick(option))
            },
            onModelRenamed = { newName, model ->
                viewModel.handleIntent(Intent.RenameModel(newName, model))
            },
            onModelDeleted = { model ->
                viewModel.handleIntent(Intent.DeleteModel(model))
            },
            onOptionsClick = { option ->
                viewModel.handleIntent(Intent.HandleOptionsClick(option))
            },
            onDownloadModelResult = { url ->
                viewModel.handleIntent(Intent.LoadModel(url))
            },
            onBackClick = { fromFilesList ->
                viewModel.handleIntent(Intent.GoBack(fromFilesList))
            }
        )
    }
}

@Composable
private fun Content(
    modifier: Modifier,
    uiState: UiState,
    coroutineScope: CoroutineScope,
    dateFormatter: DateFormat,
    onRequestPermissionsResult: (PermissionResult) -> Unit,
    onFileClick: (FileItem) -> Unit,
    onModelClick: (CollectedModel) -> Unit,
    onShowModelOptionsClick: (CollectedModel) -> Unit,
    onModelOptionsClick: (CollectedModelOptions?) -> Unit,
    onModelRenamed: (String, CollectedModel) -> Unit,
    onModelDeleted: (CollectedModel?) -> Unit,
    onOptionsClick: (ModelsExplorerOptions?) -> Unit,
    onDownloadModelResult: (String?) -> Unit,
    onBackClick: (fromFilesExplorer: Boolean) -> Unit
) {
    when {
        uiState.isLoading || uiState.requestPermissions -> {
            if (uiState.requestPermissions) {
                ReadFilesPermissionDialog(onResult = onRequestPermissionsResult)
            }
            ShimmeredFilesList(modifier = modifier)
        }
        else -> {
            Pager(
                modifier = modifier,
                coroutineScope = coroutineScope,
                dateFormatter = dateFormatter,
                pages = uiState.pages,
                onFileClick = onFileClick,
                onModelClick = onModelClick,
                onShowModelOptionsClick = onShowModelOptionsClick,
                onModelOptionsClick = onModelOptionsClick,
                onBackClick = onBackClick
            )
            when {
                uiState.showOptions -> {
                    TopBarOptions(onOptionsClick = onOptionsClick)
                }
                uiState.showDownloadModel -> {
                    DownloadModelDialog(
                        onConfirmButtonClick = { url -> onDownloadModelResult(url) },
                        onDismissButtonClick = { onDownloadModelResult(null) }
                    )
                }
                uiState.selectedCollectedModelOption != null -> {
                    when(val option = uiState.selectedCollectedModelOption) {
                        is CollectedModelOptions.Rename -> {
                            RenameModelDialog(
                                model = option.model,
                                onSaveClick = onModelRenamed
                            )
                        }
                        is CollectedModelOptions.Delete -> {
                            DeleteModelDialog(
                                model = option.model,
                                onConfirmed = onModelDeleted
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
fun ReadFilesPermissionDialog(onResult: (PermissionResult) -> Unit) {
    ReadFilesPermission(
        icon = R.drawable.ic_folder,
        rationalTitle = R.string.files_permission_rational_title.resource(),
        rationalText = R.string.files_permission_rational_text.resource(),
        deniedTitle = R.string.files_permission_rational_title.resource(),
        deniedText = R.string.files_permission_denied_text.resource(),
        deniedDismissButtonText = R.string.general_close_app.resource(),
        onResult = { result ->
            Log.d("qdwqdqw", "result: $result")
            onResult(result)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShimmeredFilesList(
    modifier: Modifier = Modifier
) {
    val count = 40
    LazyColumn(modifier = modifier) {
        repeat(count) { index ->
            item {
                ShimmeredFileListItem(
                    modifier = Modifier.animateItemPlacement()
                )
                if (index != count - 1) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = DividerDefaults.color.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShimmeredFileListItem(
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier
        .fillMaxWidth()
        .height(60.dp)
        .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.CenterVertically)
                .shimmerEffect(),
        )
        Column(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(start = 8.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(15.dp)
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(10.dp)
                    .shimmerEffect()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Pager(
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    dateFormatter: DateFormat,
    pages: List<PagerPage>,
    onFileClick: (FileItem) -> Unit,
    onModelClick: (CollectedModel) -> Unit,
    onShowModelOptionsClick: (CollectedModel) -> Unit,
    onModelOptionsClick: (CollectedModelOptions?) -> Unit,
    onBackClick: (fromFilesExplorer: Boolean) -> Unit
) {
    val pagerState = rememberPagerState()
    Column(modifier = modifier) {
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
                    isSelected = pagerState.currentPage == pageIndex,
                    page = page,
                    dateFormatter = dateFormatter,
                    onFileClick = onFileClick,
                    onBackClick = onBackClick
                )
                is PagerPage.ModelsCollection -> ModelsCollection(
                    isSelected = pagerState.currentPage == pageIndex,
                    page = page,
                    dateFormatter = dateFormatter,
                    onModelClick = onModelClick,
                    onShowModelOptionsClick = onShowModelOptionsClick,
                    onModelOptionsClick = onModelOptionsClick,
                    onBackClick = onBackClick
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
    isSelected: Boolean,
    page: PagerPage.FilesExplorer,
    dateFormatter: DateFormat,
    onFileClick: (FileItem) -> Unit,
    onBackClick: (fromFilesExplorer: Boolean) -> Unit
) {
    BackHandler(enabled = isSelected && page.canMoveBack) {
        onBackClick(true)
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

@OptIn(ExperimentalFoundationApi::class)
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
                onClick = onFileClick,
                modifier = Modifier.animateItemPlacement()
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
            tint = AppTheme.colors.primaryDark
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
                    val filesLabel = context.resources.getQuantityString(R.plurals.general_files_count, file.filesCount)
                    icon = R.drawable.ic_folder
                    tint = AppTheme.colors.primaryDark
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
    isSelected: Boolean,
    page: PagerPage.ModelsCollection,
    dateFormatter: DateFormat,
    onModelClick: (CollectedModel) -> Unit,
    onShowModelOptionsClick: (CollectedModel) -> Unit,
    onModelOptionsClick: (CollectedModelOptions?) -> Unit,
    onBackClick: (fromFilesExplorer: Boolean) -> Unit
) {
    BackHandler(enabled = isSelected) {
        onBackClick(false)
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surface)
    ) {
        items(page.models) { model ->
            CollectedModelListItem(
                model = model,
                showOptions = page.selectedModel == model,
                dateFormatter = dateFormatter,
                onClick = onModelClick,
                onShowModelOptionsClick = onShowModelOptionsClick,
                onModelOptionsClick = onModelOptionsClick
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CollectedModelListItem(
    model: CollectedModel,
    showOptions: Boolean,
    dateFormatter: DateFormat,
    onClick: (CollectedModel) -> Unit,
    onShowModelOptionsClick: (CollectedModel) -> Unit,
    onModelOptionsClick: (CollectedModelOptions?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val creationDate = runCatching { dateFormatter.format(model.creationDateMillis) }.getOrNull()
    Row(modifier = modifier
        .fillMaxWidth()
        .height(60.dp)
        .background(AppTheme.colors.surface)
        .combinedClickable(
            onClick = {
                onClick(model)
            },
            onLongClick = {
                onShowModelOptionsClick(model)
            }
        )
        .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
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
            .padding(start = 8.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            BodyMedium(text = model.name)
            LabelSmall(text = formatFileSize(context, model.sizeBytes).plus(if (creationDate != null) " | $creationDate" else ""))
        }
        ImageButton(
            modifier = Modifier.size(40.dp),
            icon = R.drawable.ic_more,
            tint = AppTheme.colors.primaryText,
        ) {
            onShowModelOptionsClick(model)
        }
        if (showOptions) {
            CollectedModelOptions(
                model = model,
                onOptionsClick = onModelOptionsClick,
            )
        }
    }
}

@Composable
private fun CollectedModelOptions(
    model: CollectedModel,
    onOptionsClick: (CollectedModelOptions?) -> Unit
) {
    Popup(
        alignment = Alignment.TopEnd,
        onDismissRequest = {
            onOptionsClick(null)
        }
    ) {
        MenuContainer {
            PopupItem(text = R.string.models_explorer_collected_model_rename.resource()) {
                onOptionsClick(CollectedModelOptions.Rename(model))
            }
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = DividerDefaults.color.copy(alpha = 0.1f)
            )
            PopupItem(text = R.string.models_explorer_collected_model_delete.resource()) {
                onOptionsClick(CollectedModelOptions.Delete(model))
            }
        }
    }
}

@Composable
private fun TopBarOptions(
    onOptionsClick: (ModelsExplorerOptions?) -> Unit
) {
    Popup(
        alignment = Alignment.TopEnd,
        onDismissRequest = {
            onOptionsClick(null)
        }
    ) {
        MenuContainer {
            PopupItem(text = R.string.models_explorer_download_model.resource()) {
                onOptionsClick(ModelsExplorerOptions.DownloadModel)
            }
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = DividerDefaults.color.copy(alpha = 0.1f)
            )
            PopupItem(text = R.string.general_app_settings.resource()) {
                onOptionsClick(ModelsExplorerOptions.AppSettings)
            }
        }
    }
}

@Composable
fun DownloadModelDialog(
    onConfirmButtonClick: (String) -> Unit,
    onDismissButtonClick: () -> Unit
) {
    InputDialog(
        icon = R.drawable.ic_download,
        text = "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb",
        title = R.string.models_explorer_download_model.resource(),
        confirmButtonText = R.string.models_explorer_download.resource(),
        dismissButtonText = R.string.general_cancel.resource(),
        validationRegex = InputRegexps.url,
        supportText = R.string.models_explorer_download_model_description_text.resource(),
        errorText = R.string.models_explorer_download_model_error_text.resource(),
        onConfirmButtonClick = onConfirmButtonClick,
        onDismissButtonClick = onDismissButtonClick
    )
}

@Composable
fun RenameModelDialog(
    model: CollectedModel,
    onSaveClick: (String, CollectedModel) -> Unit
) {
    InputDialog(
        icon = R.drawable.ic_rename,
        text = model.name,
        title = R.string.models_explorer_collected_model_rename_model.resource(),
        confirmButtonText = R.string.general_save.resource(),
        dismissButtonText = R.string.general_cancel.resource(),
        validationRegex = InputRegexps.atLeastOneNonWhiteSpace,
        supportText = R.string.models_explorer_collected_model_rename_description_text.resource(),
        errorText = R.string.models_explorer_collected_model_rename_error_text.resource(),
        onConfirmButtonClick = { newName -> (onSaveClick(newName, model)) },
        onDismissButtonClick = { onSaveClick(model.name, model) }
    )
}

@Composable
fun DeleteModelDialog(
    model: CollectedModel,
    onConfirmed: (CollectedModel?) -> Unit
) {
    TextDialog(
        icon = R.drawable.ic_delete,
        title = R.string.models_explorer_collected_model_delete_model.resource(),
        text = R.string.models_explorer_collected_model_delete_model_text.resource(model.name),
        confirmButtonText = R.string.general_yes.resource(),
        onConfirmButtonClick = { onConfirmed(model) },
        dismissButtonText = R.string.general_no.resource(),
        onDismissButtonClick = { onConfirmed(null) }
    )
}
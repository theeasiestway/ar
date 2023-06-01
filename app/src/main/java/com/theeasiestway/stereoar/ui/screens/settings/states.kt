package com.theeasiestway.stereoar.ui.screens.settings

import com.theeasiestway.domain.repositories.settings.models.AppTheme

data class State(
    val appTheme: AppTheme = AppTheme.SystemDefault,
    val collectDownloadedModel: Boolean = false,
    val saveLastVisitedFolder: Boolean = false,
    val keepScreenOn: Boolean = false
)

data class UiState(
    val appTheme: AppTheme = AppTheme.SystemDefault,
    val collectDownloadedModel: Boolean = false,
    val saveLastVisitedFolder: Boolean = false,
    val keepScreenOn: Boolean = false
)
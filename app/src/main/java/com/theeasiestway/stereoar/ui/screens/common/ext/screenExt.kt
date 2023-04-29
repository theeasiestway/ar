package com.theeasiestway.stereoar.ui.screens.common.ext

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun showSnackBar(
    coroutineScope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    message: String,
    duration: SnackbarDuration = SnackbarDuration.Short,
    onHide: (() -> Unit)? = null
) {
    coroutineScope.launch {
        snackBarHostState.showSnackbar(
            message = message,
            duration = duration
        )
        onHide?.invoke()
    }
}

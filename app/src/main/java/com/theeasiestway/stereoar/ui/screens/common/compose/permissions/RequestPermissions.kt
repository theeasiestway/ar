package com.theeasiestway.stereoar.ui.screens.common.compose.permissions

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissions(
    @DrawableRes icon: Int,
    permission: String,
    rationalTitle: String,
    rationalText: String,
    deniedTitle: String,
    deniedText: String,
    onResult: (isGranted: Boolean) -> Unit,
    onCloseApp: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var requestAnswered by remember { mutableStateOf(false) }
    val permissionState = rememberPermissionState(permission = permission) {
        Log.d("qdwddqw", "[old] received request result granted: $it")
        requestAnswered = true
    }
    var requestAttempt by remember { mutableStateOf(0) }
    if (requestAttempt > 0) {
        if (requestAnswered) {
            Log.d("qdwddqw", "[old] requestAttempt: $requestAttempt; requestAnswered")
            if (permissionState.status.isGranted) {
                onResult(true)
            } else if (permissionState.status.shouldShowRationale) {
                PermissionRationaleDialog(
                    icon = icon,
                    title = rationalTitle,
                    text = rationalText
                ) {
                    requestAttempt++
                }
            } else {
                PermissionDeniedDialog(
                    icon = icon,
                    title = deniedTitle,
                    text = deniedText,
                    onCloseApp = onCloseApp
                )
            }
        }

        LaunchedEffect(requestAttempt) {
            Log.d("qdwddqw", "[old] requestAttempt: $requestAttempt; check")
            permissionState.launchPermissionRequest()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                Log.d("qdwddqw", "[old] requestAttempt: $requestAttempt; ON_START")
                requestAttempt++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
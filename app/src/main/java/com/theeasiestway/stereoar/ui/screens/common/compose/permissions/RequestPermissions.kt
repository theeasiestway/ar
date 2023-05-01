package com.theeasiestway.stereoar.ui.screens.common.compose.permissions

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface Permission: Parcelable {
    fun toManifestString(): String

    @Parcelize
    object Camera: Permission {
        override fun toManifestString(): String {
            return android.Manifest.permission.CAMERA
        }
    }

    @Parcelize
    object ReadFiles: Permission {
        override fun toManifestString(): String {
            return android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}

enum class PermissionResult {
    Granted,
    DeniedForeverAndCanceled
}

@Parcelize
data class RequestPermissionResult(
    val permission: Permission,
    val result: PermissionResult
): Parcelable

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissions(
    @DrawableRes icon: Int,
    permission: String,
    rationalTitle: String,
    rationalText: String,
    deniedTitle: String,
    deniedText: String,
    deniedDismissButtonText: String,
    onResult: (PermissionResult) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var requestAnswered by remember { mutableStateOf(false) }
    val permissionState = rememberPermissionState(permission = permission) {
        requestAnswered = true
    }
    var requestAttempt by remember { mutableStateOf(0) }
    if (requestAttempt > 0) {
        if (requestAnswered) {
            if (permissionState.status.isGranted) {
                onResult(PermissionResult.Granted)
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
                    dismissButtonText = deniedDismissButtonText,
                    onCloseApp = {
                        onResult(PermissionResult.DeniedForeverAndCanceled)
                    }
                )
            }
        }

        LaunchedEffect(requestAttempt) {
            permissionState.launchPermissionRequest()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                requestAttempt++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
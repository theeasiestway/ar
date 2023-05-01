package com.theeasiestway.stereoar.ui.screens.common.compose.permissions

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable

@Composable
fun RequestCameraPermission(
    @DrawableRes icon: Int,
    rationalTitle: String,
    rationalText: String,
    deniedTitle: String,
    deniedText: String,
    deniedDismissButtonText: String,
    onResult: (PermissionResult) -> Unit
) {
    RequestPermissions(
        icon = icon,
        permission = Permission.Camera.toManifestString(),
        rationalTitle = rationalTitle,
        rationalText = rationalText,
        deniedTitle = deniedTitle,
        deniedText = deniedText,
        deniedDismissButtonText = deniedDismissButtonText,
        onResult = onResult
    )
}
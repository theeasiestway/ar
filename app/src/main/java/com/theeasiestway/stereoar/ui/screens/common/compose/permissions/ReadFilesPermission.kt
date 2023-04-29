package com.theeasiestway.stereoar.ui.screens.common.compose.permissions

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.theeasiestway.stereoar.BuildConfig

@Composable
fun ReadFilesPermission(
    @DrawableRes icon: Int,
    rationalTitle: String,
    rationalText: String,
    deniedTitle: String,
    deniedText: String,
    onResult: (PermissionResult) -> Unit
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        RequestPermissions(
            icon = icon,
            permission = android.Manifest.permission.READ_EXTERNAL_STORAGE,
            rationalTitle = rationalTitle,
            rationalText = rationalText,
            deniedTitle = deniedTitle,
            deniedText = deniedText,
            onResult = onResult
        )
    } else {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        var requestAttempt by remember { mutableStateOf(0) }
        if (requestAttempt > 0) {
            if (Environment.isExternalStorageManager()) {
                onResult(PermissionResult.Granted)
            } else {
                PermissionRationaleDialog(
                    icon = icon,
                    title = rationalTitle,
                    text = rationalText
                ) {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                        )
                    )
                }
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
}
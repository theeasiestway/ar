package com.theeasiestway.stereoar.ui.screens.common.compose.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.ui.screens.common.compose.dialogs.TextDialog
import com.theeasiestway.stereoar.ui.screens.common.ext.resource

/**
 * Created by Alexey Loboda on 27.04.2023
 */

@Composable
fun PermissionRationaleDialog(
    title: String,
    text: String,
    @DrawableRes icon: Int,
    onDialogClosed: () -> Unit
) {
    TextDialog(
        icon = icon,
        title = title,
        text = text,
        confirmButtonText = R.string.general_ok.resource(),
        onConfirmButtonClick = onDialogClosed
    )
}

@Composable
fun PermissionDeniedDialog(
    title: String,
    text: String,
    @DrawableRes icon: Int,
    onCloseApp: () -> Unit
) {
    val context = LocalContext.current
    val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    TextDialog(
        icon = icon,
        title = title,
        text = text,
        confirmButtonText = R.string.general_open_settings.resource(),
        dismissButtonText = R.string.general_close_app.resource(),
        onConfirmButtonClick = {
            context.startActivity(appSettingsIntent)
        },
        onDismissButtonClick = {
            onCloseApp()
        }
    )
}
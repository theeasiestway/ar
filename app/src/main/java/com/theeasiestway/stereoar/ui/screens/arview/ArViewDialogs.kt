package com.theeasiestway.stereoar.ui.screens.arview

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.ui.theme.Colors
import java.util.*

/**
 * Created by Alexey Loboda on 05.02.2022
 */

@Composable
fun CameraPermissionDialogWithExplanation(onOkClicked: () -> Unit) {
    /*var showDialog by remember { mutableStateOf(true) }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                *//* making it non-closable *//*
            },
            title = {
                Row(Modifier.fillMaxWidth()) {
                    Image(
                        modifier = Modifier.size(32.dp).align(Alignment.CenterVertically),
                        painter = painterResource(R.drawable.ic_camera),
                        colorFilter = ColorFilter.tint(Colors.primary_purple),
                        contentDescription = stringResource(R.string.permission_camera_request_title_text)
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.h5,
                        text = stringResource(R.string.permission_camera_request_title_text)
                    )
                }
            },
            text = {
                Text(
                    style = MaterialTheme.typography.body1,
                    text = stringResource(R.string.permission_camera_request_body_text)
                )
            },
            buttons = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        showDialog = false
                        onOkClicked()
                    }) {
                        Text(
                            style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                            text = stringResource(R.string.general_ok)
                        )
                    }
                }
            })
    }*/
}

@Composable
fun CameraPermissionDialogWithAppSettings() {
    /*val context = LocalContext.current
    val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    var showDialog by remember { mutableStateOf(true) }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                *//* making it non-closable *//*
            },
            title = {
                Row(Modifier.fillMaxWidth()) {
                    Image(
                        modifier = Modifier.size(32.dp).align(Alignment.CenterVertically),
                        painter = painterResource(R.drawable.ic_camera),
                        colorFilter = ColorFilter.tint(Colors.primary_purple),
                        contentDescription = stringResource(R.string.permission_camera_request_title_text)
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.h5,
                        text = stringResource(R.string.permission_camera_request_title_text)
                    )
                }
            },
            text = {
                Text(
                    style = MaterialTheme.typography.body1,
                    text = stringResource(R.string.permission_camera_request_denied_title_text)
                )
            },
            buttons = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        (context as Activity).finish()
                    }) {
                        Text(
                            style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                            text = stringResource(R.string.general_close_app)
                                .uppercase(Locale.getDefault())
                        )
                    }
                    TextButton(onClick = {
                        context.startActivity(appSettingsIntent)
                    }) {
                        Text(
                            style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                            text = stringResource(R.string.general_open_settings)
                                .uppercase(Locale.getDefault())
                        )
                    }
                }
            })
    }*/
}
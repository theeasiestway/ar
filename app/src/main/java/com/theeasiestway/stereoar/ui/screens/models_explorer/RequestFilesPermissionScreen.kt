package com.theeasiestway.stereoar.ui.screens.models_explorer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.ui.screens.common.compose.modifiers.shimmerEffect
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.Permission
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.PermissionResult
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.ReadFilesPermission
import com.theeasiestway.stereoar.ui.screens.common.compose.permissions.RequestPermissionResult
import com.theeasiestway.stereoar.ui.screens.common.ext.resource


@Destination
@Composable
fun RequestFilesPermissionScreen(
    navigator: ResultBackNavigator<RequestPermissionResult>
) {
    var permissionResult by remember { mutableStateOf<PermissionResult?>(null) }
    if (permissionResult == null) {
        ReadFilesPermission(
            icon = R.drawable.ic_folder,
            rationalTitle = R.string.permission_storage_rational_title.resource(),
            rationalText = R.string.permission_storage_rational_text.resource(),
            deniedTitle = R.string.permission_storage_rational_title.resource(),
            deniedText = R.string.permission_storage_denied_text.resource(),
            onResult = { result ->
                permissionResult = result // using variable because navigator.navigateBack() causes recomposition of lambda caller fun
            }
        )
    } else {
        navigator.navigateBack(
            RequestPermissionResult(
                permission = Permission.ReadFiles,
                result = permissionResult!!
            )
        )
    }
    ShimmeredFilesList()
}

@Composable
private fun ShimmeredFilesList() {
    val count = 40
    LazyColumn {
        repeat(count) { index ->
            item {
                ShimmeredFileListItem()
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
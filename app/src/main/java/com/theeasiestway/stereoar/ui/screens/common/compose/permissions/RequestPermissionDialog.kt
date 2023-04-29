package com.theeasiestway.stereoar.ui.screens.common.compose.permissions

import android.os.Parcelable
import androidx.compose.runtime.*
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.ui.screens.common.ext.resource
import kotlinx.parcelize.Parcelize


@Destination
@Composable
fun RequestPermissionDialog(
    input: RequestPermissionInput,
    navigator: ResultBackNavigator<RequestPermissionResult>
) {
    var permissionResult by remember { mutableStateOf<PermissionResult?>(null) }
    if (permissionResult == null) {
        when (input.permission) {
            Permission.Camera -> {
                TODO()
            }
            Permission.ReadFiles -> {
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
            }
        }
    } else {
        navigator.navigateBack(
            RequestPermissionResult(
                permission = input.permission,
                result = permissionResult!!
            )
        )
    }
}

enum class Permission {
    Camera,
    ReadFiles
}

@Parcelize
data class RequestPermissionInput(
    val permission: Permission
): Parcelable

@Parcelize
data class RequestPermissionResult(
    val permission: Permission,
    val result: PermissionResult
): Parcelable
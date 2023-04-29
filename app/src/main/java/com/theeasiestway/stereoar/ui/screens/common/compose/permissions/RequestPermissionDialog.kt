package com.theeasiestway.stereoar.ui.screens.common.compose.permissions

import android.os.Parcelable
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.ui.screens.common.ext.resource
import kotlinx.parcelize.Parcelize


@Destination(
    style = DestinationStyle.Dialog::class
)
@Composable
fun RequestPermissionDialog(
    input: RequestPermissionInput,
    navigator: ResultBackNavigator<RequestPermissionResult>
) {
    when(input.permission) {
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
                onResult = { isGranted ->
                    navigator.navigateBack(
                        RequestPermissionResult(
                            permission = input.permission,
                            isGranted = isGranted
                        )
                    )
                },
                onCloseApp = {

                }
            )
        }
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
    val isGranted: Boolean
): Parcelable
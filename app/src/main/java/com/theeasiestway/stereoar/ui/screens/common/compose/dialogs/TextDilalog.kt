package com.theeasiestway.stereoar.ui.screens.common.compose.dialogs

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theeasiestway.stereoar.ui.screens.common.compose.images.ImageDrawable
import com.theeasiestway.stereoar.ui.screens.common.compose.text.BodyMedium
import com.theeasiestway.stereoar.ui.screens.common.compose.text.HeadlineSmall
import com.theeasiestway.stereoar.ui.screens.common.compose.text.LabelLarge
import com.theeasiestway.stereoar.ui.theme.AppTheme

@Composable
fun TextDialog(
    @DrawableRes icon: Int,
    title: String,
    text: String,
    confirmButtonText: String,
    dismissButtonText: String? = null,
    onConfirmButtonClick: () -> Unit,
    onDismissButtonClick: (() -> Unit)? = null,
    dismissByClickOutside: Boolean = false,
    onDialogClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        containerColor = AppTheme.colors.surface,
        shape = RoundedCornerShape(AppTheme.shapes.extraLarge),
        onDismissRequest = {
            if (dismissByClickOutside) onDialogClose()
        },
        icon = {
            ImageDrawable(
                modifier = Modifier.size(32.dp),
                resId = icon,
                tint = AppTheme.colors.accent
            )
        },
        title = {
            HeadlineSmall(text = title)
        },
        text = {
            BodyMedium(text = text)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmButtonClick()
                }
            ) {
                LabelLarge(text = confirmButtonText)
            }
        },
        dismissButton = {
            dismissButtonText?.let { text ->
                TextButton(
                    onClick = {
                        onDismissButtonClick?.invoke()
                    }
                ) {
                    LabelLarge(text = text)
                }
            }
        }
    )
}
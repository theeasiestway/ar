package com.theeasiestway.stereoar.ui.screens.common.compose.dialogs

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.ui.screens.common.compose.images.ImageDrawable
import com.theeasiestway.stereoar.ui.screens.common.compose.input.InputField
import com.theeasiestway.stereoar.ui.screens.common.compose.text.HeadlineSmall
import com.theeasiestway.stereoar.ui.screens.common.compose.text.LabelLarge
import com.theeasiestway.stereoar.ui.theme.AppTheme
import java.util.regex.Pattern

@Composable
fun InputDialog(
    @DrawableRes icon: Int,
    title: String,
    text: String = "",
    validationRegex: String? = null,
    label: String? = null,
    supportText: String? = null,
    errorText: String? = null,
    confirmButtonText: String,
    dismissButtonText: String? = null,
    modifier: Modifier = Modifier,
    onConfirmButtonClick: (String) -> Unit,
    onDismissButtonClick: (() -> Unit)? = null,
    dismissByClickOutside: Boolean = false,
    onDialogClose: () -> Unit = {},
) {
    var textInternal by remember { mutableStateOf(text) }
    var textChanged by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val validator = if (validationRegex != null) { remember(validationRegex) { Pattern.compile(validationRegex) } } else null
    val isValidInput = remember(textInternal, validator) {
        if (textChanged) {
            validator?.matcher(textInternal)?.matches() ?: true
        } else true
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
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
            InputField(
                modifier = Modifier.focusRequester(focusRequester),
                text = textInternal,
                label = label,
                isError = !isValidInput,
                placeholder = supportText,
                supportText = if (!isValidInput) errorText else "",
                onTextChange = { changedText ->
                    textChanged = true
                    textInternal = changedText
                }
            )
        },
        confirmButton = {
            TextButton(
                enabled = isValidInput && textChanged,
                onClick = {
                    onConfirmButtonClick(textInternal)
                }
            ) {
                LabelLarge(
                    text = confirmButtonText,
                    color = if (isValidInput && textChanged) AppTheme.colors.primaryText
                    else AppTheme.colors.divider
                )
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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InputDialogPreview() {
    InputDialog(
        icon = R.drawable.ic_camera,
        title = "Title",
        text = "Text",
        confirmButtonText = "OK",
        dismissButtonText = "Cancel",
        onConfirmButtonClick = {}
    )
}
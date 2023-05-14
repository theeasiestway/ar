package com.theeasiestway.stereoar.ui.screens.common.compose.input

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.ui.screens.common.compose.images.ImageDrawable
import com.theeasiestway.stereoar.ui.theme.AppTheme

@Composable
fun InputField(
    text: String = "",
    isError: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    label: String? = null,
    placeholder: String? = null,
    supportText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onTextChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = text,
        isError = isError,
        enabled = enabled,
        singleLine = singleLine,
        onValueChange = onTextChange,
        label = {
            label?.let {
                Text(
                    text = label,
                    color = AppTheme.colors.primaryText
                )
            }
        },
        placeholder = {
            placeholder?.let {
                Text(
                    text = placeholder,
                    color = AppTheme.colors.primaryText
                )
            }
        },
        supportingText =  {
            supportText?.let {
                Text(
                    text = supportText,
                    color = AppTheme.colors.primaryText
                )
            }
        },
        leadingIcon = leadingIcon,
        trailingIcon = {
            if (text.isNotEmpty()) {
                ImageDrawable(
                    resId = R.drawable.ic_clear,
                    tint = AppTheme.colors.primaryText,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable {
                            onTextChange("")
                        }
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppTheme.colors.primaryText,
            unfocusedTextColor = AppTheme.colors.divider,
            focusedBorderColor = AppTheme.colors.accent,
            unfocusedBorderColor = AppTheme.colors.primaryText
        )
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InputFieldPreview() {
    AppTheme {
        var text by remember { mutableStateOf("text") }
        Column {
            InputField(
                text = text,
                label = "label",
                placeholder = "placeholder",
                supportText = "support text",
                isError = false,
                onTextChange = { text = it }
            )
            InputField(
                text = text,
                label = "label",
                placeholder = "placeholder",
                supportText = "support text",
                isError = true,
                onTextChange = { text = it }
            )
        }
    }
}
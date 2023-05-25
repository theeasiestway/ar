package com.theeasiestway.stereoar.ui.screens.common.compose.buttons

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.theeasiestway.stereoar.ui.screens.common.compose.text.LabelLarge
import com.theeasiestway.stereoar.ui.theme.AppTheme

@Composable
fun OutlinedButton(
    modifier: Modifier = Modifier,
    title: String,
    titleColor: Color = AppTheme.colors.primaryText,
    outlineColor: Color = AppTheme.colors.accent,
    onClick: () -> Unit
) {
    androidx.compose.material3.OutlinedButton(
        modifier = modifier,
        border = BorderStroke(1.dp, outlineColor),
        onClick = onClick,
    ) {
        LabelLarge(
            text = title,
            color = titleColor
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OutlinedButtonPreview() {
    AppTheme {
        OutlinedButton(
            title = "OutlinedButton"
        ) {}
    }
}
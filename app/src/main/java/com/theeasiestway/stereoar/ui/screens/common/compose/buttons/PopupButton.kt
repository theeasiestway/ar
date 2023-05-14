package com.theeasiestway.stereoar.ui.screens.common.compose.buttons

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.theeasiestway.stereoar.ui.screens.common.compose.text.BodyMedium
import com.theeasiestway.stereoar.ui.theme.AppTheme

@Composable
fun PopupButton(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: () -> Unit
) {
    Box(modifier = modifier
        .background(AppTheme.colors.surface)
        .clickable(onClick = onClick)
        .padding(16.dp)
    ) {
        BodyMedium(text = text)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PopupButtonPreview() {
    AppTheme {
        PopupButton(text = "Popup button text") {}
    }
}
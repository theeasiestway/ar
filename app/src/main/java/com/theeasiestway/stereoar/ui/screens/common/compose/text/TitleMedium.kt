package com.theeasiestway.stereoar.ui.screens.common.compose.text

import android.content.res.Configuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.theeasiestway.stereoar.ui.theme.AppTheme

@Composable
fun TitleMedium(
    text: String,
    textAlign: TextAlign? = null,
    color: Color = AppTheme.colors.primaryText,
    maxLines: Int = Int.MAX_VALUE,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        textAlign = textAlign,
        color = color,
        style = AppTheme.typography.titleMedium,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TitleMediumPreview() {
    AppTheme {
        TitleMedium(text = "TitleMedium")
    }
}
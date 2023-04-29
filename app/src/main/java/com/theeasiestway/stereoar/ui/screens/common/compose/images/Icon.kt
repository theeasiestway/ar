package com.theeasiestway.stereoar.ui.screens.common.compose.images

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.ui.theme.AppTheme

@Composable
fun ImageDrawable(
    @DrawableRes resId: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier,
        painter = painterResource(id = resId),
        colorFilter = ColorFilter.tint(tint),
        contentDescription = null
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ImageDrawablePreview() {
    AppTheme {
        ImageDrawable(
            resId = R.drawable.ic_sd_card,
            tint = AppTheme.colors.accent,
            modifier = Modifier.size(32.dp)
        )
    }
}
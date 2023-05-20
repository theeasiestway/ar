package com.theeasiestway.stereoar.ui.screens.common.compose.buttons

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.theeasiestway.stereoar.R
import com.theeasiestway.stereoar.ui.screens.common.compose.images.ImageDrawable
import com.theeasiestway.stereoar.ui.theme.AppTheme

@Composable
fun ImageButton(
    @DrawableRes icon: Int,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        ImageDrawable(
            resId = icon,
            tint = tint
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ImageButtonPreview() {
    AppTheme {
        ImageButton(
            icon = R.drawable.ic_more,
            tint = AppTheme.colors.white
        ) {}
    }
}
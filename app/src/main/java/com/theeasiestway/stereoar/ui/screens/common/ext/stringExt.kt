package com.theeasiestway.stereoar.ui.screens.common.ext

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun @receiver:StringRes Int.resource(): String =
    resource(
        formatArgs = emptyArray(),
    )

@Composable
fun @receiver:StringRes Int.resource(vararg formatArgs: Any): String =
    stringResource(
        id = this,
        formatArgs = formatArgs,
    )
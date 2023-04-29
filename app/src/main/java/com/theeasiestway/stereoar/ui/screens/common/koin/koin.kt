package com.theeasiestway.stereoar.ui.screens.common.koin

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.getKoin
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named

@Composable
fun createScopeIfNull(scopeId: String) {
    if (getKoin().getScopeOrNull(scopeId) == null) {
        getKoin().createScope(scopeId, named(scopeId))
    }
}

fun KoinComponent.closeScope(scopeId: String) {
    getKoin().getScopeOrNull(scopeId)?.close()
}
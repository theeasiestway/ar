package com.theeasiestway.stereoar.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect

val <S : Any, SE : Any> ContainerHost<S, SE>.state: StateFlow<S>
    get() = container.stateFlow

val <S : Any, SE : Any> ContainerHost<S, SE>.sideEffects: Flow<SE>
    get() = container.sideEffectFlow

fun <STATE : Any, EFFECT : Any> ContainerHost<STATE, EFFECT>.postSideEffect(effect: EFFECT) {
    intent {
        postSideEffect(effect)
    }
}

@Composable
fun <S : Any, E : Any> ContainerHost<S, E>.onSideEffect(
    onEffect: CoroutineScope.(E) -> Unit,
) {
    SideEffectCollector(
        host = this,
        onEffect = onEffect,
    )
}

@Composable
fun <S : Any, E : Any> SideEffectCollector(
    host: ContainerHost<S, E>,
    onEffect: CoroutineScope.(E) -> Unit,
) {
    LaunchedEffect(host) {
        host.sideEffects
            .collect {
                onEffect(it)
            }
    }
}
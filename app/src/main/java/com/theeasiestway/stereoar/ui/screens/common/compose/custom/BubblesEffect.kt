package com.theeasiestway.stereoar.ui.screens.common.compose.custom

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.theeasiestway.stereoar.ui.screens.common.compose.palette.MaterialPalette500
import com.theeasiestway.stereoar.ui.theme.AppTheme
import kotlin.math.sqrt


data class BubbleOrigin(
    val x: Float,
    val y: Float,
    val radius: Float
) {
    fun toOffset(): Offset {
        return Offset(x = x, y = y)
    }

    fun touches(otherX: Float, otherY: Float, otherRadius: Float): Boolean {
        val distance = sqrt((x - otherX) * (x - otherX) + (y - otherY) * (y - otherY))
        val intersect = distance < radius + otherRadius
        val touch = distance == radius + otherRadius
        val thisInsideOther = distance <= otherRadius - radius
        val otherInsideThis = distance <= radius - otherRadius
        return intersect || touch || thisInsideOther || otherInsideThis
    }
}

@Composable
fun BubblesEffect(
    bubblesCount: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val originsSet = remember { mutableListOf<BubbleOrigin>() }
        val size = with(LocalDensity.current) { Size(maxWidth.toPx(), maxHeight.toPx()) }
        repeat(bubblesCount) {
            RandomBubble(
                size = size,
                existsOrigins = originsSet,
                onCreate = { origin ->
                    originsSet.add(origin)
                },
                onDestroy = { origin ->
                    originsSet.remove(origin)
                }
            )
        }
    }
}

@Preview
@Composable
private fun BubblesEffectPreview() {
    AppTheme {
        BubblesEffect(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            bubblesCount = 500
        )
    }
}

private fun Size.randomPoint(radius: Float, exceptOrigins: List<BubbleOrigin> = emptyList()): BubbleOrigin {
    var count = 0
    var radiusScale = 1f
    while(count < 1000 && radiusScale > 0f) {
        count++
        val r = (radius * radiusScale)
        val x = (r.toInt()..(width.toInt() - r.toInt())).random().toFloat()
        val y = (r.toInt()..(height.toInt() - r.toInt())).random().toFloat()
        if (!exceptOrigins.any { origin ->
                origin.touches(
                    otherX = x,
                    otherY = y,
                    otherRadius = r
                )
            }
        ) {
            return BubbleOrigin(
                x = x,
                y = y,
                radius = r
            )
        }
        radiusScale -= 0.01f
    }
    return BubbleOrigin(
        x = 0f,
        y = 0f,
        radius = 0f
    )
}

@Composable
private fun RandomBubble(
    size: Size,
    existsOrigins: List<BubbleOrigin>,
    onCreate: (BubbleOrigin) -> Unit,
    onDestroy: (BubbleOrigin) -> Unit
) {
    val radius = (30..120).random().toFloat()
    val strokeWidth = (2..15).random().toFloat()
    val origin = size.randomPoint(radius = radius + strokeWidth, exceptOrigins = existsOrigins)
    val duration = (2000..6000).random()
    val color = MaterialPalette500.randomColor()
    var change by remember { mutableStateOf(false) }

    key(change) {
        Bubble(
            color = color,
            origin = origin,
            duration = duration,
            strokeWidth = strokeWidth,
            onCreate = onCreate,
            onAnimationEnd = { endedOrigin ->
                onDestroy(endedOrigin)
                change = !change
            }
        )
    }
}

@Composable
fun BubbleRepeatable(
    color: Color,
    origin: BubbleOrigin,
    duration: Int,
    strokeWidth: Float
) {
    Bubble(
        color = color,
        origin = origin,
        duration = duration,
        strokeWidth = strokeWidth,
        repeatable = true
    )
}

@Composable
fun Bubble(
    color: Color,
    origin: BubbleOrigin,
    duration: Int,
    strokeWidth: Float,
    onCreate: (BubbleOrigin) -> Unit = {},
    onAnimationEnd: (BubbleOrigin) -> Unit = {}
) {
    Bubble(
        color = color,
        origin = origin,
        duration = duration,
        strokeWidth = strokeWidth,
        repeatable = false,
        onCreate = onCreate,
        onAnimationEnd = onAnimationEnd
    )
}

@Composable
private fun Bubble(
    color: Color,
    origin: BubbleOrigin,
    duration: Int,
    strokeWidth: Float,
    repeatable: Boolean,
    onCreate: (BubbleOrigin) -> Unit = {},
    onAnimationEnd: (BubbleOrigin) -> Unit = {}
) {
    val stroke = remember(strokeWidth) { Stroke(width = strokeWidth) }
    val center = remember(origin.x, origin.y) { origin.toOffset() }
    val radiusAnimation = remember(origin.radius) { Animatable(0f) }
    val alphaAnimation = remember(origin.radius) { Animatable(1f) }
    val animationSpec = remember(duration) {
        if (repeatable) {
            infiniteRepeatable<Float>(
                animation = tween(
                    durationMillis = duration,
                    easing = LinearEasing
                )
            )
        } else {
            tween(
                durationMillis = duration,
                easing = LinearEasing
            )
        }
    }
    var create by remember { mutableStateOf(true) }
    LaunchedEffect(radiusAnimation) {
        radiusAnimation.animateTo(
            targetValue = origin.radius,
            animationSpec = animationSpec
        ) {
            if (value == 0f && create) {
                onCreate(origin)
                create = false
            } else if (value == targetValue && !create) {
                onAnimationEnd(origin)
                create = true
            }
        }
    }
    LaunchedEffect(alphaAnimation) {
        alphaAnimation.animateTo(
            targetValue = 0f,
            animationSpec = animationSpec
        )
    }

    Canvas(modifier = Modifier.wrapContentSize()) {
        drawCircle(
            color = color,
            center = center,
            radius = radiusAnimation.value,
            alpha = alphaAnimation.value,
            style = stroke
        )
    }
}
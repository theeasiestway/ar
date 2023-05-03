package com.theeasiestway.stereoar.ui.screens.common.compose.custom

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    fun touches(other: BubbleOrigin): Boolean {
        val distance = sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y))
        val intersect = distance < radius + other.radius
        val touch = distance == radius + other.radius
        val thisInsideOther = distance <= other.radius - radius
        val otherInsideThis = distance <= radius - other.radius
        return intersect || touch || thisInsideOther || otherInsideThis
    }
}

@Composable
fun BubblesEffect(
    modifier: Modifier = Modifier
) {
    BoxWithConstraints {
        val originsSet = remember { mutableSetOf<BubbleOrigin>() }
        val size = with(LocalDensity.current) { Size(maxWidth.toPx(), maxHeight.toPx()) }
        val radius = (20..100).random().toFloat()
        val duration = (1000..5000).random()
        val strokeWidth = (2..15).random().toFloat()
        val origin = size.randomPoint(radius = radius, exceptOrigins = originsSet)
        val color = MaterialPalette500.randomColor()
        var destroyed by remember { mutableStateOf(false) }
        originsSet.add(origin)
        Box(modifier = Modifier
            .size(50.dp)
            .background(color)
        )
        Log.d("wqdqwdwqd", "[1] set: $originsSet; duration: $duration")

        LaunchedEffect(destroyed) {
            destroyed = false
            Log.d("wqdqwdwqd", "[3] set: $originsSet; duration: $duration")
        }

        Bubble(
            color = color,
            origin = origin,
            duration = duration,
            strokeWidth = strokeWidth,
            onAnimationEnd = {
                originsSet.remove(it)
                destroyed = true
                Log.d("wqdqwdwqd", "[2] set: $originsSet; duration: $duration")
            }
        )
    }
}

@Preview
@Composable
private fun BubblesEffectPreview() {
    AppTheme {
        BubblesEffect(modifier = Modifier.fillMaxSize())
    }
}

private fun Size.randomPoint(radius: Float, exceptOrigins: Set<BubbleOrigin> = emptySet()): BubbleOrigin {
    while(true) {
        val newOrigin = BubbleOrigin(
            x = (radius.toInt()..(width.toInt() - radius.toInt())).random().toFloat(),
            y = (radius.toInt()..(height.toInt() - radius.toInt())).random().toFloat(),
            radius = radius
        )

        if (!exceptOrigins.any { origin -> origin.touches(newOrigin) }) {
            return newOrigin
        }
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
        origin,
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
    onAnimationEnd: (BubbleOrigin) -> Unit = {}
) {
    Bubble(
        color = color,
        origin = origin,
        duration = duration,
        strokeWidth = strokeWidth,
        repeatable = false,
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
    onAnimationEnd: (BubbleOrigin) -> Unit = {}
) {
    val stroke = remember(strokeWidth) { Stroke(width = strokeWidth) }
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
    LaunchedEffect(radiusAnimation) {
        val result = radiusAnimation.animateTo(
            targetValue = origin.radius,
            animationSpec = animationSpec
        )
        if (!repeatable && result.endReason == AnimationEndReason.Finished) {
            onAnimationEnd(origin)
        }
    }
    LaunchedEffect(alphaAnimation) {
        alphaAnimation.animateTo(
            targetValue = 0f,
            animationSpec = animationSpec
        )
    }

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawCircle(
            color = color,
            center = origin.toOffset(),
            radius = radiusAnimation.value,
            alpha = alphaAnimation.value,
            style = stroke
        )
    }
}
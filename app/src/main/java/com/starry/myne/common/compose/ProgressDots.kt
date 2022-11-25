package com.starry.myne.common.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Preview
@Composable
fun ProgressDots(
    modifier: Modifier = Modifier.padding(4.dp),
    color: Color = MaterialTheme.colorScheme.primary
) {

    val dots = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) },
    )

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * 100L)
            animatable.animateTo(
                targetValue = 1f, animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 2000
                        0.0f at 0 with LinearOutSlowInEasing
                        1.0f at 200 with LinearOutSlowInEasing
                        0.0f at 400 with LinearOutSlowInEasing
                        0.0f at 2000
                    },
                    repeatMode = RepeatMode.Restart,
                )
            )
        }
    }

    val dys = dots.map { it.value }

    val travelDistance = with(LocalDensity.current) { 15.dp.toPx() }

    Row(modifier) {
        dys.forEachIndexed { _, dy ->
            Box(
                Modifier
                    .size(25.dp)
                    .graphicsLayer {
                        translationY = -dy * travelDistance
                    },
            ) {
                Row(modifier) {
                    dots.forEachIndexed { index, _ ->
                        Box(
                            Modifier.size(25.dp)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color = color, shape = CircleShape)
                            )
                        }

                        if (index != dys.size - 1) {
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                    }
                }
            }
        }

    }
}


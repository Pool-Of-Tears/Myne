package com.starry.myne.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay

@Composable
fun SlideInAnimatedContainer(
    initialDelay: Long, content:
    @Composable () -> Unit
) {
    val showContent = remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        delay(initialDelay)
        showContent.value = true
    }

    AnimatedVisibility(
        visible = showContent.value,
        enter = slideInVertically { it / 2 } + expandVertically(expandFrom = Alignment.Top) + fadeIn(
            initialAlpha = 0.3f
        ),
        exit = slideOutVertically() + shrinkVertically() + fadeOut(),
    ) {
        content()
    }
}
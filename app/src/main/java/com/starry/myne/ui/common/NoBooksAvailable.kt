package com.starry.myne.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.starry.myne.R
import com.starry.myne.ui.theme.figeronaFont

@Composable
fun NoBooksAvailable(text: String) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 70.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val compositionResult: LottieCompositionResult =
            rememberLottieComposition(
                spec = LottieCompositionSpec.RawRes(R.raw.no_books_available_lottie)
            )
        val progressAnimation by animateLottieCompositionAsState(
            compositionResult.value,
            isPlaying = true,
            iterations = LottieConstants.IterateForever,
            speed = 1f
        )

        Spacer(modifier = Modifier.weight(1f))

        LottieAnimation(
            composition = compositionResult.value,
            progress = progressAnimation,
            modifier = Modifier.size(280.dp),
            enableMergePaths = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(0.8f),
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = figeronaFont,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.weight(1.68f))
    }
}
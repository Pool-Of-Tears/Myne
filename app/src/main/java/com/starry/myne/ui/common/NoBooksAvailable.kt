/**
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.starry.myne.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
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

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.padding(horizontal = 14.dp),
                text = text,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = figeronaFont,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.weight(1.68f))
    }
}
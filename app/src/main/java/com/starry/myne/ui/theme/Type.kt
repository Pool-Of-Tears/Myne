/*
Copyright 2022 - 2023 Stɑrry Shivɑm

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.starry.myne.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.starry.myne.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
titleLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
),
labelSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)
*/
)

val figeronaFont = FontFamily(
    fonts = listOf(
        Font(
            resId = R.font.figerona_black,
            weight = FontWeight.Black
        ),
        Font(
            resId = R.font.figerona_bold,
            weight = FontWeight.Bold
        ),
        Font(
            resId = R.font.figerona_extrabold,
            weight = FontWeight.ExtraBold
        ),
        Font(
            resId = R.font.figerona_extralight,
            weight = FontWeight.ExtraLight
        ),
        Font(
            resId = R.font.figerona_light,
            weight = FontWeight.Light
        ),
        Font(
            resId = R.font.figerona_medium,
            weight = FontWeight.Medium
        ),
        Font(
            resId = R.font.figerona_regular,
            weight = FontWeight.Normal
        ),
        Font(
            resId = R.font.figerona_semibold,
            weight = FontWeight.SemiBold
        ),
        Font(
            resId = R.font.figerona_thin,
            weight = FontWeight.Thin
        ),
    )
)

val pacificoFont = FontFamily(Font(R.font.pacifico_regular))

val PTSeriFont = FontFamily(Font(R.font.pt_serif_regular))
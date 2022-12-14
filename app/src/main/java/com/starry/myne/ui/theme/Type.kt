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
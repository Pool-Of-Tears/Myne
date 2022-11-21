package com.starry.myne

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.starry.myne.ui.screens.MainScreen
import com.starry.myne.ui.theme.MyneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyneTheme {
                MainScreen()
            }
        }
    }
}
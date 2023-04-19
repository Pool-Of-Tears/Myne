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

package com.starry.myne

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import coil.annotation.ExperimentalCoilApi
import com.starry.myne.others.NetworkObserver
import com.starry.myne.ui.screens.main.MainScreen
import com.starry.myne.ui.screens.settings.viewmodels.SettingsViewModel
import com.starry.myne.ui.screens.settings.viewmodels.ThemeMode
import com.starry.myne.ui.theme.MyneTheme
import com.starry.myne.utils.PreferenceUtils
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalMaterialApi
@ExperimentalCoilApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var networkObserver: NetworkObserver
    lateinit var settingsViewModel: SettingsViewModel
    private lateinit var mainViewModel: MainViewModel

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreferenceUtils.initialize(this)
        networkObserver = NetworkObserver(applicationContext)
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        when (PreferenceUtils.getInt(PreferenceUtils.APP_THEME_INT, ThemeMode.Auto.ordinal)) {
            ThemeMode.Auto.ordinal -> settingsViewModel.setTheme(ThemeMode.Auto)
            ThemeMode.Dark.ordinal -> settingsViewModel.setTheme(ThemeMode.Dark)
            ThemeMode.Light.ordinal -> settingsViewModel.setTheme(ThemeMode.Light)
        }

        settingsViewModel.setMaterialYou(
            PreferenceUtils.getBoolean(
                PreferenceUtils.MATERIAL_YOU_BOOL, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            )
        )

        // Install splash screen before setting content.
        installSplashScreen().setKeepOnScreenCondition {
            mainViewModel.isLoading.value
        }

        setContent {
            MyneTheme(settingsViewModel = settingsViewModel) {
                val status by networkObserver.observe().collectAsState(
                    initial = NetworkObserver.Status.Unavailable
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val startDestination by mainViewModel.startDestination
                    MainScreen(
                        startDestination = startDestination,
                        networkStatus = status,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
        checkStoragePermission()
    }

    fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity::Storage", "Permission is granted"); true
            } else {
                Log.d("MainActivity::Storage", "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this, arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE), 1
                ); false
            }
        } else {
            true
        }
    }
}
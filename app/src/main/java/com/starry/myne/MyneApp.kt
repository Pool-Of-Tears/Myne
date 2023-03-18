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

import android.app.Application
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.ExperimentalComposeUiApi
import cat.ereza.customactivityoncrash.config.CaocConfig
import coil.annotation.ExperimentalCoilApi
import dagger.hilt.android.HiltAndroidApp


@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@HiltAndroidApp
class MyneApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CaocConfig.Builder.create().restartActivity(MainActivity::class.java).apply()
    }
}
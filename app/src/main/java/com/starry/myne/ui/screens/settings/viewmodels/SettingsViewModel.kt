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

package com.starry.myne.ui.screens.settings.viewmodels

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.starry.myne.helpers.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class ThemeMode {
    Light, Dark, Auto
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceUtil: PreferenceUtil
) : ViewModel() {

    private val _theme = MutableLiveData(ThemeMode.Auto)
    private val _amoledTheme = MutableLiveData(false)
    private val _materialYou = MutableLiveData(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    private val _internalReader = MutableLiveData(true)
    private val _useGoogleApi = MutableLiveData(true)
    private val _openLibraryAtStart = MutableLiveData(false)

    val theme: LiveData<ThemeMode> = _theme
    val amoledTheme: LiveData<Boolean> = _amoledTheme
    val materialYou: LiveData<Boolean> = _materialYou
    val internalReader: LiveData<Boolean> = _internalReader
    val useGoogleApi: LiveData<Boolean> = _useGoogleApi
    val openLibraryAtStart: LiveData<Boolean> = _openLibraryAtStart

    init {
        _theme.value = ThemeMode.entries.toTypedArray()[getThemeValue()]
        _amoledTheme.value = getAmoledThemeValue()
        _materialYou.value = getMaterialYouValue()
        _internalReader.value = getInternalReaderValue()
        _useGoogleApi.value = getUseGoogleApiValue()
        _openLibraryAtStart.value = getOpenLibraryAtStartValue()
    }

    // Getters =============================================================================

    fun setTheme(newTheme: ThemeMode) {
        _theme.postValue(newTheme)
        preferenceUtil.putInt(PreferenceUtil.APP_THEME_INT, newTheme.ordinal)
    }

    fun setAmoledTheme(newValue: Boolean) {
        _amoledTheme.postValue(newValue)
        preferenceUtil.putBoolean(PreferenceUtil.AMOLED_THEME_BOOL, newValue)
    }

    fun setMaterialYou(newValue: Boolean) {
        _materialYou.postValue(newValue)
        preferenceUtil.putBoolean(PreferenceUtil.MATERIAL_YOU_BOOL, newValue)
    }

    fun setInternalReaderValue(newValue: Boolean) {
        _internalReader.postValue(newValue)
        preferenceUtil.putBoolean(PreferenceUtil.INTERNAL_READER_BOOL, newValue)
    }

    fun setUseGoogleApiValue(newValue: Boolean) {
        _useGoogleApi.postValue(newValue)
        preferenceUtil.putBoolean(PreferenceUtil.USE_GOOGLE_API_BOOL, newValue)
    }

    fun setOpenLibraryAtStartValue(newValue: Boolean) {
        _openLibraryAtStart.postValue(newValue)
        preferenceUtil.putBoolean(PreferenceUtil.OPEN_LIBRARY_AT_START_BOOL, newValue)
    }

    // Getters ============================================================================
    // Used only during initialization except getCurrentTheme()
    private fun getThemeValue() = preferenceUtil.getInt(
        PreferenceUtil.APP_THEME_INT, ThemeMode.Auto.ordinal
    )

    private fun getAmoledThemeValue() = preferenceUtil.getBoolean(
        PreferenceUtil.AMOLED_THEME_BOOL, false
    )

    private fun getMaterialYouValue() = preferenceUtil.getBoolean(
        PreferenceUtil.MATERIAL_YOU_BOOL, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    )

    private fun getInternalReaderValue() = preferenceUtil.getBoolean(
        PreferenceUtil.INTERNAL_READER_BOOL, true
    )

    private fun getUseGoogleApiValue() = preferenceUtil.getBoolean(
        PreferenceUtil.USE_GOOGLE_API_BOOL, true
    )

    private fun getOpenLibraryAtStartValue() = preferenceUtil.getBoolean(
        PreferenceUtil.OPEN_LIBRARY_AT_START_BOOL, false
    )

    @Composable
    fun getCurrentTheme(): ThemeMode {
        return if (theme.value == ThemeMode.Auto) {
            if (isSystemInDarkTheme()) ThemeMode.Dark else ThemeMode.Light
        } else theme.value!!
    }
}
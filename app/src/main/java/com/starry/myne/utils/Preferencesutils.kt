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

package com.starry.myne.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtil {
    private lateinit var prefs: SharedPreferences
    private const val PREFS_NAME = "myne_settings"

    // Preference keys
    const val APP_THEME_INT = "theme_settings"
    const val MATERIAL_YOU_BOOL = "material_you"
    const val INTERNAL_READER_BOOL = "internal_reader"
    const val READER_FONT_SIZE_INT = "reader_font_size"
    const val READER_FONT_STYLE_STR = "reader_font_style"

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun keyExists(key: String): Boolean {
        if (prefs.contains(key))
            return true
        return false
    }

    fun putString(key: String, value: String) {
        val prefsEditor = prefs.edit()
        prefsEditor.putString(key, value)
        prefsEditor.apply()
    }

    fun putInt(key: String, value: Int) {
        val prefsEditor = prefs.edit()
        prefsEditor.putInt(key, value)
        prefsEditor.apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        val prefsEditor = prefs.edit()
        prefsEditor.putBoolean(key, value)
        prefsEditor.apply()
    }

    fun getString(key: String, value: String): String? {
        return prefs.getString(key, value)
    }

    fun getInt(key: String, value: Int): Int {
        return prefs.getInt(key, value)
    }

    fun getBoolean(key: String, value: Boolean): Boolean {
        return prefs.getBoolean(key, value)
    }
}
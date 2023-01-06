package com.starry.myne.ui.viewmodels

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class ThemeMode {
    Light, Dark, Auto
}

class ThemeViewModel : ViewModel() {
    private val _theme = MutableLiveData(ThemeMode.Auto)
    private val _materialYou = MutableLiveData(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

    val theme: LiveData<ThemeMode> = _theme
    val materialYou: LiveData<Boolean> = _materialYou

    fun setTheme(newTheme: ThemeMode) {
        _theme.postValue(newTheme)
    }

    fun setMaterialYou(newValue: Boolean) {
        _materialYou.postValue(newValue)
    }
}
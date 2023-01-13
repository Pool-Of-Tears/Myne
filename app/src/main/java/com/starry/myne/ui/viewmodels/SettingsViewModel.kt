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

package com.starry.myne.ui.viewmodels

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.annotation.ExperimentalCoilApi
import com.starry.myne.BuildConfig
import com.starry.myne.MainActivity
import com.starry.myne.R
import com.starry.myne.utils.toToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

enum class ThemeMode {
    Light, Dark, Auto
}

class SettingsViewModel : ViewModel() {

    private val okHttpClient = OkHttpClient()

    companion object {
        private const val GITHUB_RELEASES_LINK =
            "https://api.github.com/repos/pool-of-tears/myne/releases"
    }

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

    @Composable
    fun getCurrentTheme(): ThemeMode {
        return if (theme.value == ThemeMode.Auto) {
            if (isSystemInDarkTheme()) ThemeMode.Dark else ThemeMode.Light
        } else theme.value!!
    }

    fun checkForUpdates(onResult: (isUpdateAvailable: Boolean, newReleaseLink: String?, errorOnRequest: Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder().get().url(GITHUB_RELEASES_LINK).build()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onResult(false, null, true)
                }

                override fun onResponse(call: Call, response: Response) {
                    val versionRegex = Regex("[0-9]+\\.[0-9]+\\.[0-9]+")
                    val jsonObj = JSONArray(response.body!!.string())
                    val latestRelJson = jsonObj.getJSONObject(0)
                    val tagName = latestRelJson.getString("tag_name")
                    val latestVersion = versionRegex.find(tagName)?.value
                    val appVersion = versionRegex.find(BuildConfig.VERSION_NAME)?.value
                    if (appVersion != latestVersion) {
                        val latestRelAssets = latestRelJson.getJSONArray("assets")
                        val latestReleaseLink =
                            latestRelAssets.getJSONObject(0).getString("browser_download_url")
                        onResult(true, latestReleaseLink, false)
                    } else {
                        onResult(false, null, false)
                    }
                }
            })
        }
    }

    @ExperimentalCoilApi
    @ExperimentalComposeUiApi
    @ExperimentalMaterial3Api
    @ExperimentalMaterialApi
    fun downloadUpdate(downloadUrl: String, activity: MainActivity) {
        if (activity.checkStoragePermission()) {
            val fileName = downloadUrl.split("/").last()
            val manager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(downloadUrl)
            val request = DownloadManager.Request(uri)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverRoaming(true)
                .setAllowedOverMetered(true)
                .setTitle(activity.getString(R.string.downloading_update))
                .setDescription(fileName)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            manager.enqueue(request)
            activity.getString(R.string.downloading_update).toToast(activity)
        } else {
            activity.getString(R.string.storage_perm_error).toToast(activity)
        }
    }
}
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


package com.starry.myne.helpers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.starry.myne.BuildConfig
import com.starry.myne.R
import com.starry.myne.database.library.LibraryItem
import com.starry.myne.ui.navigation.Screens
import java.io.File
import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

object Utils {
    fun prettyCount(number: Number): String {
        val suffix = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
        val numValue = number.toLong()
        val value = floor(log10(numValue.toDouble())).toInt()
        val base = value / 3
        return if (value >= 3 && base < suffix.size) {
            DecimalFormat("#0.0").format(
                numValue / 10.0.pow((base * 3).toDouble())
            ) + suffix[base]
        } else {
            DecimalFormat("#,##0").format(numValue)
        }
    }

    fun openBookFile(
        context: Context,
        internalReader: Boolean,
        libraryItem: LibraryItem,
        navController: NavController
    ) {

        if (internalReader) {
            navController.navigate(Screens.ReaderDetailScreen.withLibraryItemId(libraryItem.id.toString()))
        } else {
            val uri = FileProvider.getUriForFile(
                context, BuildConfig.APPLICATION_ID + ".provider", File(libraryItem.filePath)
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(uri, context.contentResolver.getType(uri))
            val chooser = Intent.createChooser(
                intent, context.getString(R.string.open_app_chooser)
            )
            try {
                context.startActivity(chooser)
            } catch (exc: ActivityNotFoundException) {
                context.getString(R.string.no_app_to_handle_epub).toToast(context)
            }
        }
    }
}
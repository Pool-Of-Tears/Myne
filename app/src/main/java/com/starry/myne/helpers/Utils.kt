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
import android.net.Uri
import android.os.Build
import com.starry.myne.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * A collection of utility functions.
 */
object Utils {

    /**
     * Formats a number into a more readable format with a suffix representing its magnitude.
     * For example, 1000 becomes "1k", 1000000 becomes "1M", etc.
     *
     * @param number The number to format.
     * @return A string representation of the number with a magnitude suffix.
     */
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

    /**
     * Opens a web link in the default browser.
     *
     * @param context The context to use.
     * @param url The URL to open.
     */
    fun openWebLink(context: Context, url: String) {
        val uri: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (exc: ActivityNotFoundException) {
            exc.printStackTrace()
            context.getString(R.string.error).toToast(context)
        }
    }

    /**
     * Opens an email client with the specified email address and subject.
     *
     * @param context The context to use.
     * @param email The email address to send the email to.
     * @param subject The subject of the email.
     */
    fun openEmail(context: Context, email: String, subject: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        try {
            context.startActivity(emailIntent)
        } catch (exc: ActivityNotFoundException) {
            exc.printStackTrace()
            context.getString(R.string.error).toToast(context)
        }
    }

    /**
     * Check if the device is running on MIUI.
     *
     * By default, HyperOS is excluded from the check.
     * If you want to include HyperOS in the check, set excludeHyperOS to false.
     *
     * @param excludeHyperOS Whether to exclude HyperOS
     * @return True if the device is running on MIUI, false otherwise
     */
    fun isMiui(excludeHyperOS: Boolean = true): Boolean {
        // Return false if the device is not from Xiaomi, Redmi, or POCO.
        val brand = Build.BRAND.lowercase()
        if (!setOf("xiaomi", "redmi", "poco").contains(brand)) return false
        // Check if the device is running on MIUI and not HyperOS.
        val isMiui = !getProperty("ro.miui.ui.version.name").isNullOrBlank()
        val isHyperOS = !getProperty("ro.mi.os.version.name").isNullOrBlank()
        return isMiui && (!excludeHyperOS || !isHyperOS)
    }

    // Private function to get the property value from build.prop.
    private fun getProperty(property: String): String? {
        return try {
            Runtime.getRuntime().exec("getprop $property").inputStream.use { input ->
                BufferedReader(InputStreamReader(input), 1024).readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
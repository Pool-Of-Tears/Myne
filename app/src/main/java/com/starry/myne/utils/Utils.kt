package com.starry.myne.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.starry.myne.BuildConfig
import com.starry.myne.R
import com.starry.myne.database.library.LibraryItem
import com.starry.myne.navigation.Screens
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

    fun openBookFile(context: Context, item: LibraryItem, navController: NavController) {

        if (PreferenceUtils.getBoolean(PreferenceUtils.INTERNAL_READER, true)) {
            navController.navigate(
                Screens.ReaderDetailScreen.withBookId(
                    item.bookId.toString()
                )
            )
        } else {
            val uri = FileProvider.getUriForFile(
                context, BuildConfig.APPLICATION_ID + ".provider", File(item.filePath)
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
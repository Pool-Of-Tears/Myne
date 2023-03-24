package com.starry.myne.others

import androidx.annotation.Keep

@Keep
sealed class BookLanguages(val name: String, val isoCode: String) {

    companion object {
        fun getAllLanguages() =
            BookLanguages::class.sealedSubclasses.mapNotNull { it.objectInstance }
    }

    @Keep
    object AllBooks : BookLanguages("All Books", "all")

    @Keep
    object Chinese : BookLanguages("Chinese", "zh")

    @Keep
    object Danish : BookLanguages("Danish", "da")

    @Keep
    object Dutch : BookLanguages("Dutch", "nl")

    @Keep
    object English : BookLanguages("English", "en")

    @Keep
    object Finnish : BookLanguages("Finnish", "fi")

    @Keep
    object French : BookLanguages("French", "fr")

    @Keep
    object German : BookLanguages("German", "de")

    @Keep
    object Hungarian : BookLanguages("Hungarian", "hu")

    @Keep
    object Italian : BookLanguages("Italian", "it")

    @Keep
    object Portuguese : BookLanguages("Portuguese", "pt")

    @Keep
    object Russian : BookLanguages("Russian", "ru")

    @Keep
    object Spanish : BookLanguages("Spanish", "es")

    @Keep
    object Swedish : BookLanguages("Swedish", "sv")
}

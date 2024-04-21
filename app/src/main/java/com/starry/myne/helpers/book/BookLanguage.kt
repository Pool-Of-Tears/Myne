package com.starry.myne.helpers.book

import androidx.annotation.Keep

@Keep
sealed class BookLanguage(val name: String, val isoCode: String) {

    companion object {
        fun getAllLanguages() =
            BookLanguage::class.sealedSubclasses.mapNotNull { it.objectInstance }
    }

    @Keep
    data object AllBooks : BookLanguage("All Books", "all")

    @Keep
    data object Chinese : BookLanguage("Chinese", "zh")

    @Keep
    data object Danish : BookLanguage("Danish", "da")

    @Keep
    data object Dutch : BookLanguage("Dutch", "nl")

    @Keep
    data object English : BookLanguage("English", "en")

    @Keep
    data object Esperanto : BookLanguage("Esperanto", "eo")

    @Keep
    data object Finnish : BookLanguage("Finnish", "fi")

    @Keep
    data object French : BookLanguage("French", "fr")

    @Keep
    data object German : BookLanguage("German", "de")

    @Keep
    data object Greek : BookLanguage("Greek", "el")

    @Keep
    data object Hungarian : BookLanguage("Hungarian", "hu")

    @Keep
    data object Italian : BookLanguage("Italian", "it")

    @Keep
    data object Latin : BookLanguage("Latin", "la")

    @Keep
    data object Portuguese : BookLanguage("Portuguese", "pt")

    @Keep
    data object Russian : BookLanguage("Russian", "ru")

    @Keep
    data object Spanish : BookLanguage("Spanish", "es")

    @Keep
    data object Swedish : BookLanguage("Swedish", "sv")

    @Keep
    data object Tagalog : BookLanguage("Tagalog", "tl")
}

package com.starry.myne.utils.book

import androidx.annotation.Keep

@Keep
sealed class BookLanguage(val name: String, val isoCode: String) {

    companion object {
        fun getAllLanguages() =
            BookLanguage::class.sealedSubclasses.mapNotNull { it.objectInstance }
    }

    @Keep
    object AllBooks : BookLanguage("All Books", "all")

    @Keep
    object Chinese : BookLanguage("Chinese", "zh")

    @Keep
    object Danish : BookLanguage("Danish", "da")

    @Keep
    object Dutch : BookLanguage("Dutch", "nl")

    @Keep
    object English : BookLanguage("English", "en")

    @Keep
    object Esperanto : BookLanguage("Esperanto", "eo")

    @Keep
    object Finnish : BookLanguage("Finnish", "fi")

    @Keep
    object French : BookLanguage("French", "fr")

    @Keep
    object German : BookLanguage("German", "de")

    @Keep
    object Greek : BookLanguage("Greek", "el")

    @Keep
    object Hungarian : BookLanguage("Hungarian", "hu")

    @Keep
    object Italian : BookLanguage("Italian", "it")

    @Keep
    object Latin : BookLanguage("Latin", "la")

    @Keep
    object Portuguese : BookLanguage("Portuguese", "pt")

    @Keep
    object Russian : BookLanguage("Russian", "ru")

    @Keep
    object Spanish : BookLanguage("Spanish", "es")

    @Keep
    object Swedish : BookLanguage("Swedish", "sv")

    @Keep
    object Tagalog : BookLanguage("Tagalog", "tl")
}

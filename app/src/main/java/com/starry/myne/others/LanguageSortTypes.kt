package com.starry.myne.others

sealed class LanguageSortTypes(val name: String, val isoCode: String) {

    companion object {
        fun getAllLanguages() =
            LanguageSortTypes::class.sealedSubclasses.mapNotNull { it.objectInstance }
    }

    object Chinese : LanguageSortTypes("Chinese", "zh")
    object Danish : LanguageSortTypes("Danish", "da")
    object Dutch : LanguageSortTypes("Dutch", "nl")
    object English : LanguageSortTypes("English", "en")
    object Esperanto : LanguageSortTypes("Esperanto", "eo")
    object Finnish : LanguageSortTypes("Finnish", "fi")
    object French : LanguageSortTypes("French", "fr")
    object German : LanguageSortTypes("German", "de")
    object Greek : LanguageSortTypes("Greek", "el")
    object Hungarian : LanguageSortTypes("Hungarian", "hu")
    object Italian : LanguageSortTypes("Italian", "it")
    object Latin : LanguageSortTypes("Latin", "la")
    object Portuguese : LanguageSortTypes("Portuguese", "pt")
    object Spanish : LanguageSortTypes("Spanish", "es")
    object Swedish : LanguageSortTypes("Swedish", "sv")
    object Tagalog : LanguageSortTypes("Tagalog", "tl")
}

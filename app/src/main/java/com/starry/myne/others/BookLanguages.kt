package com.starry.myne.others

sealed class BookLanguages(val name: String, val isoCode: String) {

    companion object {
        fun getAllLanguages() =
            BookLanguages::class.sealedSubclasses.mapNotNull { it.objectInstance }
    }

    object AllBooks: BookLanguages("All Books","all")
    object Chinese : BookLanguages("Chinese", "zh")
    object Danish : BookLanguages("Danish", "da")
    object Dutch : BookLanguages("Dutch", "nl")
    object English : BookLanguages("English", "en")
    object Finnish : BookLanguages("Finnish", "fi")
    object French : BookLanguages("French", "fr")
    object German : BookLanguages("German", "de")
    object Hungarian : BookLanguages("Hungarian", "hu")
    object Italian : BookLanguages("Italian", "it")
    object Portuguese : BookLanguages("Portuguese", "pt")
    object Russian: BookLanguages("Russian","ru")
    object Spanish : BookLanguages("Spanish", "es")
    object Swedish : BookLanguages("Swedish", "sv")
}
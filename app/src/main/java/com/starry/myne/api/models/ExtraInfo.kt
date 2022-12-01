package com.starry.myne.api.models

/** Extra info from google books API */
data class ExtraInfo(
    val coverImage: String = "",
    val pageCount: Int = 0,
    val description: String = ""
)

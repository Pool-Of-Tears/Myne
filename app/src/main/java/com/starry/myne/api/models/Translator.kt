package com.starry.myne.api.models


import com.google.gson.annotations.SerializedName

data class Translator(
    @SerializedName("name")
    val name: String = "N/A",
    @SerializedName("birth_year")
    val birthYear: Int,
    @SerializedName("death_year")
    val deathYear: Int
)
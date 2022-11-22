package com.starry.myne.api.models


import com.google.gson.annotations.SerializedName

data class Author(
    @SerializedName("name")
    val name: String,
    @SerializedName("birth_year")
    val birthYear: Int,
    @SerializedName("death_year")
    val deathYear: Int
)
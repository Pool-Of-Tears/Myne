package com.starry.myne.ui.viewmodels

import androidx.lifecycle.ViewModel


class CategoriesViewModel : ViewModel() {
    companion object {
        val CATEGORIES_ARRAY =
            listOf(
                "animal",
                "children",
                "classics",
                "countries",
                "crime",
                "education",
                "fiction",
                "geography",
                "history",
                "literature",
                "law",
                "music",
                "periodicals",
                "psychology",
                "philosophy",
                "religion",
                "romance",
                "science",
            )
    }
}
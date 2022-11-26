package com.starry.myne.navigation

import com.starry.myne.R

sealed class BottomBarScreen(
    val route: String,
    val title: Int,
    val icon: Int
) {
    object Home : BottomBarScreen(
        route = "home",
        title = R.string.navigation_home,
        icon = R.drawable.ic_nav_home
    )

    object Search : BottomBarScreen(
        route = "search",
        title = R.string.navigation_search,
        icon = R.drawable.ic_nav_search
    )

    object Library : BottomBarScreen(
        route = "library",
        title = R.string.navigation_library,
        icon = R.drawable.ic_nav_library
    )

    object Settings : BottomBarScreen(
        route = "settings",
        title = R.string.navigation_settings,
        icon = R.drawable.ic_nav_settings
    )
}
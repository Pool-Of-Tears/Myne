/*
Copyright 2022 - 2023 Stɑrry Shivɑm

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

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

    object Categories : BottomBarScreen(
        route = "categories",
        title = R.string.navigation_categories,
        icon = R.drawable.ic_nav_categories
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
/**
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.starry.myne.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.starry.myne.helpers.NetworkObserver
import com.starry.myne.ui.screens.categories.composables.CategoriesScreen
import com.starry.myne.ui.screens.categories.composables.CategoryDetailScreen
import com.starry.myne.ui.screens.detail.composables.BookDetailScreen
import com.starry.myne.ui.screens.home.composables.HomeScreen
import com.starry.myne.ui.screens.library.composables.LibraryScreen
import com.starry.myne.ui.screens.reader.composables.ReaderDetailScreen
import com.starry.myne.ui.screens.settings.composables.AboutScreen
import com.starry.myne.ui.screens.settings.composables.OSLScreen
import com.starry.myne.ui.screens.settings.composables.SettingsScreen
import com.starry.myne.ui.screens.welcome.composables.WelcomeScreen


private const val NAVIGATION_ANIM_DURATION = 300
private const val FADEIN_ANIM_DURATION = 400

private fun enterTransition() = slideInHorizontally(
    initialOffsetX = { NAVIGATION_ANIM_DURATION }, animationSpec = tween(
        durationMillis = NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing
    )
) + fadeIn(animationSpec = tween(NAVIGATION_ANIM_DURATION))

private fun exitTransition() = slideOutHorizontally(
    targetOffsetX = { -NAVIGATION_ANIM_DURATION }, animationSpec = tween(
        durationMillis = NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing
    )
) + fadeOut(animationSpec = tween(NAVIGATION_ANIM_DURATION))

private fun popEnterTransition() = slideInHorizontally(
    initialOffsetX = { -NAVIGATION_ANIM_DURATION }, animationSpec = tween(
        durationMillis = NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing
    )
) + fadeIn(animationSpec = tween(NAVIGATION_ANIM_DURATION))

private fun popExitTransition() = slideOutHorizontally(
    targetOffsetX = { NAVIGATION_ANIM_DURATION }, animationSpec = tween(
        durationMillis = NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing
    )
) + fadeOut(animationSpec = tween(NAVIGATION_ANIM_DURATION))


@Composable
fun NavGraph(
    startDestination: String,
    navController: NavHostController,
    networkStatus: NetworkObserver.Status,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {

        /** Welcome Screen */
        composable(
            route = Screens.WelcomeScreen.route,
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
        ) {
            WelcomeScreen(navController = navController)
        }

        /** Home Screen */
        composable(route = BottomBarScreen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(FADEIN_ANIM_DURATION)) },
            exitTransition = {
                if (initialState.destination.route == Screens.BookDetailScreen.route) {
                    exitTransition()
                } else fadeOut(animationSpec = tween(FADEIN_ANIM_DURATION))
            },
            popEnterTransition = {
                if (targetState.destination.route == Screens.BookDetailScreen.route) {
                    popEnterTransition()
                } else fadeIn(animationSpec = tween(FADEIN_ANIM_DURATION))
            },
            popExitTransition = { fadeOut(animationSpec = tween(FADEIN_ANIM_DURATION)) }) {
            HomeScreen(navController, networkStatus)
        }

        /** Book Detail Screen */
        composable(
            route = Screens.BookDetailScreen.route,
            arguments = listOf(
                navArgument(BOOK_ID_ARG_KEY) {
                    type = NavType.StringType
                },
            ),
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() },
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments!!.getString(BOOK_ID_ARG_KEY)!!
            BookDetailScreen(bookId, navController)
        }

        /** Categories Screen */
        composable(route = BottomBarScreen.Categories.route,
            enterTransition = { fadeIn(animationSpec = tween(FADEIN_ANIM_DURATION)) },
            exitTransition = {
                if (initialState.destination.route == Screens.CategoryDetailScreen.route) {
                    exitTransition()
                } else fadeOut(animationSpec = tween(FADEIN_ANIM_DURATION))
            },
            popEnterTransition = {
                if (targetState.destination.route == Screens.CategoryDetailScreen.route) {
                    popEnterTransition()
                } else fadeIn(animationSpec = tween(FADEIN_ANIM_DURATION))
            },
            popExitTransition = { fadeOut(animationSpec = tween(FADEIN_ANIM_DURATION)) }) {
            CategoriesScreen(navController)
        }

        /** Category Detail Screen */
        composable(
            route = Screens.CategoryDetailScreen.route,
            arguments = listOf(navArgument(CATEGORY_DETAIL_ARG_KEY) {
                type = NavType.StringType
            }),
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() },
        ) { backStackEntry ->
            val category = backStackEntry.arguments!!.getString(CATEGORY_DETAIL_ARG_KEY)!!
            CategoryDetailScreen(category, navController, networkStatus)
        }

        /** Library Screen */
        composable(route = BottomBarScreen.Library.route,
            enterTransition = { fadeIn(animationSpec = tween(FADEIN_ANIM_DURATION)) },
            exitTransition = {
                if (initialState.destination.route == Screens.BookDetailScreen.route) {
                    exitTransition()
                } else fadeOut(animationSpec = tween(FADEIN_ANIM_DURATION))
            },
            popEnterTransition = {
                if (targetState.destination.route == BottomBarScreen.Library.route) {
                    popEnterTransition()
                } else fadeIn(animationSpec = tween(FADEIN_ANIM_DURATION))
            },
            popExitTransition = { fadeOut(animationSpec = tween(FADEIN_ANIM_DURATION)) }) {
            LibraryScreen(navController)
        }

        /** Reader Detail Screen */
        composable(
            route = Screens.ReaderDetailScreen.route,
            arguments = listOf(navArgument(
                LIBRARY_ITEM_ID_ARG_KEY
            ) {
                type = NavType.StringType
            }),
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() },
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments!!.getString(LIBRARY_ITEM_ID_ARG_KEY)!!
            ReaderDetailScreen(
                libraryItemId = bookId, navController = navController, networkStatus = networkStatus
            )
        }

        /** Settings Screen */
        composable(route = BottomBarScreen.Settings.route,
            enterTransition = { fadeIn(animationSpec = tween(FADEIN_ANIM_DURATION)) },
            exitTransition = {
                if (initialState.destination.route == Screens.OSLScreen.route || initialState.destination.route == Screens.AboutScreen.route) {
                    exitTransition()
                } else fadeOut(animationSpec = tween(FADEIN_ANIM_DURATION))
            },
            popEnterTransition = {
                if (initialState.destination.route == Screens.OSLScreen.route || initialState.destination.route == Screens.AboutScreen.route) {
                    popEnterTransition()
                } else fadeIn(animationSpec = tween(FADEIN_ANIM_DURATION))
            },
            popExitTransition = { fadeOut(animationSpec = tween(FADEIN_ANIM_DURATION)) }) {
            SettingsScreen(navController)
        }

        /** Open Source Licenses Screen */
        composable(
            route = Screens.OSLScreen.route,
            enterTransition = { enterTransition() },
            popExitTransition = { popExitTransition() },
        ) {
            OSLScreen(navController = navController)
        }

        /** About Screen */
        composable(
            route = Screens.AboutScreen.route,
            enterTransition = { enterTransition() },
            popExitTransition = { popExitTransition() },
        ) {
            AboutScreen(navController = navController)
        }
    }
}
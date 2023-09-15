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

package com.starry.myne.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.starry.myne.others.NetworkObserver
import com.starry.myne.ui.screens.categories.composables.CategoriesScreen
import com.starry.myne.ui.screens.categories.composables.CategoryDetailScreen
import com.starry.myne.ui.screens.home.composables.BookDetailScreen
import com.starry.myne.ui.screens.home.composables.HomeScreen
import com.starry.myne.ui.screens.library.composables.LibraryScreen
import com.starry.myne.ui.screens.reader.composables.ReaderDetailScreen
import com.starry.myne.ui.screens.settings.composables.AboutScreen
import com.starry.myne.ui.screens.settings.composables.OSLScreen
import com.starry.myne.ui.screens.settings.composables.SettingsScreen
import com.starry.myne.ui.screens.welcome.composables.WelcomeScreen

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalCoilApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun NavGraph(
    startDestination: String,
    navController: NavHostController,
    networkStatus: NetworkObserver.Status,
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {

        /** Welcome Screen */
        composable(
            route = Screens.WelcomeScreen.route,
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeIn(animationSpec = tween(300))

            },
        ) {
            WelcomeScreen(navController = navController)
        }

        /** Home Screen */
        composable(
            route = BottomBarScreen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = {
                if (initialState.destination.route == Screens.BookDetailScreen.route) {
                    slideOutHorizontally(
                        targetOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeOut(animationSpec = tween(300))
                } else fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                if (targetState.destination.route == Screens.BookDetailScreen.route) {
                    slideInHorizontally(
                        initialOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeIn(animationSpec = tween(300))
                } else fadeIn(animationSpec = tween(400))
            },
            popExitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
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
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween(300))

            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeIn(animationSpec = tween(300))

            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween(300))
            },
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments!!.getString(BOOK_ID_ARG_KEY)!!
            BookDetailScreen(bookId, navController)
        }

        /** Categories Screen */
        composable(
            route = BottomBarScreen.Categories.route,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = {
                if (initialState.destination.route == Screens.CategoryDetailScreen.route) {
                    slideOutHorizontally(
                        targetOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeOut(animationSpec = tween(300))
                } else fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                if (targetState.destination.route == Screens.CategoryDetailScreen.route) {
                    slideInHorizontally(
                        initialOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeIn(animationSpec = tween(300))
                } else fadeIn(animationSpec = tween(400))
            },
            popExitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            CategoriesScreen(navController)
        }

        /** Category Detail Screen */
        composable(
            route = Screens.CategoryDetailScreen.route,
            arguments = listOf(navArgument(CATEGORY_DETAIL_ARG_KEY) {
                type = NavType.StringType
            }),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween(300))

            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeIn(animationSpec = tween(300))

            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween(300))
            },
        ) { backStackEntry ->
            val category = backStackEntry.arguments!!.getString(CATEGORY_DETAIL_ARG_KEY)!!
            CategoryDetailScreen(category, navController, networkStatus)
        }

        /** Library Screen */
        composable(
            route = BottomBarScreen.Library.route,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = {
                if (initialState.destination.route == Screens.BookDetailScreen.route) {
                    slideOutHorizontally(
                        targetOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeOut(animationSpec = tween(300))
                } else fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                if (targetState.destination.route == BottomBarScreen.Library.route) {
                    slideInHorizontally(
                        initialOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeIn(animationSpec = tween(300))
                } else fadeIn(animationSpec = tween(400))
            },
            popExitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            LibraryScreen(navController)
        }

        /** Reader Detail Screen */
        composable(
            route = Screens.ReaderDetailScreen.route,
            arguments = listOf(navArgument(
                BOOK_ID_ARG_KEY
            ) {
                type = NavType.StringType
            }),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween(300))

            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeIn(animationSpec = tween(300))

            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween(300))
            },
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments!!.getString(BOOK_ID_ARG_KEY)!!
            ReaderDetailScreen(
                bookId = bookId,
                navController = navController,
                networkStatus = networkStatus
            )
        }

        /** Settings Screen */
        composable(
            route = BottomBarScreen.Settings.route,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = {
                if (initialState.destination.route == Screens.OSLScreen.route || initialState.destination.route == Screens.AboutScreen.route) {
                    slideOutHorizontally(
                        targetOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeOut(animationSpec = tween(300))
                } else fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                if (initialState.destination.route == Screens.OSLScreen.route || initialState.destination.route == Screens.AboutScreen.route) {
                    slideInHorizontally(
                        initialOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeIn(animationSpec = tween(300))
                } else fadeIn(animationSpec = tween(400))
            },
            popExitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            SettingsScreen(navController)
        }

        /** Open Source Licenses Screen */
        composable(
            route = Screens.OSLScreen.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween(300))
            },
        ) {
            OSLScreen(navController = navController)
        }

        /** About Screen */
        composable(
            route = Screens.AboutScreen.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 300 }, animationSpec = tween(
                        durationMillis = 300, easing = FastOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween(300))
            },
        ) {
            AboutScreen(navController = navController)
        }
    }
}
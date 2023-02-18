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

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import com.starry.myne.ui.screens.*
import com.starry.myne.ui.screens.home.composables.BookDetailScreen
import com.starry.myne.ui.screens.home.composables.HomeScreen
import com.starry.myne.ui.screens.library.composables.LibraryScreen
import com.starry.myne.ui.screens.reader.composables.ReaderDetailScreen
import com.starry.myne.ui.screens.reader.composables.ReaderScreen
import com.starry.myne.ui.screens.settings.composables.SettingsScreen

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalCoilApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun NavGraph(
    navController: NavHostController,
    networkStatus: NetworkObserver.Status,
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Screens.SplashScreen.route,
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        // Splash Screen.
        composable(
            route = Screens.SplashScreen.route,
        ) {
            SplashScreen(navController = navController)
        }

        /** Home Screen */
        composable(
            route = BottomBarScreen.Home.route,
            exitTransition = {
                if (initialState.destination.route == Screens.BookDetailScreen.route) {
                    slideOutHorizontally(
                        targetOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeOut(animationSpec = tween(300))
                } else null
            },
            popEnterTransition = {
                if (targetState.destination.route == Screens.BookDetailScreen.route) {
                    slideInHorizontally(
                        initialOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeIn(animationSpec = tween(300))
                } else null
            },
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
            BookDetailScreen(bookId, navController, networkStatus)
        }

        /** Categories Screen */
        composable(
            route = BottomBarScreen.Categories.route,
            exitTransition = {
                if (initialState.destination.route == Screens.CategoryDetailScreen.route) {
                    slideOutHorizontally(
                        targetOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeOut(animationSpec = tween(300))
                } else null
            },
            popEnterTransition = {
                if (targetState.destination.route == Screens.CategoryDetailScreen.route) {
                    slideInHorizontally(
                        initialOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeIn(animationSpec = tween(300))
                } else null
            },
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
            exitTransition = {
                if (initialState.destination.route == Screens.BookDetailScreen.route) {
                    slideOutHorizontally(
                        targetOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeOut(animationSpec = tween(300))
                } else null
            },
            popEnterTransition = {
                if (targetState.destination.route == BottomBarScreen.Library.route) {
                    slideInHorizontally(
                        initialOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeIn(animationSpec = tween(300))
                } else null
            },
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
            ReaderDetailScreen(bookId = bookId, navController = navController)
        }

        /** Reader Screen */
        composable(
            route = Screens.ReaderScreen.route,
            arguments = listOf(navArgument(
                BOOK_ID_ARG_KEY
            ) {
                type = NavType.StringType
            }, navArgument(READER_CHAPTER_INDEX_KEY) {
                type = NavType.IntType
            }),
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
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments!!.getString(BOOK_ID_ARG_KEY)!!
            val chapterIdx = backStackEntry.arguments!!.getInt(READER_CHAPTER_INDEX_KEY)
            ReaderScreen(bookId = bookId, chapterIdx)
        }

        /** Settings Screen */
        composable(
            route = BottomBarScreen.Settings.route,
            exitTransition = {
                if (initialState.destination.route == Screens.OSLScreen.route || initialState.destination.route == Screens.AboutScreen.route) {
                    slideOutHorizontally(
                        targetOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeOut(animationSpec = tween(300))
                } else null
            },
            popEnterTransition = {
                if (initialState.destination.route == Screens.OSLScreen.route || initialState.destination.route == Screens.AboutScreen.route) {
                    slideInHorizontally(
                        initialOffsetX = { -300 }, animationSpec = tween(
                            durationMillis = 300, easing = FastOutSlowInEasing
                        )
                    ) + fadeIn(animationSpec = tween(300))
                } else null
            },
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
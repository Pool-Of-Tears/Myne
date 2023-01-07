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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import coil.annotation.ExperimentalCoilApi
import com.starry.myne.others.NetworkObserver
import com.starry.myne.ui.screens.*

@ExperimentalMaterialApi
@ExperimentalCoilApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    networkStatus: NetworkObserver.Status,
) {
    NavHost(
        navController = navController,
        startDestination = Screens.SplashScreen.route,
        modifier = Modifier
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Splash Screen.
        composable(route = Screens.SplashScreen.route) {
            SplashScreen(navController = navController)
        }
        // Bottom Navigation Screens
        composable(route = BottomBarScreen.Home.route) {
            HomeScreen(navController, networkStatus)
        }
        composable(route = BottomBarScreen.Categories.route) {
            CategoriesScreen(navController)
        }
        composable(route = BottomBarScreen.Library.route) {
            LibraryScreen()
        }
        composable(route = BottomBarScreen.Settings.route) {
            SettingsScreen(navController)
        }

        // Other screens.
        composable(
            route = Screens.BookDetailScreen.route, arguments = listOf(
                navArgument(BOOK_DETAIL_ARG_KEY) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments!!.getString(BOOK_DETAIL_ARG_KEY)!!
            BookDetailScreen(bookId, navController, networkStatus)
        }

        composable(
            route = Screens.CategoryDetailScreen.route, arguments = listOf(
                navArgument(CATEGORY_DETAIL_ARG_KEY) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments!!.getString(CATEGORY_DETAIL_ARG_KEY)!!
            CategoryDetailScreen(category, navController, networkStatus)
        }

        composable(route = Screens.OSLScreen.route) {
            OSLScreen(navController = navController)
        }
    }
}
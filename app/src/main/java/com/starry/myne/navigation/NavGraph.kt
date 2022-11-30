package com.starry.myne.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import com.starry.myne.ui.screens.*

@ExperimentalCoilApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun NavGraph(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Home.route,
        modifier = Modifier
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Bottom Navigation Screens
        composable(route = BottomBarScreen.Home.route) {
            HomeScreen(navController)
        }
        composable(route = BottomBarScreen.Categories.route) {
            CategoriesScreen()
        }
        composable(route = BottomBarScreen.Library.route) {
            LibraryScreen()
        }
        composable(route = BottomBarScreen.Settings.route) {
            SettingsScreen()
        }

        // Other screens.
        composable(
            route = Screens.BookDetailScreen.route, arguments = listOf(
                navArgument(BOOK_DETAIL_ARG_KEY) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments!!.getInt(BOOK_DETAIL_ARG_KEY)
            BookDetailScreen(bookId)
        }
    }
}
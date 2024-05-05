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

package com.starry.myne.ui.screens.categories.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.starry.myne.R
import com.starry.myne.helpers.NetworkObserver
import com.starry.myne.helpers.book.BookUtils
import com.starry.myne.ui.common.BookItemCard
import com.starry.myne.ui.common.BookItemShimmerLoader
import com.starry.myne.ui.common.BookLanguageSheet
import com.starry.myne.ui.common.CustomTopAppBar
import com.starry.myne.ui.common.NetworkError
import com.starry.myne.ui.common.NoBooksAvailable
import com.starry.myne.ui.common.ProgressDots
import com.starry.myne.ui.navigation.Screens
import com.starry.myne.ui.screens.categories.viewmodels.CategoryViewModel
import java.util.Locale


@Composable
fun CategoryDetailScreen(
    category: String, navController: NavController, networkStatus: NetworkObserver.Status
) {
    val viewModel: CategoryViewModel = hiltViewModel()
    val showLanguageSheet = remember { mutableStateOf(false) }

    BookLanguageSheet(
        showBookLanguage = showLanguageSheet,
        selectedLanguage = viewModel.language.value,
        onLanguageChange = { viewModel.changeLanguage(it) }
    )

    val state = viewModel.state
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CustomTopAppBar(headerText = stringResource(id = BookCategories.getNameRes(category)),
                actionIcon = Icons.Filled.Translate,
                onBackButtonClicked = { navController.navigateUp() },
                onActionClicked = { showLanguageSheet.value = true }
            )
        }, content = {
            LaunchedEffect(key1 = true, block = { viewModel.loadBookByCategory(category) })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(it)
            ) {
                AnimatedVisibility(
                    visible = state.page == 1L && state.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    BookItemShimmerLoader()
                }
                AnimatedVisibility(
                    visible = !state.isLoading && state.error != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    NetworkError(onRetryClicked = { viewModel.reloadItems() })
                }
                AnimatedVisibility(
                    visible = !state.isLoading && state.items.isEmpty() && state.error == null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    NoBooksAvailable(
                        text = stringResource(id = R.string.no_books_found_for_lang_and_cat)
                            .format(viewModel.language.value.name.lowercase(Locale.getDefault()))
                    )
                }
                AnimatedVisibility(
                    visible = !state.isLoading || (state.items.isNotEmpty() && state.error == null),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(start = 8.dp, end = 8.dp),
                        columns = GridCells.Adaptive(295.dp)
                    ) {
                        items(state.items.size) { i ->
                            val item = state.items[i]
                            if (networkStatus == NetworkObserver.Status.Available && i >= state.items.size - 1 && !state.endReached && !state.isLoading) {
                                viewModel.loadNextItems()
                            }
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                BookItemCard(
                                    title = item.title,
                                    author = BookUtils.getAuthorsAsString(item.authors),
                                    language = BookUtils.getLanguagesAsString(item.languages),
                                    subjects = BookUtils.getSubjectsAsString(item.subjects, 3),
                                    coverImageUrl = item.formats.imagejpeg
                                ) {
                                    navController.navigate(Screens.BookDetailScreen.withBookId(item.id.toString()))
                                }
                            }
                        }
                        item {
                            if (state.isLoading) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    ProgressDots()
                                }
                            }
                        }
                    }
                }
            }

        })
}
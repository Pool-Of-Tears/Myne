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

package com.starry.myne.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.starry.myne.navigation.Screens
import com.starry.myne.others.NetworkObserver
import com.starry.myne.others.viewModelFactory
import com.starry.myne.ui.common.BookItemCard
import com.starry.myne.ui.common.CustomTopAppBar
import com.starry.myne.ui.common.ProgressDots
import com.starry.myne.ui.screens.categories.viewmodels.CategoryViewModel
import com.starry.myne.utils.BookUtils
import kotlinx.coroutines.delay

@ExperimentalCoilApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun CategoryDetailScreen(
    category: String,
    navController: NavController,
    networkStatus: NetworkObserver.Status
) {
    if (networkStatus == NetworkObserver.Status.Available) {
        val viewModel = viewModel<CategoryViewModel>(factory = viewModelFactory {
            CategoryViewModel(category)
        })
        val state = viewModel.state

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            CustomTopAppBar(headerText = category) {
                navController.navigateUp()
            }

            if (state.page == 1L && state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(start = 8.dp, end = 8.dp)
                ) {
                    items(state.items.size) { i ->
                        val item = state.items[i]
                        if (i >= state.items.size - 1 && !state.endReached && !state.isLoading) {
                            viewModel.loadNextItems()
                        }
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth(), contentAlignment = Alignment.Center
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

    } else {
        var showNoInternet by remember { mutableStateOf(false) }
        LaunchedEffect(key1 = Unit) {
            delay(250)
            showNoInternet = true
        }
        if (showNoInternet) {
            NoInternetScreen()
        }
    }
}
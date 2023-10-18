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

package com.starry.myne.ui.screens.categories.composables

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.starry.myne.R
import com.starry.myne.others.BookLanguage
import com.starry.myne.others.NetworkObserver
import com.starry.myne.ui.common.BookItemCard
import com.starry.myne.ui.common.CustomTopAppBar
import com.starry.myne.ui.common.NoBooksAvailable
import com.starry.myne.ui.common.ProgressDots
import com.starry.myne.ui.navigation.Screens
import com.starry.myne.ui.screens.categories.viewmodels.CategoryViewModel
import com.starry.myne.ui.screens.home.composables.LanguageItem
import com.starry.myne.ui.screens.other.NetworkError
import com.starry.myne.ui.theme.pacificoFont
import com.starry.myne.utils.BookUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

@ExperimentalMaterialApi
@ExperimentalCoilApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun CategoryDetailScreen(
    category: String, navController: NavController, networkStatus: NetworkObserver.Status
) {
    val viewModel: CategoryViewModel = hiltViewModel()
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()


    val modalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )
    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetElevation = 24.dp,
        sheetBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        sheetContent = {
            val languages = BookLanguage.getAllLanguages()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    text = stringResource(id = R.string.language_menu_title),
                    textAlign = TextAlign.Center,
                    fontFamily = pacificoFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                )
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp),
                    columns = GridCells.Adaptive(168.dp)
                ) {
                    items(languages.size) { idx ->
                        val language = languages[idx]
                        LanguageItem(language = language,
                            isSelected = language == viewModel.language.value,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.changeLanguage(language)
                                coroutineScope.launch {
                                    modalBottomSheetState.hide()
                                }
                            })
                    }
                }
            }

        }) {
        CategoryDetailScaffold(
            category = category,
            viewModel = viewModel,
            navController = navController,
            networkStatus = networkStatus,
            coroutineScope = coroutineScope,
            bottomSheetState = modalBottomSheetState
        )
    }

}

@ExperimentalMaterialApi
@ExperimentalCoilApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun CategoryDetailScaffold(
    category: String,
    viewModel: CategoryViewModel,
    navController: NavController,
    networkStatus: NetworkObserver.Status,
    coroutineScope: CoroutineScope,
    bottomSheetState: ModalBottomSheetState
) {
    val state = viewModel.state
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        CustomTopAppBar(headerText = category,
            actionIconRes = R.drawable.ic_sort_language,
            onBackButtonClicked = { navController.navigateUp() },
            onActionClicked = {
                coroutineScope.launch {
                    if (!bottomSheetState.isVisible) {
                        bottomSheetState.show()
                    } else {
                        bottomSheetState.hide()
                    }
                }
            })
    }, content = {
        LaunchedEffect(key1 = true, block = { viewModel.loadBookByCategory(category) })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(it)
        ) {
            if (state.page == 1L && state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (!state.isLoading && state.error != null) {
                NetworkError(onRetryClicked = { viewModel.reloadItems() })
            } else if (!state.isLoading && state.items.isEmpty()) {
                NoBooksAvailable(
                    text = stringResource(id = R.string.no_books_found_for_lang_and_cat)
                        .format(viewModel.language.value.name.lowercase(Locale.getDefault()))
                )
            } else {
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
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

package com.starry.myne.ui.screens.home.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.starry.myne.R
import com.starry.myne.others.BookLanguages
import com.starry.myne.others.NetworkObserver
import com.starry.myne.ui.common.BookItemCard
import com.starry.myne.ui.common.ProgressDots
import com.starry.myne.ui.navigation.Screens
import com.starry.myne.ui.screens.home.viewmodels.HomeViewModel
import com.starry.myne.ui.screens.home.viewmodels.UserAction
import com.starry.myne.ui.screens.other.NetworkErrorView
import com.starry.myne.ui.theme.figeronaFont
import com.starry.myne.ui.theme.pacificoFont
import com.starry.myne.utils.BookUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalCoilApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun HomeScreen(navController: NavController, networkStatus: NetworkObserver.Status) {

    val viewModel: HomeViewModel = hiltViewModel()

    /*
     Block back button press if search bar is visible to avoid
     app from closing immediately, instead disable search bar
     on first back press, and close app on second.
     */
    val sysBackButtonState = remember { mutableStateOf(false) }
    BackHandler(enabled = sysBackButtonState.value) {
        if (viewModel.topBarState.isSearchBarVisible) {
            if (viewModel.topBarState.searchText.isNotEmpty()) {
                viewModel.onAction(UserAction.TextFieldInput("", networkStatus))
            } else {
                viewModel.onAction(UserAction.CloseIconClicked)
            }
        }
    }

    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = androidx.compose.material.rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    )

    androidx.compose.material.BottomSheetScaffold(scaffoldState = bottomSheetScaffoldState,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetPeekHeight = 0.dp,
        sheetElevation = 24.dp,
        sheetBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        sheetContent = {
            val languages = BookLanguages.getAllLanguages()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 72.dp)
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
                        LanguageItem(language = language, onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onAction(
                                UserAction.LanguageItemClicked(language)
                            )
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                            }
                        })
                    }
                }
            }

        }) {
        HomeScreenScaffold(
            viewModel = viewModel,
            networkStatus = networkStatus,
            navController = navController,
            coroutineScope = coroutineScope,
            sysBackButtonState = sysBackButtonState,
            bottomSheetScaffoldState = bottomSheetScaffoldState
        )
    }

}


@ExperimentalCoilApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun HomeScreenScaffold(
    viewModel: HomeViewModel,
    networkStatus: NetworkObserver.Status,
    navController: NavController,
    coroutineScope: CoroutineScope,
    sysBackButtonState: MutableState<Boolean>,
    bottomSheetScaffoldState: androidx.compose.material.BottomSheetScaffoldState
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val allBooksState = viewModel.allBooksState
    val topBarState = viewModel.topBarState

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 8.dp)
            ) {
                Crossfade(
                    targetState = topBarState.isSearchBarVisible,
                    animationSpec = tween(durationMillis = 200)
                ) {
                    if (it) {
                        SearchAppBar(onCloseIconClicked = {
                            viewModel.onAction(UserAction.CloseIconClicked)
                        }, onInputValueChange = { newText ->
                            viewModel.onAction(
                                UserAction.TextFieldInput(newText, networkStatus)
                            )
                        }, text = topBarState.searchText, onSearchClicked = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        })
                        sysBackButtonState.value = true
                    } else {
                        HomeTopAppBar(bookLanguages = viewModel.language.value,
                            onSearchIconClicked = {
                                viewModel.onAction(UserAction.SearchIconClicked)
                            }, onSortIconClicked = {
                                coroutineScope.launch {
                                    if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                                        bottomSheetScaffoldState.bottomSheetState.expand()
                                    } else {
                                        bottomSheetScaffoldState.bottomSheetState.collapse()
                                    }
                                }
                            })
                        sysBackButtonState.value = false
                    }
                }
                Divider(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    thickness = 2.dp,
                )
            }
        },
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 70.dp)
            ) {

                // If search text is empty show list of all books.
                if (topBarState.searchText.isBlank()) {
                    // show fullscreen progress indicator when loading the first page.
                    if (allBooksState.page == 1L && allBooksState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (allBooksState.error != null) {
                        NetworkErrorView(onRetryClicked = { viewModel.reloadItems() })
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(start = 8.dp, end = 8.dp)
                        ) {
                            items(allBooksState.items.size) { i ->
                                val item = allBooksState.items[i]
                                if (networkStatus == NetworkObserver.Status.Available
                                    && i >= allBooksState.items.size - 1
                                    && !allBooksState.endReached
                                    && !allBooksState.isLoading
                                ) {
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
                                        subjects = BookUtils.getSubjectsAsString(
                                            item.subjects, 3
                                        ),
                                        coverImageUrl = item.formats.imagejpeg
                                    ) {
                                        navController.navigate(
                                            Screens.BookDetailScreen.withBookId(
                                                item.id.toString()
                                            )
                                        )
                                    }
                                }

                            }
                            item {
                                if (allBooksState.isLoading) {
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

                    // Else show the search results.
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(start = 8.dp, end = 8.dp)
                    ) {
                        item {
                            if (topBarState.isSearching) {
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

                        items(topBarState.searchResults.size) { i ->
                            val item = topBarState.searchResults[i]
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
                                    subjects = BookUtils.getSubjectsAsString(
                                        item.subjects, 3
                                    ),
                                    coverImageUrl = item.formats.imagejpeg
                                ) {
                                    navController.navigate(
                                        Screens.BookDetailScreen.withBookId(
                                            item.id.toString()
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTopAppBar(
    bookLanguages: BookLanguages,
    onSearchIconClicked: () -> Unit,
    onSortIconClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (bookLanguages == BookLanguages.AllBooks)
                stringResource(id = R.string.home_header) else bookLanguages.name,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = pacificoFont
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onSortIconClicked) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_sort_language),
                contentDescription = stringResource(id = R.string.home_language_icon_desc),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(30.dp)
            )
        }
        IconButton(onClick = onSearchIconClicked) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_search),
                contentDescription = stringResource(id = R.string.home_search_icon_desc),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun SearchAppBar(
    onCloseIconClicked: () -> Unit,
    onInputValueChange: (String) -> Unit,
    text: String,
    onSearchClicked: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        value = text,
        onValueChange = {
            onInputValueChange(it)
        },
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp
        ),
        placeholder = {
            Text(
                text = "Search...",
                fontFamily = figeronaFont,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = ContentAlpha.medium)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = ContentAlpha.medium
                )
            )
        },
        trailingIcon = {
            IconButton(onClick = {
                if (text.isNotEmpty()) {
                    onInputValueChange("")
                } else {
                    onCloseIconClicked()
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close Icon",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                alpha = ContentAlpha.medium
            ),
            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
            cursorColor = MaterialTheme.colorScheme.onBackground,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchClicked() }),
        shape = RoundedCornerShape(16.dp)
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@ExperimentalMaterial3Api
@Composable
fun LanguageItem(language: BookLanguages, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(60.dp)
            .width(70.dp)
            .padding(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.padding(2.dp),
                text = language.name,
                fontSize = 18.sp,
                fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                fontFamily = figeronaFont,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
@Preview(showBackground = true)
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController(), NetworkObserver.Status.Unavailable)
}
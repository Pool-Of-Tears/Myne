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

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.starry.myne.R
import com.starry.myne.navigation.Screens
import com.starry.myne.others.NetworkObserver
import com.starry.myne.ui.common.BookItemCard
import com.starry.myne.ui.common.ProgressDots
import com.starry.myne.ui.theme.figeronaFont
import com.starry.myne.ui.theme.pacificoFont
import com.starry.myne.ui.viewmodels.HomeViewModel
import com.starry.myne.ui.viewmodels.UserAction
import com.starry.myne.utils.BookUtils
import kotlinx.coroutines.delay

@ExperimentalCoilApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun HomeScreen(navController: NavController, networkStatus: NetworkObserver.Status) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    if (networkStatus == NetworkObserver.Status.Available) {
        val viewModel = viewModel<HomeViewModel>()
        val allBooksState = viewModel.allBooksState
        val topBarState = viewModel.topBarState

        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 70.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
            ) {
                Crossfade(
                    targetState = topBarState.isSearchBarVisible,
                    animationSpec = tween(durationMillis = 200)
                ) {
                    if (it) {
                        SearchAppBar(
                            onCloseIconClicked = {
                                viewModel.onAction(UserAction.CloseIconClicked)
                            },
                            onInputValueChange = { newText ->
                                viewModel.onAction(
                                    UserAction.TextFieldInput(newText)
                                )
                            },
                            text = topBarState.searchText,
                            onSearchClicked = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        )
                    } else {
                        HomeTopAppBar(
                            onSearchIconClicked = {
                                viewModel.onAction(UserAction.SearchIconClicked)
                            },
                        )
                    }
                }
                Divider(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    thickness = 2.dp,
                    // modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // If search text is empty show list of all books.
            if (topBarState.searchText.isBlank()) {
                // show fullscreen progress indicator when loading the first page.
                if (allBooksState.page == 1L && allBooksState.isLoading) {
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
                        items(allBooksState.items.size) { i ->
                            val item = allBooksState.items[i]
                            if (i >= allBooksState.items.size - 1 && !allBooksState.endReached && !allBooksState.isLoading) {
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
                }
            }
        }
    } else {
        /*
        Show no internet screen with some delay to avoid flashing
        it for a moment when app was launched and internet connection
        was available.
        */
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


@Composable
fun HomeTopAppBar(
    onSearchIconClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = R.string.home_header),
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = pacificoFont
        )
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
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp
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
            IconButton(
                onClick = {
                    if (text.isNotEmpty()) {
                        onInputValueChange("")
                    } else {
                        onCloseIconClicked()
                    }
                }
            ) {
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
        keyboardActions = KeyboardActions(
            onSearch = { onSearchClicked() }
        ),
        shape = RoundedCornerShape(16.dp)
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
@Preview(showBackground = true)
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController(), NetworkObserver.Status.Unavailable)
}
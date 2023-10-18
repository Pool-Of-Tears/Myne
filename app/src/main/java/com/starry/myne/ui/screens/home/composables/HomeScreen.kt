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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
import com.starry.myne.others.BookLanguage
import com.starry.myne.others.NetworkObserver
import com.starry.myne.ui.common.BookItemCard
import com.starry.myne.ui.common.ProgressDots
import com.starry.myne.ui.navigation.Screens
import com.starry.myne.ui.screens.home.viewmodels.HomeViewModel
import com.starry.myne.ui.screens.home.viewmodels.UserAction
import com.starry.myne.ui.screens.other.NetworkError
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
                        LanguageItem(
                            language = language,
                            isSelected = language == viewModel.language.value,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onAction(
                                    UserAction.LanguageItemClicked(language)
                                )
                                coroutineScope.launch {
                                    modalBottomSheetState.hide()
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
            bottomSheetState = modalBottomSheetState
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
    bottomSheetState: ModalBottomSheetState
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
                        HomeTopAppBar(bookLanguage = viewModel.language.value,
                            onSearchIconClicked = {
                                viewModel.onAction(UserAction.SearchIconClicked)
                            }, onLanguageIconClicked = {
                                coroutineScope.launch {
                                    if (!bottomSheetState.isVisible) {
                                        bottomSheetState.show()
                                    } else {
                                        bottomSheetState.hide()
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
                    } else if (!allBooksState.isLoading && allBooksState.error != null) {
                        NetworkError(onRetryClicked = { viewModel.reloadItems() })
                    } else {
                        LazyVerticalGrid(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(start = 8.dp, end = 8.dp),
                            columns = GridCells.Adaptive(295.dp)
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
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(start = 8.dp, end = 8.dp),
                        columns = GridCells.Adaptive(295.dp)
                    ) {
                        if (topBarState.isSearching) {
                            item {
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
    bookLanguage: BookLanguage,
    onSearchIconClicked: () -> Unit,
    onLanguageIconClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (bookLanguage == BookLanguage.AllBooks)
                stringResource(id = R.string.home_header) else bookLanguage.name,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = pacificoFont
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onLanguageIconClicked) {
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
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = MaterialTheme.colorScheme.onBackground,
            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                alpha = ContentAlpha.medium
            ),
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
fun LanguageItem(language: BookLanguage, isSelected: Boolean, onClick: () -> Unit) {
    val buttonColor: Color
    val textColor: Color
    if (isSelected) {
        buttonColor = MaterialTheme.colorScheme.primary
        textColor = MaterialTheme.colorScheme.onPrimary
    } else {
        buttonColor = MaterialTheme.colorScheme.secondaryContainer
        textColor = MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = Modifier
            .height(60.dp)
            .width(70.dp)
            .padding(6.dp),
        colors = CardDefaults.cardColors(containerColor = buttonColor),
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
                color = textColor,
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
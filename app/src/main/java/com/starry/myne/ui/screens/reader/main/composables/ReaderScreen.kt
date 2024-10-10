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


package com.starry.myne.ui.screens.reader.main.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.starry.myne.ui.screens.reader.main.viewmodel.ReaderScreenState
import com.starry.myne.ui.screens.reader.main.viewmodel.ReaderViewModel
import com.starry.myne.ui.theme.poppinsFont
import kotlinx.coroutines.launch


@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onScrollToChapter: suspend (Int) -> Unit,
    chaptersContent: @Composable (paddingValues: PaddingValues) -> Unit
) {
    val state = viewModel.state.collectAsState().value
    // Hide reader menu on back press.
    BackHandler(state.showReaderMenu) {
        viewModel.hideReaderInfo()
    }

    val showFontDialog = remember { mutableStateOf(false) }
    ReaderFontChooserDialog(
        showFontDialog = showFontDialog,
        fontFamily = state.fontFamily,
        onFontFamilyChanged = { viewModel.setFontFamily(it) }
    )

    val snackBarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BackHandler(drawerState.isOpen) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        }
    }

    ChaptersDrawer(
        drawerState = drawerState,
        chapters = state.chapters,
        currentChapterIndex = state.currentChapterIndex,
        onScrollToChapter = { coroutineScope.launch { onScrollToChapter(it) } },
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackBarHostState) },
            topBar = {
                ReaderTopAppBar(
                    state = state,
                    onChapterListClicked = { coroutineScope.launch { drawerState.open() } }
                )
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = state.showReaderMenu,
                    enter = expandVertically(initialHeight = { 0 }) + fadeIn(),
                    exit = shrinkVertically(targetHeight = { 0 }) + fadeOut(),
                ) {
                    ReaderBottomBar(
                        state = state,
                        showFontDialog = showFontDialog,
                        snackBarHostState = snackBarHostState,
                        onFontSizeChanged = { viewModel.setFontSize(it) },
                    )
                }
            },
            content = { paddingValues ->
                Crossfade(
                    targetState = state.isLoading,
                    label = "reader content loading cross fade"
                ) { loading ->
                    if (loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 65.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.shouldShowLoader) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    } else {
                        chaptersContent(paddingValues)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTopAppBar(
    state: ReaderScreenState,
    onChapterListClicked: () -> Unit,
) {
    AnimatedVisibility(
        visible = state.showReaderMenu,
        enter = expandVertically(initialHeight = { 0 }, expandFrom = Alignment.Top)
                + fadeIn(),
        exit = shrinkVertically(targetHeight = { 0 }, shrinkTowards = Alignment.Top)
                + fadeOut(),
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)) {
            Column(modifier = Modifier.displayCutoutPadding()) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                            2.dp
                        ),
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                            2.dp
                        ),
                    ),
                    title = {
                        Text(
                            text = state.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.animateContentSize(),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = poppinsFont
                        )

                    },
                    actions = {
                        IconButton(onClick = onChapterListClicked) {
                            Icon(
                                Icons.Filled.Menu, null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
                Column(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = state.currentChapter.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = poppinsFont,
                        fontWeight = FontWeight.Medium
                    )
                }
                val chapterProgressbarState = animateFloatAsState(
                    targetValue = state.chapterScrollPercent,
                    label = "chapter progress bar state animation"
                )
                LinearProgressIndicator(
                    progress = { chapterProgressbarState.value },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

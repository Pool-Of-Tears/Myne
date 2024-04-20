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


package com.starry.myne.ui.screens.reader.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.starry.myne.R
import com.starry.myne.ui.screens.reader.viewmodels.ReaderFont
import com.starry.myne.ui.screens.reader.viewmodels.ReaderViewModel
import com.starry.myne.ui.screens.settings.viewmodels.SettingsViewModel
import com.starry.myne.ui.screens.settings.viewmodels.ThemeMode
import com.starry.myne.ui.theme.figeronaFont
import kotlinx.coroutines.launch


enum class TextScaleButtonType { INCREASE, DECREASE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    lazyListState: LazyListState,
    readerContent: @Composable (paddingValues: PaddingValues) -> Unit
) {
    // Hide reader menu on back press.
    BackHandler(viewModel.state.showReaderMenu) {
        viewModel.hideReaderInfo()
    }

    // Font style chooser dialog.
    val showFontDialog = remember { mutableStateOf(false) }
    FontChooserDialog(showFontDialog, viewModel)

    val snackBarHostState = remember { SnackbarHostState() }
    val currentChapter =
        viewModel.state.epubBook?.chapters?.getOrNull(viewModel.visibleChapterIndex.value)

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val chapters = viewModel.state.epubBook?.chapters
    val coroutineScope = rememberCoroutineScope()

    BackHandler(drawerState.isOpen) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(14.dp))

                Text(
                    text = stringResource(id = R.string.reader_chapter_list_title),
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = figeronaFont,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )

                chapters?.let { chapters ->
                    LazyColumn {
                        items(chapters.size) { idx ->
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = chapters[idx].title,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                selected = idx == viewModel.visibleChapterIndex.value,
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.close()
                                        lazyListState.scrollToItem(idx)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }) {

        Scaffold(
            snackbarHost = { SnackbarHost(snackBarHostState) },
            topBar = {
                AnimatedVisibility(
                    visible = viewModel.state.showReaderMenu,
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
                                    viewModel.state.epubBook?.let {
                                        Text(
                                            text = it.title,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.animateContentSize(),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = figeronaFont
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
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
                                currentChapter?.title?.let {
                                    Text(
                                        text = it,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = figeronaFont,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            val chapterProgressbarState = animateFloatAsState(
                                targetValue = viewModel.chapterScrolledPercent.value,
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
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = viewModel.state.showReaderMenu,
                    enter = expandVertically(initialHeight = { 0 }) + fadeIn(),
                    exit = shrinkVertically(targetHeight = { 0 }) + fadeOut(),
                ) {
                    BottomSheetContents(
                        viewModel = viewModel,
                        showFontDialog = showFontDialog,
                        snackBarHostState = snackBarHostState
                    )
                }
            },
            content = { paddingValues ->
                Crossfade(
                    targetState = viewModel.state.isLoading,
                    label = "reader content loading cross fade"
                ) { loading ->
                    if (loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 65.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        readerContent(paddingValues)
                    }
                }
            }
        )
    }
}

@Composable
private fun FontChooserDialog(
    showFontDialog: MutableState<Boolean>,
    viewModel: ReaderViewModel,
) {
    val radioOptions = ReaderFont.getAllFonts().map { it.name }
    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(viewModel.getFontFamily())
    }

    if (showFontDialog.value) {
        AlertDialog(onDismissRequest = {
            showFontDialog.value = false
        }, title = {
            Text(
                text = stringResource(id = R.string.reader_font_style_chooser),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }, text = {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.Center,
            ) {
                radioOptions.forEach { text ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .selectable(
                                selected = (text == selectedOption.name),
                                onClick = { onOptionSelected(ReaderFont.getFontByName(text)) },
                                role = Role.RadioButton,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (text == selectedOption.name),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.inversePrimary,
                                disabledSelectedColor = Color.Black,
                                disabledUnselectedColor = Color.Black
                            ),
                        )
                        Text(
                            text = text,
                            modifier = Modifier.padding(start = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = figeronaFont
                        )
                    }
                }
            }
        }, confirmButton = {
            TextButton(onClick = {
                showFontDialog.value = false
                viewModel.setFontFamily(selectedOption)
            }) {
                Text(stringResource(id = R.string.theme_dialog_apply_button))
            }
        }, dismissButton = {
            TextButton(onClick = {
                showFontDialog.value = false
            }) {
                Text(stringResource(id = R.string.cancel))
            }
        })
    }
}


@Composable
private fun BottomSheetContents(
    viewModel: ReaderViewModel,
    showFontDialog: MutableState<Boolean>,
    snackBarHostState: SnackbarHostState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        TextScaleControls(
            viewModel = viewModel,
            snackBarHostState = snackBarHostState
        )

        Spacer(modifier = Modifier.height(14.dp))

        FontSelectionButton(
            readerFontFamily = viewModel.state.fontFamily,
            showFontDialog = showFontDialog
        )
    }
}


@Composable
private fun TextScaleControls(
    viewModel: ReaderViewModel,
    snackBarHostState: SnackbarHostState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        ReaderTextScaleButton(
            buttonType = TextScaleButtonType.DECREASE,
            fontSize = viewModel.state.fontSize,
            snackBarHostState = snackBarHostState,
            onFontSizeChanged = { viewModel.setFontSize(it) }
        )

        Spacer(modifier = Modifier.width(14.dp))

        Box(
            modifier = Modifier
                .size(100.dp, 45.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ButtonDefaults.filledTonalButtonColors().containerColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = viewModel.state.fontSize.toString(),
                fontFamily = figeronaFont,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 2.dp, bottom = 1.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        ReaderTextScaleButton(
            buttonType = TextScaleButtonType.INCREASE,
            fontSize = viewModel.state.fontSize,
            snackBarHostState = snackBarHostState,
            onFontSizeChanged = { viewModel.setFontSize(it) }
        )
    }
}

@Composable
private fun FontSelectionButton(
    readerFontFamily: ReaderFont,
    showFontDialog: MutableState<Boolean>
) {
    FilledTonalButton(
        onClick = { showFontDialog.value = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_reader_font),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = readerFontFamily.name,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
private fun ReaderTextScaleButton(
    buttonType: TextScaleButtonType,
    fontSize: Int,
    snackBarHostState: SnackbarHostState,
    onFontSizeChanged: (newValue: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val (iconRes, adjustment) = remember(buttonType) {
        when (buttonType) {
            TextScaleButtonType.DECREASE -> Pair(R.drawable.ic_reader_text_minus, -10)
            TextScaleButtonType.INCREASE -> Pair(R.drawable.ic_reader_text_plus, 10)
        }
    }

    val callback: () -> Unit = {
        val newSize = fontSize + adjustment
        when {
            newSize < 50 -> {
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(
                        context.getString(R.string.reader_min_font_size_reached),
                        null
                    )
                }
            }

            newSize > 200 -> {
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(
                        context.getString(R.string.reader_max_font_size_reached),
                        null
                    )
                }
            }

            else -> {
                coroutineScope.launch {
                    val adjustedSize = fontSize + adjustment
                    onFontSizeChanged(adjustedSize)
                }
            }
        }
    }

    FilledTonalButton(
        onClick = { callback() },
        modifier = Modifier.size(100.dp, 45.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
    }
}


@Composable
fun TransparentSystemBars(alpha: Float = 0f, settingsViewModel: SettingsViewModel) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = settingsViewModel.getCurrentTheme() == ThemeMode.Light
    val baseColor = MaterialTheme.colorScheme.primary
    val color = remember(alpha, baseColor) { baseColor.copy(alpha = alpha) }
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = color,
            darkIcons = useDarkIcons
        )
    }
}
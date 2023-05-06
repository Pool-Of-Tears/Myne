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


package com.starry.myne.ui.screens.reader.composables

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedButton
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.starry.myne.R
import com.starry.myne.epub.BookTextMapper
import com.starry.myne.epub.models.EpubBook
import com.starry.myne.epub.models.EpubChapter
import com.starry.myne.ui.screens.reader.viewmodels.ReaderFont
import com.starry.myne.ui.screens.reader.viewmodels.ReaderViewModel
import com.starry.myne.ui.theme.figeronaFont
import com.starry.myne.utils.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


enum class TextScaleButtonType { INCREASE, DECREASE }

@ExperimentalMaterialApi
@Composable
fun ReaderScreen(bookId: String, chapterIndex: Int) {
    val viewModel: ReaderViewModel = hiltViewModel()
    val state = viewModel.state
    val context = LocalContext.current

    LaunchedEffect(key1 = true, block = { viewModel.loadEpubBook(bookId) })

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    )

    val textSizeValue =
        remember {
            mutableStateOf(
                PreferenceUtil.getInt(
                    PreferenceUtil.READER_FONT_SIZE_INT,
                    100
                )
            )
        }
    val textSize = (textSizeValue.value / 10) * 1.8
    val readerFontFamily = remember { mutableStateOf(viewModel.getFontFamily()) }

    // Font style chooser dialog.
    val showFontDialog = remember { mutableStateOf(false) }
    val radioOptions = ReaderFont.getAllFonts().map { it.name }
    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(viewModel.getFontFamily())
    }

    if (showFontDialog.value) {
        AlertDialog(onDismissRequest = {
            showFontDialog.value = false
        }, title = {
            Text(
                text = stringResource(id = R.string.reader_font_style_chooer),
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
                readerFontFamily.value = selectedOption
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

    BottomSheetScaffold(scaffoldState = bottomSheetScaffoldState,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetPeekHeight = 0.dp,
        sheetContent = {
            BottomSheetContents(
                context = context,
                textSizeValue = textSizeValue,
                readerFontFamily = readerFontFamily,
                showFontDialog = showFontDialog,
                coroutineScope = coroutineScope,
                bottomSheetScaffoldState = bottomSheetScaffoldState
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 65.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(state = lazyListState) {
                        items(state.epubBook!!.chapters.size,
                            key = { state.epubBook.chapters[it].title },
                            contentType = { null }) { idx ->
                            val chapter = state.epubBook.chapters[idx]
                            ReaderItem(chapter = chapter,
                                epubBook = state.epubBook,
                                textSize = textSize.sp,
                                fontFamily = readerFontFamily.value.fontFamily,
                                onClick = {
                                    coroutineScope.launch {
                                        if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                                            bottomSheetScaffoldState.bottomSheetState.expand()
                                        } else {
                                            bottomSheetScaffoldState.bottomSheetState.collapse()
                                        }
                                    }
                                })
                        }
                    }

                    // Update reader progress.
                    LaunchedEffect(key1 = !lazyListState.isScrollInProgress, block = {
                        /*
                         For some weird reason when using lazyListState.scrollToItem()
                         to automatically scroll to last viewed item position, the
                         lazyListState.firstVisibleItemIndex remains 0 even if we are
                         not showing the item at first index right now, however it gets updated
                         to current position when user scrolls for the first time.
                         */
                        if (lazyListState.firstVisibleItemIndex == 0 && state.readerItem != null) {
                            viewModel.updateReaderProgress(
                                bookId.toInt(),
                                state.readerItem.lastChapterIndex,
                                lazyListState.firstVisibleItemScrollOffset
                            )
                        } else {
                            viewModel.updateReaderProgress(
                                bookId.toInt(),
                                lazyListState.firstVisibleItemIndex,
                                lazyListState.firstVisibleItemScrollOffset
                            )
                        }
                    })

                    // Scroll to last read chapter or some specific chapter
                    // depending on user's selection.
                    LaunchedEffect(key1 = true, block = {
                        if (chapterIndex != -1) {
                            lazyListState.scrollToItem(chapterIndex, 0)
                        } else if (state.readerItem != null) {
                            lazyListState.scrollToItem(
                                state.readerItem.lastChapterIndex,
                                state.readerItem.lastChapterOffset
                            )
                        }
                    })
                }
            }
        })
}


@Composable
fun ReaderItem(
    chapter: EpubChapter,
    epubBook: EpubBook,
    textSize: TextUnit,
    fontFamily: FontFamily,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val paragraphs = chapter.body.splitToSequence("\n\n").filter { it.isNotBlank() }.toList()

    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable(interactionSource = interactionSource, indication = null) { onClick() }) {

        Text(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 10.dp),
            text = chapter.title,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f)
        )

        Spacer(modifier = Modifier.height(15.dp))

        paragraphs.forEach { para ->
            when (val imgEntry = BookTextMapper.ImgEntry.fromXMLString(para)) {
                null -> {
                    Text(
                        text = para,
                        fontSize = textSize,
                        lineHeight = (textSize * 4) / 3,
                        fontFamily = fontFamily,
                        modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 8.dp),
                    )
                }

                else -> {
                    val image = epubBook.images.find { it.absPath == imgEntry.path }
                    image?.let {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(image.image)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Divider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
            thickness = 2.dp,
        )
    }
}


@ExperimentalMaterialApi
@Composable
fun BottomSheetContents(
    context: Context,
    textSizeValue: MutableState<Int>,
    readerFontFamily: MutableState<ReaderFont>,
    showFontDialog: MutableState<Boolean>,
    coroutineScope: CoroutineScope,
    bottomSheetScaffoldState: BottomSheetScaffoldState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 21.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            ReaderTextScaleButton(
                context = context,
                buttonType = TextScaleButtonType.DECREASE,
                textSizeValue = textSizeValue,
                coroutineScope = coroutineScope,
                bottomSheetScaffoldState = bottomSheetScaffoldState
            )
            Spacer(modifier = Modifier.width(14.dp))
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(45.dp)
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                    .border(
                        1.dp, MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(6.dp)
                    )
                    .clip(RoundedCornerShape(6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_reader_text_size),
                            contentDescription = null,
                            //    modifier = Modifier.size(size = 15.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = textSizeValue.value.toString(),
                            fontFamily = figeronaFont,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 2.dp, bottom = 1.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            ReaderTextScaleButton(
                context = context,
                buttonType = TextScaleButtonType.INCREASE,
                textSizeValue = textSizeValue,
                coroutineScope = coroutineScope,
                bottomSheetScaffoldState = bottomSheetScaffoldState
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Divider()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            OutlinedButton(
                onClick = { showFontDialog.value = true },
                modifier = Modifier.width(325.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        2.dp
                    )
                )
            ) {
                Row {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_reader_font),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = readerFontFamily.value.name)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@ExperimentalMaterialApi
@Composable
fun ReaderTextScaleButton(
    context: Context,
    buttonType: TextScaleButtonType,
    textSizeValue: MutableState<Int>,
    coroutineScope: CoroutineScope,
    bottomSheetScaffoldState: BottomSheetScaffoldState
) {
    val iconRes: Int
    val callback: () -> Unit
    when (buttonType) {
        TextScaleButtonType.DECREASE -> {
            iconRes = R.drawable.ic_reader_text_minus
            callback = {
                if (textSizeValue.value <= 50) {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                            context.getString(R.string.reader_min_font_size_reached), null
                        )
                    }
                } else {
                    coroutineScope.launch {
                        textSizeValue.value -= 10
                        PreferenceUtil.putInt(
                            PreferenceUtil.READER_FONT_SIZE_INT,
                            textSizeValue.value
                        )
                    }
                }
            }
        }

        TextScaleButtonType.INCREASE -> {
            iconRes = R.drawable.ic_reader_text_plus
            callback = {
                if (textSizeValue.value >= 200) {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                            context.getString(R.string.reader_max_font_size_reached), null
                        )
                    }
                } else {
                    coroutineScope.launch {
                        textSizeValue.value += 10
                        PreferenceUtil.putInt(
                            PreferenceUtil.READER_FONT_SIZE_INT,
                            textSizeValue.value
                        )
                    }
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(45.dp)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            .border(
                1.dp, MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(6.dp)
            )
            .clip(RoundedCornerShape(6.dp))
            .clickable { callback() }, contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = iconRes),
            contentDescription = stringResource(id = R.string.back_button_desc),
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(14.dp)
        )
    }
}


@ExperimentalMaterialApi
@Composable
@Preview
fun BottomSheetContentsPV() {
    val textValue = remember { mutableStateOf(100) }
    BottomSheetContents(
        context = LocalContext.current,
        textSizeValue = textValue,
        readerFontFamily = remember { mutableStateOf(ReaderFont.System) },
        showFontDialog = remember { mutableStateOf(false) },
        coroutineScope = rememberCoroutineScope(),
        bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    )
}
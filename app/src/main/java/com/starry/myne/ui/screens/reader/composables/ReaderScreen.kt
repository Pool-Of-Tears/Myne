package com.starry.myne.ui.screens.reader.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.starry.myne.R
import com.starry.myne.ui.screens.reader.viewmodels.ReaderViewModel
import com.starry.myne.ui.theme.figeronaFont
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun ReaderScreen(bookId: String, chapterIndex: Int) {
    val viewModel: ReaderViewModel = hiltViewModel()
    viewModel.loadEpubBook(bookId)
    val state = viewModel.state

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    )

    val textSizeValue = remember { mutableStateOf(100) }
    val textSize = (textSizeValue.value / 10) * 1.8

    BottomSheetScaffold(scaffoldState = bottomSheetScaffoldState,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetPeekHeight = 0.dp,
        sheetContent = {
            BottomSheetContents(
                textSizeValue = textSizeValue,
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
                        items(state.epubBook!!.chapters.size, key = {
                            state.epubBook.chapters[it].title
                        }) { idx ->
                            val chapter = state.epubBook.chapters[idx]
                            ReaderItem(title = chapter.title,
                                body = chapter.body,
                                textSize = textSize.sp,
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
fun ReaderItem(title: String, body: String, textSize: TextUnit, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable(interactionSource = interactionSource, indication = null) { onClick() }) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(14.dp))

            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_reader_cat),
                contentDescription = null,
                modifier = Modifier
                    .size(58.dp)
                    .padding(top = 4.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                modifier = Modifier.padding(end = 4.dp),
                text = title,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = body,
            fontSize = textSize,
            lineHeight = (textSize * 4) / 3,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.padding(start = 14.dp, end = 14.dp),
        )

        Divider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
            thickness = 2.dp,
        )
    }
}


@ExperimentalMaterialApi
@Composable
fun BottomSheetContents(
    textSizeValue: MutableState<Int>,
    coroutineScope: CoroutineScope,
    bottomSheetScaffoldState: BottomSheetScaffoldState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .height(240.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 21.dp),
            horizontalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier
                    .width(115.dp)
                    .height(54.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        if (textSizeValue.value <= 50) {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                                    "No", null
                                )
                            }
                        } else {
                            textSizeValue.value -= 10
                        }
                    }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_reader_text_minus),
                    contentDescription = stringResource(id = R.string.back_button_desc),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Card(
                modifier = Modifier
                    .height(54.dp)
                    .width(115.dp), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ), shape = RoundedCornerShape(6.dp)
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
                            tint = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = textSizeValue.value.toString(),
                            fontFamily = figeronaFont,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 2.dp, bottom = 1.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Box(
                modifier = Modifier
                    .width(115.dp)
                    .height(54.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        if (textSizeValue.value >= 200) {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                                    "No", null
                                )
                            }
                        } else {
                            textSizeValue.value += 10
                        }
                    }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_reader_text_plus),
                    contentDescription = stringResource(id = R.string.back_button_desc),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}


@ExperimentalMaterialApi
@Composable
@Preview
fun BottomSheetContentsPV() {
    val textValue = remember { mutableStateOf(100) }
    BottomSheetContents(
        textSizeValue = textValue,
        coroutineScope = rememberCoroutineScope(),
        bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    )
}
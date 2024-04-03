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

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.starry.myne.MainActivity
import com.starry.myne.R
import com.starry.myne.ui.common.CustomTopAppBar
import com.starry.myne.ui.common.ProgressDots
import com.starry.myne.ui.common.simpleVerticalScrollbar
import com.starry.myne.ui.screens.reader.activities.ReaderActivity
import com.starry.myne.ui.screens.reader.activities.ReaderConstants
import com.starry.myne.ui.screens.reader.viewmodels.ReaderDetailViewModel
import com.starry.myne.ui.screens.settings.viewmodels.ThemeMode
import com.starry.myne.ui.theme.figeronaFont
import com.starry.myne.utils.NetworkObserver
import com.starry.myne.utils.getActivity
import com.starry.myne.utils.toToast

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@Composable
fun ReaderDetailScreen(
    bookId: String,
    navController: NavController,
    networkStatus: NetworkObserver.Status
) {
    val viewModel: ReaderDetailViewModel = hiltViewModel()
    val state = viewModel.state

    LaunchedEffect(key1 = true) { viewModel.loadEbookData(bookId, networkStatus) }

    val context = LocalContext.current
    val settingsVM = (context.getActivity() as MainActivity).settingsViewModel

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 65.dp),
            contentAlignment = Alignment.Center
        ) {
            ProgressDots()
        }
    } else if (state.error != null) {
        stringResource(id = R.string.error).toToast(context)
    } else {
        // Collect saved reader progress for the current book.
        val readerItem = viewModel.readerData?.collectAsState(initial = null)?.value

        Scaffold(topBar = {
            CustomTopAppBar(headerText = stringResource(id = R.string.reader_detail_header)) {
                navController.navigateUp()
            }
        }, floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(id = if (readerItem != null) R.string.continue_reading_button else R.string.start_reading_button)) },
                onClick = {
                    val intent = Intent(context, ReaderActivity::class.java)
                    intent.putExtra(ReaderConstants.EXTRA_BOOK_ID, bookId.toInt())
                    context.startActivity(intent)
                },
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_reader_fab_button),
                        contentDescription = null
                    )
                },
                modifier = Modifier.padding(end = 10.dp, bottom = 8.dp),
            )
        }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(it)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    AsyncImage(
                        model = R.drawable.book_reader_bg,
                        contentDescription = null,
                        alpha = 0.35f,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.background,
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background
                                    ), startY = 15f
                                )
                            )
                    )

                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val imageBackground =
                                if (settingsVM.getCurrentTheme() == ThemeMode.Dark) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                                }

                            Box(
                                modifier = Modifier
                                    .shadow(24.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(imageBackground)
                            ) {
                                if (state.ebookData!!.coverImage != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(state.ebookData.coverImage).crossfade(true)
                                            .build(),
                                        placeholder = painterResource(id = R.drawable.placeholder_cat),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .width(118.dp)
                                            .height(169.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                } else if (state.ebookData.epubBook.coverImage != null) {
                                    Image(
                                        bitmap = state.ebookData.epubBook.coverImage.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .width(118.dp)
                                            .height(169.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.placeholder_cat),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .width(118.dp)
                                            .height(169.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.ebookData!!.title,
                                modifier = Modifier
                                    .padding(
                                        start = 12.dp, end = 12.dp, top = 20.dp
                                    )
                                    .fillMaxWidth(),
                                fontSize = 24.sp,
                                fontFamily = figeronaFont,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )

                            Text(
                                text = state.ebookData.author,
                                modifier = Modifier.padding(
                                    start = 12.dp, end = 8.dp, top = 4.dp
                                ),
                                fontSize = 18.sp,
                                fontFamily = figeronaFont,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground,
                            )

                            if (readerItem != null) {
                                Text(
                                    text = "${readerItem.getProgressPercent(state.ebookData.epubBook.chapters.size)}% Completed",
                                    modifier = Modifier.padding(
                                        start = 12.dp, end = 8.dp, top = 8.dp
                                    ),
                                    fontSize = 16.sp,
                                    fontFamily = figeronaFont,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                            Spacer(modifier = Modifier.height(50.dp))
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(
                        start = 20.dp, end = 20.dp, top = 2.dp, bottom = 2.dp
                    ),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                )

                val lazyListState = rememberLazyListState()
                LazyColumn(
                    state = lazyListState, modifier = Modifier.simpleVerticalScrollbar(
                        lazyListState, color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    items(state.ebookData!!.epubBook.chapters.size) { idx ->
                        val chapter = state.ebookData.epubBook.chapters[idx]
                        ChapterItem(chapterTitle = chapter.title, onClick = {
                            val intent = Intent(context, ReaderActivity::class.java)
                            intent.putExtra(ReaderConstants.EXTRA_BOOK_ID, bookId.toInt())
                            intent.putExtra(ReaderConstants.EXTRA_CHAPTER_IDX, idx)
                            context.startActivity(intent)
                        })
                    }
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun ChapterItem(chapterTitle: String, onClick: () -> Unit) {
    Card(
        onClick = { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                2.dp
            )
        ),
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
        ) {
            Text(
                modifier = Modifier
                    .weight(3f)
                    .padding(start = 12.dp),
                text = chapterTitle,
                fontFamily = figeronaFont,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )

            Icon(
                modifier = Modifier
                    .size(15.dp)
                    .weight(0.4f),
                painter = painterResource(id = R.drawable.ic_right_arrow),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }

}


@ExperimentalCoilApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Preview(showBackground = true)
@Composable
fun EpubDetailScreenPV() {
    ReaderDetailScreen("", rememberNavController(), NetworkObserver.Status.Available)
    //ReaderError(rememberNavController())
}
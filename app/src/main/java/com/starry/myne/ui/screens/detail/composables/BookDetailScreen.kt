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

package com.starry.myne.ui.screens.detail.composables

import android.app.DownloadManager
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.starry.myne.MainActivity
import com.starry.myne.R
import com.starry.myne.helpers.Utils
import com.starry.myne.helpers.book.BookUtils
import com.starry.myne.helpers.getActivity
import com.starry.myne.helpers.weakHapticFeedback
import com.starry.myne.ui.common.BookDetailTopUI
import com.starry.myne.ui.common.NetworkError
import com.starry.myne.ui.common.ProgressDots
import com.starry.myne.ui.screens.detail.viewmodels.BookDetailViewModel
import com.starry.myne.ui.theme.pacificoFont
import com.starry.myne.ui.theme.poppinsFont
import kotlinx.coroutines.launch


@Composable
fun BookDetailScreen(
    bookId: String, navController: NavController
) {
    val context = LocalContext.current
    val viewModel: BookDetailViewModel = hiltViewModel()
    val state = viewModel.state

    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        content = { paddingValues ->
            LaunchedEffect(key1 = true, block = {
                if (state.isLoading) viewModel.getBookDetails(bookId)
            })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                BookDetailTopBar(onBackClicked = {
                    navController.navigateUp()
                }, onShareClicked = {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(
                        Intent.EXTRA_TEXT, "https://www.gutenberg.org/ebooks/$bookId"
                    )
                    val chooser = Intent.createChooser(
                        intent, context.getString(R.string.share_intent_header)
                    )
                    context.startActivity(chooser)
                })

                Crossfade(
                    targetState = state.isLoading,
                    label = "BookDetailLoadingCrossFade"
                ) { isLoading ->
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 65.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ProgressDots()
                        }
                    } else {
                        if (state.error != null) {
                            NetworkError(onRetryClicked = {
                                viewModel.getBookDetails(bookId)
                            })
                        } else {
                            BookDetailContents(
                                viewModel = viewModel,
                                navController = navController,
                                snackBarHostState = snackBarHostState
                            )
                        }
                    }

                }
            }
        })
}

@Composable
private fun BookDetailContents(
    viewModel: BookDetailViewModel,
    navController: NavController,
    snackBarHostState: SnackbarHostState
) {
    val view = LocalView.current
    val context = LocalContext.current
    val settingsVM = (context.getActivity() as MainActivity).settingsViewModel

    val state = viewModel.state
    val coroutineScope = rememberCoroutineScope()
    // Get book details for this bookId.
    val book = remember { state.bookSet.books.first() }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        val authors = remember { BookUtils.getAuthorsAsString(book.authors) }

        BookDetailTopUI(
            title = book.title,
            authors = authors,
            imageData = state.extraInfo.coverImage.ifEmpty { book.formats.imagejpeg },
            currentThemeMode = settingsVM.getCurrentTheme()
        )

        val pageCount = remember {
            if (state.extraInfo.pageCount > 0) {
                state.extraInfo.pageCount.toString()
            } else {
                context.getString(R.string.not_applicable)
            }
        }
        var buttonText by remember { mutableStateOf("") }

        // Update button text based on download status.
        LaunchedEffect(key1 = true) {
            buttonText = if (viewModel.bookDownloader.isBookCurrentlyDownloading(book.id)) {
                context.getString(R.string.cancel)
            } else {
                when (state.bookLibraryItem) {
                    null -> context.getString(R.string.download_book_button)
                    else -> context.getString(R.string.read_book_button)
                }
            }
        }

        var progressState by remember { mutableFloatStateOf(0f) }
        var showProgressBar by remember { mutableStateOf(false) }

        // Callable which updates book details screen button.
        val updateBtnText: (Int?) -> Unit = { downloadStatus ->
            buttonText = when (downloadStatus) {
                DownloadManager.STATUS_RUNNING -> {
                    showProgressBar = true
                    context.getString(R.string.cancel)
                }

                DownloadManager.STATUS_SUCCESSFUL -> {
                    showProgressBar = false
                    context.getString(R.string.read_book_button)
                }

                else -> {
                    showProgressBar = false
                    context.getString(R.string.download_book_button)
                }
            }
        }

        // Check if this book is in downloadQueue.
        if (viewModel.bookDownloader.isBookCurrentlyDownloading(book.id)) {
            progressState =
                viewModel.bookDownloader.getRunningDownload(book.id)?.progress?.collectAsState()?.value!!
            LaunchedEffect(key1 = progressState, block = {
                updateBtnText(viewModel.bookDownloader.getRunningDownload(book.id)?.status)
            })
        }

        MiddleBar(
            bookLang = BookUtils.getLanguagesAsString(book.languages),
            pageCount = pageCount,
            downloadCount = Utils.prettyCount(book.downloadCount),
            progressValue = progressState,
            buttonText = buttonText,
            showProgressBar = showProgressBar
        ) {
            when (buttonText) {
                context.getString(R.string.read_book_button) -> {
                    /**
                     *  Library item could be null if we reload the screen
                     *  while some download was running, in that case we'll
                     *  de-attach from our old state where download function
                     *  will update library item and our new state will have
                     *  no library item, i.e. null.
                     */
                    if (state.bookLibraryItem == null) {
                        viewModel.reFetchLibraryItem(
                            bookId = book.id,
                            onComplete = { libraryItem ->
                                BookUtils.openBookFile(
                                    context = context,
                                    internalReader = viewModel.getInternalReaderSetting(),
                                    libraryItem = libraryItem,
                                    navController = navController
                                )
                            }
                        )
                    } else {
                        BookUtils.openBookFile(
                            context = context,
                            internalReader = viewModel.getInternalReaderSetting(),
                            libraryItem = state.bookLibraryItem,
                            navController = navController
                        )
                    }
                }

                context.getString(R.string.download_book_button) -> {
                    view.weakHapticFeedback()
                    viewModel.downloadBook(
                        book = book,
                        downloadProgressListener = { downloadProgress, downloadStatus ->
                            progressState = downloadProgress
                            updateBtnText(downloadStatus)
                        })
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(
                            message = context.getString(R.string.download_started),
                        )
                    }
                }

                context.getString(R.string.cancel) -> {
                    viewModel.bookDownloader.cancelDownload(
                        viewModel.bookDownloader.getRunningDownload(book.id)?.downloadId
                    )
                }
            }
        }

        Text(
            text = stringResource(id = R.string.book_synopsis),
            modifier = Modifier.padding(start = 13.dp, end = 8.dp),
            fontSize = 18.sp,
            fontFamily = poppinsFont,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        val synopsis = state.extraInfo.description.ifEmpty { null }
        if (synopsis != null) {
            Text(
                text = synopsis,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                fontFamily = poppinsFont,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
            )
        } else {
            NoSynopsisUI()
        }
    }
}

@Composable
private fun MiddleBar(
    bookLang: String,
    pageCount: String,
    downloadCount: String,
    progressValue: Float,
    buttonText: String,
    showProgressBar: Boolean,
    onButtonClick: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = progressValue,
        label = "download progress bar"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(visible = showProgressBar) {
            if (progressValue > 0f) {
                // Determinate progress bar.
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .padding(start = 14.dp, end = 14.dp, top = 6.dp)
                        .clip(RoundedCornerShape(40.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                )
            } else {
                // Indeterminate progress bar.
                LinearProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .padding(start = 14.dp, end = 14.dp, top = 6.dp)
                        .clip(RoundedCornerShape(40.dp))
                )
            }
        }

        Card(
            modifier = Modifier
                .height(90.dp)
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                    2.dp
                )
            )
        ) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Row {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_book_language),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp, end = 4.dp)
                        )
                        Text(
                            text = bookLang,
                            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp, start = 4.dp),
                            fontSize = 16.sp,
                            fontFamily = poppinsFont,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                }
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .width(2.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Row {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_book_pages),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 13.dp, bottom = 15.dp, end = 4.dp)
                        )
                        Text(
                            text = pageCount,
                            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp, start = 4.dp),
                            fontSize = 16.sp,
                            fontFamily = poppinsFont,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .width(2.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Row {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_book_downloads),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 15.dp, bottom = 13.dp, end = 4.dp)
                        )
                        Text(
                            text = downloadCount,
                            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp, start = 4.dp),
                            fontSize = 16.sp,
                            fontFamily = poppinsFont,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }

        Card(
            onClick = { onButtonClick() },
            modifier = Modifier
                .height(75.dp)
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = buttonText,
                    fontSize = 17.sp,
                    fontFamily = poppinsFont,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun BookDetailTopBar(
    onBackClicked: () -> Unit, onShareClicked: () -> Unit
) {
    val view = LocalView.current
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .padding(horizontal = 22.dp, vertical = 16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                .clickable {
                    view.weakHapticFeedback()
                    onBackClicked()
                }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(id = R.string.back_button_desc),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(14.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(id = R.string.book_detail_header),
            modifier = Modifier.padding(bottom = 2.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
            fontFamily = pacificoFont,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier
            .padding(22.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
            .clickable {
                view.weakHapticFeedback()
                onShareClicked()
            }) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = stringResource(id = R.string.back_button_desc),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
private fun NoSynopsisUI() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val compositionResult: LottieCompositionResult =
            rememberLottieComposition(
                spec = LottieCompositionSpec.RawRes(R.raw.synopis_not_found_lottie)
            )
        val progressAnimation by animateLottieCompositionAsState(
            compositionResult.value,
            isPlaying = true,
            iterations = LottieConstants.IterateForever,
            speed = 1f
        )

        Spacer(modifier = Modifier.weight(2f))
        LottieAnimation(
            composition = compositionResult.value,
            progress = { progressAnimation },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(200.dp),
            enableMergePaths = true
        )

        Text(
            text = stringResource(id = R.string.book_synopsis_not_found),
            modifier = Modifier.padding(14.dp),
            fontFamily = poppinsFont,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}


@Composable
@Preview(showBackground = true)
fun BookDetailScreenPreview() {
    BookDetailScreen(
        bookId = "0",
        navController = rememberNavController(),
    )
}
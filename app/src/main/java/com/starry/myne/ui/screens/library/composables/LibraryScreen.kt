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

package com.starry.myne.ui.screens.library.composables

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.starry.myne.BuildConfig
import com.starry.myne.MainActivity
import com.starry.myne.R
import com.starry.myne.database.library.LibraryItem
import com.starry.myne.helpers.Utils
import com.starry.myne.helpers.getActivity
import com.starry.myne.helpers.isScrollingUp
import com.starry.myne.ui.common.CustomTopAppBar
import com.starry.myne.ui.navigation.Screens
import com.starry.myne.ui.screens.library.viewmodels.ImportStatus
import com.starry.myne.ui.screens.library.viewmodels.LibraryViewModel
import com.starry.myne.ui.screens.settings.viewmodels.SettingsViewModel
import com.starry.myne.ui.screens.settings.viewmodels.ThemeMode
import com.starry.myne.ui.theme.figeronaFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import java.io.File
import java.io.FileInputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController) {
    val viewModel: LibraryViewModel = hiltViewModel()
    val state = viewModel.state

    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()

    val importBookLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                (context as MainActivity).contentResolver.openInputStream(uri)?.let { ips ->
                    viewModel.importBook(context, ips as FileInputStream)
                }
            }
        }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 70.dp),
        topBar = {
            CustomTopAppBar(
                headerText = stringResource(id = R.string.library_header),
                iconRes = R.drawable.ic_nav_library
            )
        },
        floatingActionButton = {
            val density = LocalDensity.current
            AnimatedVisibility(
                visible = !state.showImportUI && lazyListState.isScrollingUp(),
                enter = slideInVertically {
                    with(density) { 40.dp.roundToPx() }
                } + fadeIn(),
                exit = fadeOut(
                    animationSpec = keyframes {
                        this.durationMillis = 120
                    }
                )
            ) {
                ExtendedFloatingActionButton(onClick = {
                    importBookLauncher.launch(arrayOf("application/epub+zip"))
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(id = R.string.import_button_desc)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.import_button_text),
                        fontWeight = FontWeight.Medium,
                        fontFamily = figeronaFont,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                }
            }
        }
    ) { paddingValues ->
        LibraryContents(
            viewModel = viewModel,
            lazyListState = lazyListState,
            snackBarHostState = snackBarHostState,
            navController = navController,
            paddingValues = paddingValues
        )

        if (state.showImportUI) {
            BasicAlertDialog(onDismissRequest = {}) {
                ImportingDialog(importStatus = viewModel.state.importStatus)
            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryContents(
    viewModel: LibraryViewModel,
    lazyListState: LazyListState,
    snackBarHostState: SnackbarHostState,
    navController: NavController,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val settingsVm = (context.getActivity() as MainActivity).settingsViewModel
    val libraryItems = viewModel.allItems.observeAsState(listOf()).value

    // Show tooltip for library screen.
    LaunchedEffect(key1 = true) {
        if (viewModel.shouldShowLibraryTooltip()) {
            val result = snackBarHostState.showSnackbar(
                message = context.getString(R.string.library_tooltip),
                actionLabel = context.getString(R.string.got_it),
                duration = SnackbarDuration.Indefinite
            )

            when (result) {
                SnackbarResult.ActionPerformed -> {
                    viewModel.libraryTooltipDismissed()
                }

                SnackbarResult.Dismissed -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        if (libraryItems.isEmpty()) {
            NoLibraryItemAnimation()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                state = lazyListState
            ) {
                items(
                    count = libraryItems.size,
                    key = { i -> libraryItems[i].id }
                ) { i ->
                    val item = libraryItems[i]
                    if (item.fileExist()) {
                        LibraryLazyItem(
                            modifier = Modifier.animateItemPlacement(),
                            item = item,
                            snackBarHostState = snackBarHostState,
                            navController = navController,
                            viewModel = viewModel,
                            settingsVm = settingsVm
                        )
                    } else {
                        viewModel.deleteItemFromDB(item)
                    }
                }
            }

        }
    }
}

@Composable
private fun LibraryLazyItem(
    modifier: Modifier,
    item: LibraryItem,
    snackBarHostState: SnackbarHostState,
    navController: NavController,
    viewModel: LibraryViewModel,
    settingsVm: SettingsViewModel
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val openDeleteDialog = remember { mutableStateOf(false) }

    // Swipe actions to show book details.
    val detailsAction = SwipeAction(icon = painterResource(
        id = if (settingsVm.getCurrentTheme() == ThemeMode.Dark) R.drawable.ic_info else R.drawable.ic_info_white
    ), background = MaterialTheme.colorScheme.primary, onSwipe = {
        viewModel.viewModelScope.launch {
            delay(250L)
            if (item.isExternalBook) {
                snackBarHostState.showSnackbar(
                    message = context.getString(R.string.external_book_info_unavailable),
                    actionLabel = context.getString(R.string.ok),
                    duration = SnackbarDuration.Short
                )
            } else {
                navController.navigate(
                    Screens.BookDetailScreen.withBookId(
                        item.bookId.toString()
                    )
                )
            }
        }
    })

    // Swipe actions to share book.
    val shareAction = SwipeAction(icon = painterResource(
        id = if (settingsVm.getCurrentTheme() == ThemeMode.Dark) R.drawable.ic_share else R.drawable.ic_share_white
    ), background = MaterialTheme.colorScheme.primary, onSwipe = {
        val uri = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            File(item.filePath)
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = context.contentResolver.getType(uri)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(
            Intent.createChooser(
                intent,
                context.getString(R.string.share_app_chooser)
            )
        )
    })

    SwipeableActionsBox(
        modifier = modifier.padding(vertical = 4.dp),
        startActions = listOf(shareAction),
        endActions = listOf(detailsAction),
        swipeThreshold = 85.dp
    ) {
        LibraryCard(title = item.title,
            author = item.authors,
            item.getFileSize(),
            item.getDownloadDate(),
            isExternalBook = item.isExternalBook,
            onReadClick = {
                Utils.openBookFile(
                    context = context,
                    internalReader = viewModel.getInternalReaderSetting(),
                    libraryItem = item,
                    navController = navController
                )
            },
            onDeleteClick = { openDeleteDialog.value = true })
    }

    if (openDeleteDialog.value) {
        AlertDialog(onDismissRequest = {
            openDeleteDialog.value = false
        }, title = {
            Text(
                text = stringResource(id = R.string.library_delete_dialog_title),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }, confirmButton = {
            FilledTonalButton(
                onClick = {
                    openDeleteDialog.value = false
                    val fileDeleted = item.deleteFile()
                    if (fileDeleted) {
                        viewModel.deleteItemFromDB(item)
                    } else {
                        coroutineScope.launch {
                            snackBarHostState.showSnackbar(
                                message = context.getString(R.string.error),
                                actionLabel = context.getString(R.string.ok),
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(stringResource(id = R.string.confirm))
            }
        }, dismissButton = {
            TextButton(onClick = {
                openDeleteDialog.value = false
            }) {
                Text(stringResource(id = R.string.cancel))
            }
        })
    }
}

@Composable
private fun LibraryCard(
    title: String,
    author: String,
    fileSize: String,
    date: String,
    isExternalBook: Boolean,
    onReadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                3.dp
            )
        ), shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(90.dp)
                    .width(90.dp)
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        id = if (isExternalBook) R.drawable.ic_library_external_item
                        else R.drawable.ic_library_item
                    ),
                    contentDescription = stringResource(id = R.string.back_button_desc),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = title,
                    fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                    fontSize = 20.sp,
                    fontFamily = figeronaFont,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = author,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    fontStyle = MaterialTheme.typography.bodySmall.fontStyle,
                    fontFamily = figeronaFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row {
                    Text(
                        text = fileSize,
                        fontFamily = figeronaFont,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    VerticalDivider(
                        modifier = Modifier
                            .height(17.5.dp)
                            .width(1.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = date,
                        fontFamily = figeronaFont,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row {
                    LibraryCardButton(text = stringResource(id = R.string.library_read_button),
                        icon = ImageVector.vectorResource(id = R.drawable.ic_library_read),
                        onClick = { onReadClick() })

                    Spacer(modifier = Modifier.width(10.dp))

                    LibraryCardButton(text = stringResource(id = R.string.library_delete_button),
                        icon = Icons.Outlined.Delete,
                        onClick = { onDeleteClick() })
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun LibraryCardButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(8.dp)
        )
        .clickable { onClick() }) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size = 14.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                fontFamily = figeronaFont,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 2.dp),
            )
        }
    }
}

@Composable
private fun NoLibraryItemAnimation() {
    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val compositionResult: LottieCompositionResult = rememberLottieComposition(
            spec = LottieCompositionSpec.RawRes(R.raw.no_library_items_lottie)
        )
        val progressAnimation by animateLottieCompositionAsState(
            compositionResult.value,
            isPlaying = true,
            iterations = LottieConstants.IterateForever,
            speed = 1f
        )

        Spacer(modifier = Modifier.weight(1f))
        LottieAnimation(
            composition = compositionResult.value,
            progress = { progressAnimation },
            modifier = Modifier.size(300.dp),
            enableMergePaths = true
        )

        Text(
            text = stringResource(id = R.string.empty_library),
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp)
        )
        Spacer(modifier = Modifier.weight(1.4f))
    }
}

@Composable
private fun ImportingDialog(importStatus: ImportStatus) {
    val composition = rememberLottieComposition(
        spec = when (importStatus) {
            ImportStatus.IMPORTING -> LottieCompositionSpec.RawRes(R.raw.epub_importing_lottie)
            ImportStatus.SUCCESS -> LottieCompositionSpec.RawRes(R.raw.epub_import_success_lottie)
            ImportStatus.ERROR -> LottieCompositionSpec.RawRes(R.raw.epub_import_error_lottie)
            ImportStatus.IDLE -> LottieCompositionSpec.RawRes(R.raw.epub_importing_lottie)
        }
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                composition = composition.value,
                modifier = Modifier.size(200.dp),
                enableMergePaths = true,
                isPlaying = true,
                iterations = if (importStatus == ImportStatus.IMPORTING)
                    LottieConstants.IterateForever else 1,
            )

            val text = when (importStatus) {
                ImportStatus.IMPORTING -> stringResource(id = R.string.epub_importing)
                ImportStatus.SUCCESS -> stringResource(id = R.string.epub_imported)
                ImportStatus.ERROR -> stringResource(id = R.string.epub_import_error)
                ImportStatus.IDLE -> ""
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .offset(y = (-28).dp),
                fontFamily = figeronaFont
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
@Preview
fun LibraryScreenPreview() {
    LibraryCard(title = "The Idiot",
        author = "Fyodor Dostoevsky",
        fileSize = "5.9MB",
        date = "01- Jan -2020",
        isExternalBook = false,
        onReadClick = {},
        onDeleteClick = {})
}

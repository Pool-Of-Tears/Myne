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


package com.starry.myne.ui.screens.reader.detail

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.starry.myne.MainActivity
import com.starry.myne.R
import com.starry.myne.database.progress.ProgressData
import com.starry.myne.helpers.NetworkObserver
import com.starry.myne.helpers.getActivity
import com.starry.myne.helpers.toToast
import com.starry.myne.helpers.weakHapticFeedback
import com.starry.myne.ui.common.BookDetailTopUI
import com.starry.myne.ui.common.CustomTopAppBar
import com.starry.myne.ui.common.ProgressDots
import com.starry.myne.ui.common.simpleVerticalScrollbar
import com.starry.myne.ui.screens.reader.main.activities.ReaderActivity
import com.starry.myne.ui.screens.reader.main.activities.ReaderConstants
import com.starry.myne.ui.screens.settings.viewmodels.SettingsViewModel
import com.starry.myne.ui.theme.poppinsFont

@Composable
fun ReaderDetailScreen(
    libraryItemId: String,
    navController: NavController,
    networkStatus: NetworkObserver.Status,
    viewModel: ReaderDetailViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val readerData by (viewModel.progressData?.collectAsStateWithLifecycle(initialValue = null)
        ?: remember { mutableStateOf(null) })

    LaunchedEffect(key1 = true) {
        viewModel.loadEbookData(libraryItemId, networkStatus)
    }

    ReaderDetailScreen(
        libraryItemId = libraryItemId,
        navController = navController,
        state = state,
        progressData = readerData,
    )
}

@Composable
fun ReaderDetailScreen(
    libraryItemId: String,
    navController: NavController,
    state: ReaderDetailScreenState,
    progressData: ProgressData?
) {
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    /*
    * Cases
    * zen mode | notification policy access permission | action
    * true     | true                                  | 1) Set up lifecycle events observer for start and stop.
    * true     | true                                  | 2) On toggling of dnd through quick setting panel, update original dnd filter
    * true     | false                                 | 3) Disable Zen mode
    * */

    val notificationManager : NotificationManager = remember {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    val viewModel = (context.getActivity() as MainActivity).settingsViewModel

    val enableZenMode by viewModel.enableZenMode.observeAsState(false)

    if (enableZenMode && notificationManager.isNotificationPolicyAccessGranted) {
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (notificationManager.isNotificationPolicyAccessGranted){
                    when (event) {
                        Lifecycle.Event.ON_START -> {
                            viewModel.originalFilter = notificationManager.currentInterruptionFilter
                            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                            viewModel.isChangedManually=true
                            Log.d("TRACKER","Start hit")
                        }
                        Lifecycle.Event.ON_STOP -> {
                            notificationManager.setInterruptionFilter(viewModel.originalFilter)
                            viewModel.isChangedManually=true
                            Log.d("TRACKER","Stop hit")
                        }
                        else -> {}
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                viewModel.isChangedManually=false
            }
        }
        DisposableEffect(lifecycleOwner) {
            val filterReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (viewModel.isChangedManually){
                        viewModel.isChangedManually=false
                    }else{
                        if (notificationManager.isNotificationPolicyAccessGranted) {
                            viewModel.originalFilter=notificationManager.currentInterruptionFilter
                        }
                    }
                    Log.d("TRACKER","Receiver hit")
                }
            }
            ContextCompat.registerReceiver(
                context,
                filterReceiver,
                IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            onDispose {
                context.unregisterReceiver(filterReceiver)
            }
        }
    }

    if (enableZenMode && !notificationManager.isNotificationPolicyAccessGranted){
        viewModel.setEnableZenMode(false)
    }

    Crossfade(targetState = state.isLoading, label = "ReaderDetailLoadingCrossFade") { isLoading ->
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
                stringResource(id = R.string.error).toToast(context)
                navController.navigateUp()
            } else {
                ReaderDetailScaffold(
                    libraryItemId = libraryItemId,
                    progressData = progressData,
                    state = state,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun ReaderDetailScaffold(
    libraryItemId: String,
    progressData: ProgressData?,
    state: ReaderDetailScreenState,
    navController: NavController
) {
    val context = LocalContext.current
    val settingsVM = (context.getActivity() as MainActivity).settingsViewModel

    Scaffold(topBar = {
        CustomTopAppBar(headerText = stringResource(id = R.string.reader_detail_header)) {
            navController.navigateUp()
        }
    }, floatingActionButton = {
        ExtendedFloatingActionButton(
            text = { Text(text = stringResource(id = if (progressData != null) R.string.continue_reading_button else R.string.start_reading_button)) },
            onClick = {
                val intent = Intent(context, ReaderActivity::class.java)
                intent.putExtra(
                    ReaderConstants.EXTRA_LIBRARY_ITEM_ID, libraryItemId.toInt()
                )
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
            BookDetailTopUI(
                title = state.title,
                authors = state.authors,
                imageData = state.coverImage,
                currentThemeMode = settingsVM.getCurrentTheme(),
                showReaderBackground = true,
                progressPercent = progressData?.getProgressPercent(state.chapters.size) ?: ""
            )

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
                items(state.chapters.size) { idx ->
                    val chapter = state.chapters[idx]
                    ChapterItem(
                        chapterTitle = chapter.title,
                        isRead = idx < (progressData?.lastChapterIndex ?: 0),
                        onClick = {
                            val intent = Intent(context, ReaderActivity::class.java)
                            intent.putExtra(
                                ReaderConstants.EXTRA_LIBRARY_ITEM_ID, libraryItemId.toInt()
                            )
                            intent.putExtra(ReaderConstants.EXTRA_CHAPTER_IDX, idx)
                            context.startActivity(intent)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterItem(
    chapterTitle: String,
    isRead: Boolean,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    Card(
        onClick = {
            view.weakHapticFeedback()
            onClick()
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)
        ) {
            Text(
                modifier = Modifier.weight(3f),
                text = chapterTitle,
                fontFamily = poppinsFont,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isRead) {
                    Icon(
                        modifier = Modifier.size(15.dp),
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Icon(
                    modifier = Modifier.size(15.dp),
                    painter = painterResource(id = R.drawable.ic_right_arrow),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

}



@Preview(showBackground = true)
@Composable
fun EpubDetailScreenPV() {
    ReaderDetailScreen(
        "",
        rememberNavController(),
        state = ReaderDetailScreenState(),
        progressData = ProgressData(
            libraryItemId = 0,
            lastChapterIndex = 0,
            lastChapterOffset = 0,
            lastReadTime = 0,
        ),
    )
    //ReaderError(rememberNavController())
}
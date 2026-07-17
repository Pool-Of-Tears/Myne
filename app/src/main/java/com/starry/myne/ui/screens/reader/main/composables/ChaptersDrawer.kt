package com.starry.myne.ui.screens.reader.main.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starry.myne.R
import com.starry.myne.database.reader.ReaderBookmark
import com.starry.myne.epub.models.EpubChapter
import com.starry.myne.ui.theme.poppinsFont
import kotlinx.coroutines.launch

@Composable
fun ChaptersDrawer(
    drawerState: DrawerState,
    chapters: List<EpubChapter>,
    bookmarks: List<ReaderBookmark>,
    currentChapterIndex: Int,
    onScrollToChapter: (Int) -> Unit,
    onScrollToBookmark: (ReaderBookmark) -> Unit,
    onDeleteBookmark: (ReaderBookmark) -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    ModalNavigationDrawer(
        modifier = Modifier.systemBarsPadding(),
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                text = stringResource(id = R.string.reader_chapter_list_title),
                                fontFamily = poppinsFont,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                text = stringResource(id = R.string.reader_bookmarks_list_title),
                                fontFamily = poppinsFont,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )

                if (selectedTabIndex == 0) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(chapters.size) { idx ->
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = chapters[idx].title,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontFamily = poppinsFont
                                    )
                                },
                                icon = {
                                    if (idx < currentChapterIndex) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircleOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                selected = idx == currentChapterIndex,
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.close()
                                        onScrollToChapter(idx)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(bookmarks.size) { idx ->
                            val bookmark = bookmarks[idx]
                            NavigationDrawerItem(
                                label = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = bookmark.chapterTitle,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            fontFamily = poppinsFont
                                        )
                                        IconButton(onClick = { onDeleteBookmark(bookmark) }) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                },
                                selected = false,
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.close()
                                        onScrollToBookmark(bookmark)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }, content = content
    )
}

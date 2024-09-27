package com.starry.myne.ui.screens.reader.main.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starry.myne.R
import com.starry.myne.epub.models.EpubChapter
import com.starry.myne.ui.theme.poppinsFont
import kotlinx.coroutines.launch

@Composable
fun ChaptersDrawer(
    drawerState: DrawerState,
    chapters: List<EpubChapter>,
    currentChapterIndex: Int,
    onScrollToChapter: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        modifier = Modifier.systemBarsPadding(),
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
                    fontFamily = poppinsFont,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
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
            }
        }, content = content
    )
}
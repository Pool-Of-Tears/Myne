package com.starry.myne.ui.screens.reader.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.starry.myne.ui.screens.reader.viewmodels.ReaderViewModel
import com.starry.myne.ui.theme.PTSeriFont

@Composable
fun ReaderScreen(bookId: String) {
    val viewModel: ReaderViewModel = hiltViewModel()
    viewModel.loadEpubBook(bookId)
    val state = viewModel.state

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

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
                items(state.epubBook!!.chapters.size) { idx ->
                    val chapter = state.epubBook.chapters[idx]
                    PageItem(text = chapter.body, textSize = 20.sp)
                }
            }

            LaunchedEffect(key1 = true, block = {
                // lazyListState.scrollToItem(4, 0)
            })
            // println("READER: ${lazyListState.firstVisibleItemIndex} ")
        }

    }
}


@Composable
fun PageItem(text: String, textSize: TextUnit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            fontSize = textSize,
            fontFamily = PTSeriFont,
            modifier = Modifier.padding(start = 14.dp, end = 14.dp),
        )
    }


}
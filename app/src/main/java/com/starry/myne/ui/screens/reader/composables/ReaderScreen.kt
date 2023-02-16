package com.starry.myne.ui.screens.reader.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.starry.myne.R
import com.starry.myne.ui.screens.reader.viewmodels.ReaderViewModel
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
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )

    BottomSheetScaffold(scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                //TODO
                Text("MEOW")
            }
        },
        sheetShape = RoundedCornerShape(topEnd = 30.dp),
        sheetPeekHeight = 0.dp,
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
                        items(state.epubBook!!.chapters.size) { idx ->
                            val chapter = state.epubBook.chapters[idx]
                            LazyChapterItem(title = chapter.title,
                                body = chapter.body,
                                textSize = 16.sp,
                                onClick = {
                                    coroutineScope.launch {
                                        bottomSheetScaffoldState.bottomSheetState.expand()
                                    }
                                })
                        }
                    }

                    if (chapterIndex != -1) {
                        LaunchedEffect(key1 = true, block = {
                            lazyListState.scrollToItem(chapterIndex, 0)
                        })
                    }
                }
            }
        })
}


@Composable
fun LazyChapterItem(title: String, body: String, textSize: TextUnit, onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }) {

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
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = body,
            fontSize = textSize,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.padding(start = 14.dp, end = 14.dp),
        )

        Divider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
            thickness = 2.dp,
        )
    }
}
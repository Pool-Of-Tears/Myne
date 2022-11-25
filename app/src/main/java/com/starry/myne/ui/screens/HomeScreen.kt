@file:OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)

package com.starry.myne.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.starry.myne.R
import com.starry.myne.common.compose.ProgressDots
import com.starry.myne.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen() {
    val viewModel = viewModel<HomeViewModel>()
    val state = viewModel.state

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(state.items.size) { i ->
            val item = state.items[i]
            if (i >= state.items.size - 1 && !state.endReached && !state.isLoading) {
                viewModel.loadNextItems()
            }
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                BookItemCard(
                    title = item.title,
                    author = "Fyodor Dostoyevsky", //item.authors.first().name,
                    language = "English", // item.languages.first(),
                    subjects = "Crime, Drama, Psychological Thriller", // item.subjects.first(),
                    downloadCount = item.downloadCount,
                    coverImageUrl = item.formats.imagejpeg
                )
            }

        }
        item {
            if (state.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ProgressDots()
                }
            }
        }
    }

}


@Composable
fun BookItemCard(
    title: String,
    author: String,
    language: String,
    subjects: String,
    downloadCount: Long,
    coverImageUrl: String
) {
    Card(
        modifier = Modifier
            .height(215.dp)
            .width(375.dp),
        onClick = { /*TODO*/ },
        colors = CardDefaults.elevatedCardColors(),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        shape = RoundedCornerShape(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.weight(1.8f)
            ) {
                val painter = rememberImagePainter(data = coverImageUrl, builder = {
                    placeholder(R.drawable.placeholder_cat)
                    error(R.drawable.placeholder_cat)
                    crossfade(800)
                })
                Image(
                    painter = painter,
                    contentDescription = stringResource(id = R.string.cover_image_desc),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .padding(
                            start = 12.dp, top = 6.dp, bottom = 6.dp, end = 8.dp
                        )
                        .fillMaxWidth(),
                    fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                    fontSize = 24.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = author,
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = language,
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )

                Text(
                    text = subjects,
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontStyle = FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_bookcard_downloads),
                        contentDescription = stringResource(id = R.string.download_count_desc),
                        tint = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = downloadCount.toString(),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun HomeScreenPreview() {
    HomeScreen()
}
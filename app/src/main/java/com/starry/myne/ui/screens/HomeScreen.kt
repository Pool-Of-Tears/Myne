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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.starry.myne.R
import com.starry.myne.ui.common.ProgressDots
import com.starry.myne.ui.viewmodels.HomeViewModel
import com.starry.myne.utils.Utils

@Composable
fun HomeScreen() {
    val viewModel = viewModel<HomeViewModel>()
    val state = viewModel.state

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 6.dp)
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
                    author = Utils.getAuthorsAsString(item.authors),
                    language = Utils.getLanguagesAsString(item.languages),
                    subjects = Utils.getSubjectsAsString(item.subjects, 3),
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
    coverImageUrl: String
) {
    Card(
        modifier = Modifier
            .width(380.dp)
            .height(200.dp),
        onClick = { /*TODO*/ },
        colors = CardDefaults.elevatedCardColors(),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        shape = RoundedCornerShape(6.dp)
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
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
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
                            start = 12.dp, top = 8.dp, end = 8.dp
                        )
                        .fillMaxWidth(),
                    fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                    fontSize = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = author,
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    fontStyle = MaterialTheme.typography.bodySmall.fontStyle
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = language,
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                )

                Text(
                    text = subjects,
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp, bottom = 2.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    fontStyle = MaterialTheme.typography.bodySmall.fontStyle
                    // fontStyle = FontStyle.Italic
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun HomeScreenPreview() {
    BookItemCard(
        title = "Crime and Punishment",
        author = "Dostoyevsky, Fyodor",
        language = "English, Russian",
        subjects = "Crime, Psychological aspects, Fiction",
        coverImageUrl = "https://www.gutenberg.org/cache/epub/2554/pg2554.cover.medium.jpg"
    )
}
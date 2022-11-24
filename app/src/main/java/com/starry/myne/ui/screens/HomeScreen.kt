@file:OptIn(
    ExperimentalCoilApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class
)

package com.starry.myne.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.starry.myne.R

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        BookItemCard(
            "The Idiot",
            "Fyodor Dostoyevsky",
            "https://www.gutenberg.org/cache/epub/2638/pg2638.cover.medium.jpg"
        )

    }
}


@Composable
fun BookItemCard(title: String, author: String, coverImageUrl: String) {
    Card(
        modifier = Modifier
            .height(215.dp)
            .width(375.dp),
        onClick = { /*TODO*/ },
        colors = CardDefaults.elevatedCardColors(),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.weight(1.8f)
            ) {
                val painter = rememberImagePainter(data = coverImageUrl, builder = {
                    placeholder(R.drawable.placeholder_cat)
                    crossfade(800)
                })
                Image(
                    painter = painter,
                    contentDescription = stringResource(id = R.string.cover_image_desc),
                    modifier = Modifier.fillMaxSize()
                )
                if (painter.state is ImagePainter.State.Loading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }
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
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun HomeScreenPreview() {
    HomeScreen()
}
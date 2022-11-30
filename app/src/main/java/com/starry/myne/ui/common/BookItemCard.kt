package com.starry.myne.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.starry.myne.R
import com.starry.myne.ui.theme.comfortFont

@ExperimentalCoilApi
@ExperimentalMaterial3Api
@Composable
fun BookItemCard(
    title: String,
    author: String,
    language: String,
    subjects: String,
    coverImageUrl: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                2.dp
            )
        ),
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
                    fontFamily = comfortFont,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = author,
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    fontStyle = MaterialTheme.typography.bodySmall.fontStyle,
                    fontFamily = comfortFont
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = language,
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
                    fontFamily = comfortFont
                )

                Text(
                    text = subjects,
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp, bottom = 2.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    fontStyle = MaterialTheme.typography.bodySmall.fontStyle,
                    fontFamily = comfortFont
                    // fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

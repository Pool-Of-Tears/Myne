package com.starry.myne.ui.screens

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.starry.myne.R
import com.starry.myne.ui.common.ProgressDots
import com.starry.myne.ui.theme.comfortFont
import com.starry.myne.ui.theme.pacificoFont
import com.starry.myne.ui.viewmodels.BookDetailViewModel
import com.starry.myne.utils.BookUtils
import com.starry.myne.utils.Utils


@ExperimentalMaterial3Api
@ExperimentalCoilApi
@Composable
fun BookDetailScreen(bookId: String, navController: NavController) {
    val viewModel = viewModel<BookDetailViewModel>()
    viewModel.getBookDetails(bookId)
    val state = viewModel.state
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            onBackClicked = {
                navController.navigateUp()
            }, onShareClicked = {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, "https://www.gutenberg.org/ebooks/$bookId")
                val chooser = Intent.createChooser(
                    intent,
                    context.getString(R.string.share_intent_header)
                )
                context.startActivity(chooser)
            })

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 65.dp),
                contentAlignment = Alignment.Center
            ) {
                ProgressDots()
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.book_details_bg),
                        contentDescription = "",
                        alpha = 0.2f,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.background,
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background
                                    ), startY = 15f
                                )
                            )
                    )

                    Row(modifier = Modifier.fillMaxSize()) {
                        val imageUrl = state.extraInfo.coverImage.ifEmpty {
                            state.item.books.first().formats.imagejpeg
                        }
                        val painter =
                            rememberImagePainter(
                                data = imageUrl,
                                builder = {
                                    placeholder(R.drawable.placeholder_cat)
                                    error(R.drawable.placeholder_cat)
                                    crossfade(500)
                                })

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val imageBackground = if (isSystemInDarkTheme()) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                            }
                            Box(
                                modifier = Modifier
                                    .shadow(24.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(imageBackground)
                            ) {
                                Image(
                                    painter = painter, contentDescription = "", modifier = Modifier
                                        .width(118.dp)
                                        .height(169.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.item.books.first().title,
                                modifier = Modifier
                                    .padding(
                                        start = 12.dp, end = 8.dp, top = 20.dp
                                    )
                                    .fillMaxWidth(),
                                fontSize = 24.sp,
                                fontFamily = comfortFont,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground,
                            )

                            Text(
                                text = BookUtils.getAuthorsAsString(state.item.books.first().authors),
                                modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                                fontSize = 18.sp,
                                fontFamily = comfortFont,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground,
                            )

                            Text(
                                text = stringResource(id = R.string.book_download_count).format(
                                    Utils.prettyCount(state.item.books.first().downloadCount)
                                ),
                                modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 8.dp),
                                fontSize = 14.sp,
                                fontFamily = comfortFont,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground,
                            )

                            Spacer(modifier = Modifier.height(50.dp))
                        }
                    }
                }

                val pageCount = if (state.extraInfo.pageCount > 0) {
                    state.extraInfo.pageCount.toString()
                } else {
                    stringResource(id = R.string.not_applicable)
                }
                MiddleBar(
                    bookLang = BookUtils.getLanguagesAsString(state.item.books.first().languages),
                    pageCount = pageCount
                ) {
                    //TODO: Handle download button click.
                }

                Text(
                    text = stringResource(id = R.string.book_synopsis),
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                    fontSize = 20.sp,
                    fontFamily = comfortFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                val synopsis = state.extraInfo.description.ifEmpty {
                    stringResource(id = R.string.book_synopsis_not_found)
                }

                Text(
                    text = synopsis,
                    modifier = Modifier.padding(14.dp),
                    fontFamily = comfortFont,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun MiddleBar(bookLang: String, pageCount: String, onDownloadButtonClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .height(90.dp)
                .weight(3f)
                .padding(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                    2.dp
                )
            )
        ) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = bookLang,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 18.sp,
                        fontFamily = comfortFont,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                }
                Divider(
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .width(2.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.book_page_count).format(pageCount),
                        modifier = Modifier.padding(14.dp),
                        fontSize = 18.sp,
                        fontFamily = comfortFont,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }

        Card(
            onClick = { onDownloadButtonClick() },
            modifier = Modifier
                .height(90.dp)
                .weight(1f)
                .padding(top = 12.dp, bottom = 12.dp, end = 12.dp, start = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ), shape = CircleShape
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_download),
                    contentDescription = stringResource(id = R.string.download_button_desc),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun TopAppBar(
    onBackClicked: () -> Unit,
    onShareClicked: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .padding(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                .clickable { onBackClicked() }
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = stringResource(id = R.string.back_button_desc),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(14.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(id = R.string.book_detail_header),
            modifier = Modifier.padding(bottom = 5.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
            fontFamily = pacificoFont,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .padding(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                .clickable { onShareClicked() }
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = stringResource(id = R.string.back_button_desc),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@ExperimentalMaterial3Api
@ExperimentalCoilApi
@Composable
@Preview
fun BookDetailScreenPreview() {
    BookDetailScreen(bookId = "0", navController = rememberNavController())
}
/*
Copyright 2022 - 2023 Stɑrry Shivɑm

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.starry.myne.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.starry.myne.BuildConfig
import com.starry.myne.R
import com.starry.myne.ui.common.CustomTopAppBar
import com.starry.myne.ui.theme.figeronaFont
import com.starry.myne.ui.viewmodels.LibraryViewModel
import com.starry.myne.utils.toToast
import java.io.File


@ExperimentalMaterial3Api
@Composable
fun LibraryScreen() {
    val viewModel: LibraryViewModel = hiltViewModel()
    val state = viewModel.allItems.observeAsState(listOf()).value
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
        ) {
            CustomTopAppBar(
                headerText = stringResource(id = R.string.library_header),
                icon = R.drawable.ic_nav_library
            )
            Divider(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                thickness = 2.dp,
            )
        }

        if (state.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_empty_library),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(85.dp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(id = R.string.empty_library),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = figeronaFont,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                items(state.size) { i ->
                    val item = state[i]
                    if (item.fileExist()) {
                        LibraryCard(
                            title = item.title,
                            author = item.authors,
                            item.getFileSize(),
                            item.getDownloadDate()
                        ) {
                            val uri = FileProvider.getUriForFile(
                                context,
                                BuildConfig.APPLICATION_ID + ".provider",
                                File(item.filePath)
                            )
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            intent.setDataAndType(uri, context.contentResolver.getType(uri))
                            val chooser = Intent.createChooser(
                                intent,
                                context.getString(R.string.app_chooser)
                            )
                            try {
                                context.startActivity(chooser)
                            } catch (exc: ActivityNotFoundException) {
                                context.getString(R.string.no_app_to_handle_epub).toToast(context)
                            }
                        }
                    } else {
                        viewModel.deleteItem(item)
                    }
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun LibraryCard(
    title: String,
    author: String,
    fileSize: String,
    date: String,
    onClick: () -> Unit
) {
    Card(
        onClick = { onClick() },
        modifier = Modifier
            .height(125.dp)
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                1.dp
            )
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(85.dp)
                    .width(85.dp)
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_library_item),
                    contentDescription = stringResource(id = R.string.back_button_desc),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = title,
                    fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                    fontSize = 20.sp,
                    fontFamily = figeronaFont,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = author,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    fontStyle = MaterialTheme.typography.bodySmall.fontStyle,
                    fontFamily = figeronaFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row {
                    Text(
                        text = fileSize,
                        fontFamily = figeronaFont,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Divider(
                        modifier = Modifier
                            .height(17.5.dp)
                            .width(1.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = date,
                        fontFamily = figeronaFont,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
@Preview
fun ProfileScreenPreview() {
    LibraryCard(
        title = "The Idiot",
        author = "Fyodor Dostoevsky",
        fileSize = "5.9MB",
        date = "01- Jan -2020",
        onClick = {}
    )
}

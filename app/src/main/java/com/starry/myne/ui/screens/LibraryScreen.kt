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

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.starry.myne.BuildConfig
import com.starry.myne.MainActivity
import com.starry.myne.R
import com.starry.myne.navigation.Screens
import com.starry.myne.ui.common.CustomTopAppBar
import com.starry.myne.ui.theme.figeronaFont
import com.starry.myne.ui.viewmodels.LibraryViewModel
import com.starry.myne.ui.viewmodels.ReaderViewModel
import com.starry.myne.ui.viewmodels.ThemeMode
import com.starry.myne.utils.Utils
import com.starry.myne.utils.getActivity
import com.starry.myne.utils.toToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import java.io.File


@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
fun LibraryScreen(navController: NavController) {
    val viewModel: LibraryViewModel = hiltViewModel()
    val state = viewModel.allItems.observeAsState(listOf()).value
    val context = LocalContext.current
    val settingsViewModel = (context.getActivity() as MainActivity).settingsViewModel

    //
    val readerVM: ReaderViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 70.dp)
    ) {
        CustomTopAppBar(
            headerText = stringResource(id = R.string.library_header),
            icon = R.drawable.ic_nav_library
        )

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

                        val openDeleteDialog = remember { mutableStateOf(false) }

                        val detailsAction = SwipeAction(icon = painterResource(
                            id = if (settingsViewModel.getCurrentTheme() == ThemeMode.Dark) R.drawable.ic_info else R.drawable.ic_info_white
                        ), background = MaterialTheme.colorScheme.primary, onSwipe = {
                            viewModel.viewModelScope.launch {
                                delay(250L)
                                navController.navigate(Screens.BookDetailScreen.withBookId(item.bookId.toString()))
                            }
                        })

                        val shareAction = SwipeAction(icon = painterResource(
                            id = if (settingsViewModel.getCurrentTheme() == ThemeMode.Dark) R.drawable.ic_share else R.drawable.ic_share_white
                        ), background = MaterialTheme.colorScheme.primary, onSwipe = {
                            val uri = FileProvider.getUriForFile(
                                context,
                                BuildConfig.APPLICATION_ID + ".provider",
                                File(item.filePath)
                            )
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            intent.type = context.contentResolver.getType(uri)
                            intent.putExtra(Intent.EXTRA_STREAM, uri)
                            context.startActivity(
                                Intent.createChooser(
                                    intent, context.getString(R.string.share_app_chooser)
                                )
                            )
                        })

                        SwipeableActionsBox(
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                            startActions = listOf(shareAction),
                            endActions = listOf(detailsAction),
                            swipeThreshold = 85.dp
                        ) {
                            LibraryCard(title = item.title,
                                author = item.authors,
                                item.getFileSize(),
                                item.getDownloadDate(),
                                onReadClick = {
                                    (Utils.openBookFile(context, item))

                                    readerVM.parseEpubFile(item.filePath)
                                },
                                onDeleteClick = { openDeleteDialog.value = true })
                        }

                        if (openDeleteDialog.value) {
                            AlertDialog(onDismissRequest = {
                                openDeleteDialog.value = false
                            }, title = {
                                Text(
                                    text = stringResource(id = R.string.library_delete_dialog_title),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }, confirmButton = {
                                TextButton(onClick = {
                                    openDeleteDialog.value = false
                                    val fileDeleted = item.deleteFile()
                                    if (fileDeleted) {
                                        viewModel.deleteItem(item)
                                    } else {
                                        context.getString(R.string.error).toToast(context)
                                    }
                                }) {
                                    Text(stringResource(id = R.string.dialog_confirm_button))
                                }
                            }, dismissButton = {
                                TextButton(onClick = {
                                    openDeleteDialog.value = false
                                }) {
                                    Text(stringResource(id = R.string.cancel))
                                }
                            })
                        }

                    } else {
                        viewModel.deleteItem(item)
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryCard(
    title: String,
    author: String,
    fileSize: String,
    date: String,
    onReadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(125.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                3.dp
            )
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(90.dp)
                    .width(90.dp)
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_library_item),
                    contentDescription = stringResource(id = R.string.back_button_desc),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
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

                Spacer(modifier = Modifier.height(10.dp))

                Row {
                    LibraryCardButton(
                        text = stringResource(id = R.string.library_read_button),
                        icon = ImageVector.vectorResource(id = R.drawable.ic_library_read),
                        onClick = { onReadClick() }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    LibraryCardButton(
                        text = stringResource(id = R.string.library_delete_button),
                        icon = Icons.Outlined.Delete,
                        onClick = { onDeleteClick() }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun LibraryCardButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(6.dp)


        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size = 15.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                fontFamily = figeronaFont,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 2.dp, bottom = 1.dp),
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
@Preview
fun LibraryScreenPreview() {
    LibraryCard(title = "The Idiot",
        author = "Fyodor Dostoevsky",
        fileSize = "5.9MB",
        date = "01- Jan -2020",
        onReadClick = {},
        onDeleteClick = {})
}

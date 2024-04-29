package com.starry.myne.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starry.myne.R
import com.starry.myne.helpers.book.BookLanguage
import com.starry.myne.helpers.weakHapticFeedback
import com.starry.myne.ui.theme.pacificoFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookLanguageSheet(
    showBookLanguage: MutableState<Boolean>,
    selectedLanguage: BookLanguage,
    onLanguageChange: (BookLanguage) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    if (showBookLanguage.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    delay(300)
                    showBookLanguage.value = false
                }
            }
        ) {
            val languages = BookLanguage.getAllLanguages()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    text = stringResource(id = R.string.language_menu_title),
                    textAlign = TextAlign.Center,
                    fontFamily = pacificoFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                )
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp),
                    columns = GridCells.Adaptive(168.dp)
                ) {
                    items(languages.size) { idx ->
                        val language = languages[idx]
                        BookLanguageButton(language = language,
                            isSelected = language == selectedLanguage,
                            onClick = {
                                view.weakHapticFeedback()
                                coroutineScope.launch {
                                    sheetState.hide()
                                    delay(300)
                                    showBookLanguage.value = false
                                    onLanguageChange(language)
                                }
                            })
                    }
                }
            }
        }
    }
}
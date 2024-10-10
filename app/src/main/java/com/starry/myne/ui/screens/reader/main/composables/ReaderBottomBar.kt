package com.starry.myne.ui.screens.reader.main.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starry.myne.R
import com.starry.myne.ui.screens.reader.main.viewmodel.ReaderFont
import com.starry.myne.ui.screens.reader.main.viewmodel.ReaderScreenState
import com.starry.myne.ui.theme.poppinsFont
import kotlinx.coroutines.launch

private enum class TextScaleButtonType { INCREASE, DECREASE }

@Composable
fun ReaderBottomBar(
    state: ReaderScreenState,
    showFontDialog: MutableState<Boolean>,
    snackBarHostState: SnackbarHostState,
    onFontSizeChanged: (newValue: Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        TextScaleControls(
            fontSize = state.fontSize,
            snackBarHostState = snackBarHostState,
            onFontSizeChanged = onFontSizeChanged
        )

        Spacer(modifier = Modifier.height(14.dp))

        FontSelectionButton(
            readerFontFamily = state.fontFamily,
            showFontDialog = showFontDialog
        )
    }
}


@Composable
private fun TextScaleControls(
    fontSize: Int,
    snackBarHostState: SnackbarHostState,
    onFontSizeChanged: (newValue: Int) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.width(335.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReaderTextScaleButton(
                buttonType = TextScaleButtonType.DECREASE,
                fontSize = fontSize,
                snackBarHostState = snackBarHostState,
                onFontSizeChanged = onFontSizeChanged
            )

            Box(
                modifier = Modifier
                    .size(100.dp, 45.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ButtonDefaults.filledTonalButtonColors().containerColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fontSize.toString(),
                    fontFamily = poppinsFont,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 2.dp, bottom = 1.dp)
                )
            }

            ReaderTextScaleButton(
                buttonType = TextScaleButtonType.INCREASE,
                fontSize = fontSize,
                snackBarHostState = snackBarHostState,
                onFontSizeChanged = onFontSizeChanged
            )
        }
    }
}

@Composable
private fun FontSelectionButton(
    readerFontFamily: ReaderFont,
    showFontDialog: MutableState<Boolean>
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        FilledTonalButton(
            onClick = { showFontDialog.value = true },
            modifier = Modifier
                .width(365.dp)
                .padding(horizontal = 14.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_reader_font),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = readerFontFamily.name,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


@Composable
private fun ReaderTextScaleButton(
    buttonType: TextScaleButtonType,
    fontSize: Int,
    snackBarHostState: SnackbarHostState,
    onFontSizeChanged: (newValue: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val (iconRes, adjustment) = remember(buttonType) {
        when (buttonType) {
            TextScaleButtonType.DECREASE -> Pair(R.drawable.ic_reader_text_minus, -5)
            TextScaleButtonType.INCREASE -> Pair(R.drawable.ic_reader_text_plus, 5)
        }
    }

    val callback: () -> Unit = {
        val newSize = fontSize + adjustment
        when {
            newSize < 50 -> {
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(
                        context.getString(R.string.reader_min_font_size_reached),
                        null
                    )
                }
            }

            newSize > 200 -> {
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(
                        context.getString(R.string.reader_max_font_size_reached),
                        null
                    )
                }
            }

            else -> {
                coroutineScope.launch {
                    val adjustedSize = fontSize + adjustment
                    onFontSizeChanged(adjustedSize)
                }
            }
        }
    }

    FilledTonalButton(
        onClick = { callback() },
        modifier = Modifier.size(100.dp, 45.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
    }
}

@Composable
fun ReaderFontChooserDialog(
    showFontDialog: MutableState<Boolean>,
    fontFamily: ReaderFont,
    onFontFamilyChanged: (ReaderFont) -> Unit
) {
    val radioOptions = ReaderFont.getAllFonts().map { it.name }
    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(fontFamily)
    }

    if (showFontDialog.value) {
        AlertDialog(onDismissRequest = {
            showFontDialog.value = false
        }, title = {
            Text(
                text = stringResource(id = R.string.reader_font_style_chooser),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }, text = {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.Center,
            ) {
                radioOptions.forEach { text ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .selectable(
                                selected = (text == selectedOption.name),
                                onClick = { onOptionSelected(ReaderFont.getFontByName(text)) },
                                role = Role.RadioButton,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (text == selectedOption.name),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.inversePrimary,
                                disabledSelectedColor = Color.Black,
                                disabledUnselectedColor = Color.Black
                            ),
                        )
                        Text(
                            text = text,
                            modifier = Modifier.padding(start = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = poppinsFont
                        )
                    }
                }
            }
        }, confirmButton = {
            FilledTonalButton(
                onClick = {
                    showFontDialog.value = false
                    onFontFamilyChanged(selectedOption)
                }, colors = ButtonDefaults.filledTonalButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(id = R.string.theme_dialog_apply_button))
            }
        }, dismissButton = {
            TextButton(onClick = {
                showFontDialog.value = false
            }) {
                Text(stringResource(id = R.string.cancel))
            }
        })
    }
}

/**
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.starry.myne.ui.screens.welcome.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.starry.myne.R
import com.starry.myne.ui.common.SlideInAnimatedContainer
import com.starry.myne.ui.navigation.BottomBarScreen
import com.starry.myne.ui.screens.welcome.viewmodels.WelcomeViewModel
import com.starry.myne.ui.theme.figeronaFont

@Composable
fun WelcomeScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: WelcomeViewModel = hiltViewModel()

    val internalReaderValue = when (viewModel.getInternalReaderSetting()) {
        true -> stringResource(id = R.string.reader_option_inbuilt)
        false -> stringResource(id = R.string.reader_option_external)
    }
    val internalReaderDialog = remember { mutableStateOf(false) }
    val radioOptions = listOf(
        stringResource(id = R.string.reader_option_inbuilt),
        stringResource(id = R.string.reader_option_external)
    )
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(internalReaderValue) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center
    ) {
        val compositionResult: LottieCompositionResult = rememberLottieComposition(
            spec = LottieCompositionSpec.RawRes(R.raw.welcome_lottie)
        )
        val progressAnimation by animateLottieCompositionAsState(
            compositionResult.value,
            isPlaying = true,
            iterations = LottieConstants.IterateForever,
            speed = 1f
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            LottieAnimation(
                composition = compositionResult.value,
                progress = { progressAnimation },
                modifier = Modifier.size(300.dp),
                enableMergePaths = true
            )
        }

        SlideInAnimatedContainer(initialDelay = 2500L) {
            ReaderSelectionCard(
                internalReaderValue = internalReaderValue,
                onReaderClicked = {
                    internalReaderDialog.value = true
                },
                onContinueClicked = {
                    viewModel.saveOnBoardingState(completed = true)
                    navController.popBackStack()
                    navController.navigate(BottomBarScreen.Home.route)
                }
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        if (internalReaderDialog.value) {
            AlertDialog(onDismissRequest = {
                internalReaderDialog.value = false
            }, title = {
                Text(
                    text = stringResource(id = R.string.default_reader_dialog_title),
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
                                    selected = (text == selectedOption),
                                    onClick = { onOptionSelected(text) },
                                    role = Role.RadioButton,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = (text == selectedOption),
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
                                fontFamily = figeronaFont
                            )
                        }
                    }
                }
            }, confirmButton = {
                FilledTonalButton(
                    onClick = {
                        internalReaderDialog.value = false

                        when (selectedOption) {
                            context.getString(R.string.reader_option_inbuilt) -> {
                                viewModel.setInternalReaderSetting(true)
                            }

                            context.getString(R.string.reader_option_external) -> {
                                viewModel.setInternalReaderSetting(false)
                            }
                        }
                    }, colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(id = R.string.confirm))
                }
            }, dismissButton = {
                TextButton(onClick = {
                    internalReaderDialog.value = false
                }) {
                    Text(stringResource(id = R.string.cancel))
                }
            })
        }
    }
}

@Composable
fun ReaderSelectionCard(
    internalReaderValue: String,
    onReaderClicked: () -> Unit,
    onContinueClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                4.dp
            )
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = stringResource(id = R.string.welcome_screen_title),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(id = R.string.welcome_screen_desc),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onReaderClicked,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = internalReaderValue,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onContinueClicked,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.welcome_screen_button),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

        }

    }
}
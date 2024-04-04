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

package com.starry.myne.ui.screens.settings.composables

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.starry.myne.BuildConfig
import com.starry.myne.MainActivity
import com.starry.myne.R
import com.starry.myne.ui.common.CustomTopAppBar
import com.starry.myne.ui.navigation.Screens
import com.starry.myne.ui.screens.settings.viewmodels.SettingsViewModel
import com.starry.myne.ui.screens.settings.viewmodels.ThemeMode
import com.starry.myne.ui.theme.figeronaFont
import com.starry.myne.utils.getActivity
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = (context.getActivity() as MainActivity).settingsViewModel

    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.padding(bottom = 70.dp),
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            CustomTopAppBar(
                headerText = stringResource(id = R.string.settings_header),
                iconRes = R.drawable.ic_nav_settings
            )
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                SettingsCard()
                GeneralOptionsUI(viewModel = viewModel, context = context)
                DisplayOptionsUI(viewModel = viewModel, context = context, snackBarHostState)
                InformationUI(navController = navController)
            }
        }
    }
}

@Composable
@ExperimentalMaterial3Api
fun SettingsCard() {
    Card(
        modifier = Modifier
            .height(150.dp)
            .padding(10.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${stringResource(id = R.string.app_name)} ${stringResource(id = R.string.app_desc)}",
                    fontFamily = figeronaFont,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = stringResource(id = R.string.made_by),
                    fontFamily = figeronaFont,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Button(
                    modifier = Modifier.padding(top = 10.dp),
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 30.dp),
                ) {
                    Text(
                        text = "version-${BuildConfig.VERSION_NAME}",
                        fontFamily = figeronaFont,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .height(90.dp)
                    .width(90.dp)
                    .clip(CircleShape)
                    //  .padding(10.dp)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_splash_screen),
                    contentDescription = null,
                    modifier = Modifier.size(110.dp)
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun GeneralOptionsUI(viewModel: SettingsViewModel, context: Context) {
    val internalReaderValue = when (viewModel.getInternalReaderValue()) {
        true -> stringResource(id = R.string.reader_option_inbuilt)
        false -> stringResource(id = R.string.reader_option_external)
    }
    val showReaderDialog = remember { mutableStateOf(false) }
    val radioOptions = listOf(
        stringResource(id = R.string.reader_option_inbuilt),
        stringResource(id = R.string.reader_option_external)
    )
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(internalReaderValue) }

    Column(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .padding(top = 10.dp)
    ) {
        Text(
            text = stringResource(id = R.string.general_settings_header),
            fontFamily = figeronaFont,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        SettingItem(
            icon = ImageVector.vectorResource(id = R.drawable.ic_settings_reader),
            mainText = stringResource(id = R.string.default_reader_setting),
            subText = internalReaderValue,
            onClick = { showReaderDialog.value = true }
        )
    }

    if (showReaderDialog.value) {
        AlertDialog(onDismissRequest = {
            showReaderDialog.value = false
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
            TextButton(onClick = {
                showReaderDialog.value = false

                when (selectedOption) {
                    context.getString(R.string.reader_option_inbuilt) -> {
                        viewModel.setInternalReaderValue(true)
                    }

                    context.getString(R.string.reader_option_external) -> {
                        viewModel.setInternalReaderValue(false)
                    }
                }
            }) {
                Text(stringResource(id = R.string.confirm))
            }
        }, dismissButton = {
            TextButton(onClick = {
                showReaderDialog.value = false
            }) {
                Text(stringResource(id = R.string.cancel))
            }
        })
    }
}


@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@Composable
fun DisplayOptionsUI(
    viewModel: SettingsViewModel,
    context: Context,
    snackBarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()

    val displayValue =
        when (viewModel.getThemeValue()) {
            ThemeMode.Light.ordinal -> stringResource(id = R.string.theme_option_light)
            ThemeMode.Dark.ordinal -> stringResource(id = R.string.theme_option_dark)
            else -> stringResource(id = R.string.theme_option_system)
        }
    val displayDialog = remember { mutableStateOf(false) }
    val radioOptions = listOf(
        stringResource(id = R.string.theme_option_light),
        stringResource(id = R.string.theme_option_dark),
        stringResource(id = R.string.theme_option_system)
    )
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(displayValue) }

    val materialYouSwitch = remember { mutableStateOf(viewModel.getMaterialYouValue()) }
    val materialYouDesc = if (materialYouSwitch.value) {
        stringResource(id = R.string.material_you_settings_enabled_desc)
    } else {
        stringResource(id = R.string.material_you_settings_disabled_desc)
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .padding(top = 10.dp)
    ) {
        Text(
            text = stringResource(id = R.string.display_setting_header),
            fontFamily = figeronaFont,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        SettingItem(
            icon = ImageVector.vectorResource(id = R.drawable.ic_settings_theme),
            mainText = stringResource(id = R.string.theme_setting),
            subText = displayValue,
            onClick = { displayDialog.value = true }
        )
        SettingItemWIthSwitch(
            icon = ImageVector.vectorResource(id = R.drawable.ic_settings_material_you),
            mainText = stringResource(id = R.string.material_you_setting),
            subText = materialYouDesc,
            switchState = materialYouSwitch,
            onCheckChange = { materialYouValue ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    viewModel.setMaterialYou(materialYouValue)
                    materialYouSwitch.value = materialYouValue
                } else {
                    viewModel.setMaterialYou(false)
                    materialYouSwitch.value = false
                    coroutineScope.launch { snackBarHostState.showSnackbar(context.getString(R.string.material_you_error)) }
                }
            }
        )
    }

    if (displayDialog.value) {
        AlertDialog(onDismissRequest = {
            displayDialog.value = false
        }, title = {
            Text(
                text = stringResource(id = R.string.theme_dialog_title),
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
            TextButton(onClick = {
                displayDialog.value = false

                when (selectedOption) {
                    context.getString(R.string.theme_option_light) -> {
                        viewModel.setTheme(ThemeMode.Light)
                    }

                    context.getString(R.string.theme_option_dark) -> {
                        viewModel.setTheme(ThemeMode.Dark)
                    }

                    context.getString(R.string.theme_option_system) -> {
                        viewModel.setTheme(ThemeMode.Auto)
                    }
                }
            }) {
                Text(stringResource(id = R.string.theme_dialog_apply_button))
            }
        }, dismissButton = {
            TextButton(onClick = {
                displayDialog.value = false
            }) {
                Text(stringResource(id = R.string.cancel))
            }
        })
    }
}

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
fun InformationUI(navController: NavController) {
    Box {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .padding(top = 10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.miscellaneous_setting_header),
                fontFamily = figeronaFont,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            SettingItem(
                icon = ImageVector.vectorResource(id = R.drawable.ic_settings_license),
                mainText = stringResource(id = R.string.license_setting),
                subText = stringResource(id = R.string.license_setting_desc),
                onClick = { navController.navigate(Screens.OSLScreen.route) }
            )
            SettingItem(
                icon = ImageVector.vectorResource(id = R.drawable.ic_settings_about),
                mainText = stringResource(id = R.string.about_setting),
                subText = stringResource(id = R.string.about_setting_desc),
                onClick = { navController.navigate(Screens.AboutScreen.route) }
            )

        }
    }

}

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
@Preview
fun SettingsScreenPreview() {
    SettingsScreen(rememberNavController())
}
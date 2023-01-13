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

import android.content.Context
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.starry.myne.navigation.Screens
import com.starry.myne.ui.common.CustomTopAppBar
import com.starry.myne.ui.theme.figeronaFont
import com.starry.myne.ui.viewmodels.SettingsViewModel
import com.starry.myne.ui.viewmodels.ThemeMode
import com.starry.myne.utils.PreferenceUtils
import com.starry.myne.utils.getActivity
import com.starry.myne.utils.toToast
import kotlinx.coroutines.launch

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = (context.getActivity() as MainActivity).settingsViewModel

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
                headerText = stringResource(id = R.string.settings_header),
                icon = R.drawable.ic_nav_settings
            )
            Divider(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                thickness = 2.dp,
            )
        }

        SettingsCard {
            navController.navigate(Screens.AboutScreen.route)
        }

        DisplayOptionsUI(viewModel, context)
        InformationUI(navController, viewModel, context)
    }
}

@Composable
@ExperimentalMaterial3Api
fun SettingsCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(150.dp)
            .padding(10.dp)
            .fillMaxWidth(),
        onClick = onClick,
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
                        text = "v${BuildConfig.VERSION_NAME}",
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

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@Composable
fun DisplayOptionsUI(viewModel: SettingsViewModel, context: Context) {
    val displayValue =
        when (PreferenceUtils.getInt(PreferenceUtils.APP_THEME, ThemeMode.Auto.ordinal)) {
            ThemeMode.Light.ordinal -> "Light"
            ThemeMode.Dark.ordinal -> "Dark"
            else -> "System"
        }
    val displayDialog = remember { mutableStateOf(false) }
    val radioOptions = listOf("Light", "Dark", "System")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(displayValue) }

    val materialYouSwitch = remember {
        mutableStateOf(
            PreferenceUtils.getBoolean(
                PreferenceUtils.MATERIAL_YOU, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            )
        )
    }

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
        SettingItem(icon = R.drawable.ic_settings_theme,
            mainText = stringResource(id = R.string.theme_setting),
            subText = displayValue,
            onClick = { displayDialog.value = true })
        SettingItemWIthSwitch(
            icon = R.drawable.ic_settings_material_you,
            mainText = stringResource(id = R.string.material_you_setting),
            subText = materialYouDesc,
            switchState = materialYouSwitch
        )
    }

    if (materialYouSwitch.value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            viewModel.setMaterialYou(true)
            PreferenceUtils.putBoolean(PreferenceUtils.MATERIAL_YOU, true)
        } else {
            materialYouSwitch.value = false
            stringResource(id = R.string.material_you_error).toToast(context)
        }
    } else {
        viewModel.setMaterialYou(false)
        PreferenceUtils.putBoolean(PreferenceUtils.MATERIAL_YOU, false)
    }

    if (displayDialog.value) {
        AlertDialog(onDismissRequest = {
            displayDialog.value = false
        }, title = {
            Text(
                text = "Theme",
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
                    "Light" -> {
                        viewModel.setTheme(
                            ThemeMode.Light
                        )
                        PreferenceUtils.putInt(
                            PreferenceUtils.APP_THEME, ThemeMode.Light.ordinal
                        )
                    }
                    "Dark" -> {
                        viewModel.setTheme(
                            ThemeMode.Dark
                        )
                        PreferenceUtils.putInt(
                            PreferenceUtils.APP_THEME, ThemeMode.Dark.ordinal
                        )
                    }
                    "System" -> {
                        viewModel.setTheme(
                            ThemeMode.Auto
                        )
                        PreferenceUtils.putInt(
                            PreferenceUtils.APP_THEME, ThemeMode.Auto.ordinal
                        )
                    }
                }
            }) {
                Text("Apply")
            }
        }, dismissButton = {
            TextButton(onClick = {
                displayDialog.value = false
            }) {
                Text("Cancel")
            }
        })
    }
}

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
fun InformationUI(navController: NavController, viewModel: SettingsViewModel, context: Context) {
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) {
        Box(modifier = Modifier.padding(it)) {
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
                SettingItem(icon = R.drawable.ic_settings_license,
                    mainText = stringResource(id = R.string.license_setting),
                    subText = stringResource(id = R.string.license_setting_desc),
                    onClick = { navController.navigate(Screens.OSLScreen.route) })
                SettingItem(icon = R.drawable.ic_settings_update,
                    mainText = stringResource(id = R.string.update_setting),
                    subText = stringResource(id = R.string.update_setting_desc),
                    onClick = {
                        viewModel.checkForUpdates { updateAvailable, newReleaseLink, errorOnRequest ->
                            if (errorOnRequest) {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = context.getString(R.string.error),
                                    )
                                }
                            }
                            if (updateAvailable) {
                                coroutineScope.launch {
                                    val result = snackBarHostState.showSnackbar(
                                        message = context.getString(R.string.update_available),
                                        actionLabel = "UPDATE"
                                    )

                                    when (result) {
                                        SnackbarResult.ActionPerformed -> {
                                            viewModel.downloadUpdate(
                                                newReleaseLink!!,
                                                (context.getActivity() as MainActivity)
                                            )
                                        }
                                        SnackbarResult.Dismissed -> {
                                            /* dismissed, no action needed */
                                        }
                                    }
                                }
                            } else {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = context.getString(R.string.no_update_available)
                                    )
                                }
                            }
                        }
                    })
            }
        }
    }
}


@ExperimentalMaterial3Api
@Composable
fun SettingItem(icon: Int, mainText: String, subText: String, onClick: () -> Unit) {
    Card(
        onClick = { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                2.dp
            )
        ),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .height(69.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 14.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier.offset(y = (2).dp)
                ) {
                    Text(
                        text = mainText,
                        fontFamily = figeronaFont,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subText,
                        fontFamily = figeronaFont,
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_right_arrow),
                contentDescription = "",
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun SettingItemWIthSwitch(
    icon: Int, mainText: String, subText: String, switchState: MutableState<Boolean>
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                2.dp
            )
        ),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .height(69.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 14.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier.offset(y = (2).dp)
                ) {
                    Text(
                        text = mainText,
                        fontFamily = figeronaFont,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subText,
                        fontFamily = figeronaFont,
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }
            }
            Switch(checked = switchState.value, onCheckedChange = { switchState.value = it })
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
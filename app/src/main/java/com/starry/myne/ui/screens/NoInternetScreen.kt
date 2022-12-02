package com.starry.myne.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.starry.myne.R

@ExperimentalComposeUiApi
@Composable
fun NoInternetScreen() {
    Dialog(
        onDismissRequest = {
            // TODO
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false // experimental
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_no_internet),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .height(225.dp)
                        .fillMaxWidth(),

                    )

                Text(
                    text = stringResource(id = R.string.woops),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.no_internet_connection),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 10.dp, start = 25.dp, end = 25.dp)
                        .fillMaxWidth(),
                    letterSpacing = 1.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}


@Preview
@ExperimentalComposeUiApi
@Composable
fun NoInternetScreenPreview() {
    NoInternetScreen()
}



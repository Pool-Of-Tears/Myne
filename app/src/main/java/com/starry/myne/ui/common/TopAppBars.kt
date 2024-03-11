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

package com.starry.myne.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starry.myne.R
import com.starry.myne.ui.theme.pacificoFont

@Composable
fun CustomTopAppBar(headerText: String, iconRes: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = headerText,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = pacificoFont
            )
            Icon(
                imageVector = ImageVector.vectorResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(28.dp)
            )
        }
        HorizontalDivider(
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        )
    }
}

@Composable
fun CustomTopAppBar(headerText: String, onBackButtonClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            TopBarActionItem(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                onclick = onBackButtonClicked
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = headerText,
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                fontFamily = pacificoFont,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.weight(2.35f))
        }
        HorizontalDivider(
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        )
    }
}

@Composable
fun CustomTopAppBar(
    headerText: String,
    actionIconRes: Int,
    onBackButtonClicked: () -> Unit,
    onActionClicked: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            TopBarActionItem(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                onclick = onBackButtonClicked
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = headerText,
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                fontFamily = pacificoFont,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            TopBarActionItem(
                icon = ImageVector.vectorResource(id = actionIconRes),
                onclick = onActionClicked
            )
        }
        HorizontalDivider(
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        )
    }

}

@Composable
private fun TopBarActionItem(icon: ImageVector, onclick: () -> Unit) {
    Box(modifier = Modifier
        .padding(start = 16.dp, top = 2.dp, bottom = 18.dp, end = 16.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
        .clickable { onclick() }) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = R.string.back_button_desc),
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TopAppBarsPV() {
    Column(Modifier.fillMaxSize()) {
        CustomTopAppBar(headerText = "Something", iconRes = R.drawable.ic_nav_categories)
        CustomTopAppBar(headerText = "Something", onBackButtonClicked = {})
        CustomTopAppBar(
            headerText = "Something",
            actionIconRes = R.drawable.ic_sort_language,
            onBackButtonClicked = { /*TODO*/ }, onActionClicked = {})
    }
}
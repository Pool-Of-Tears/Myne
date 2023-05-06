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

package com.starry.myne.ui.screens.categories.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.starry.myne.R
import com.starry.myne.ui.common.CustomTopAppBar
import com.starry.myne.ui.navigation.Screens
import com.starry.myne.ui.screens.categories.viewmodels.CategoryViewModel
import com.starry.myne.ui.theme.figeronaFont
import java.util.Locale

@ExperimentalMaterial3Api
@Composable
fun CategoriesScreen(navController: NavController) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            CustomTopAppBar(
                headerText = stringResource(id = R.string.categories_header),
                iconRes = R.drawable.ic_nav_categories
            )
        }, content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 70.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(168.dp),
                        content = {
                            items(CategoryViewModel.CATEGORIES_ARRAY.size) { i ->
                                val category =
                                    CategoryViewModel.CATEGORIES_ARRAY[i].replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(
                                            Locale.getDefault()
                                        ) else it.toString()
                                    }
                                CategoriesItem(category) {
                                    navController.navigate(
                                        Screens.CategoryDetailScreen.withCategory(category)
                                    )
                                }
                            }
                        },
                    )
                }
            }
        })

}


@ExperimentalMaterial3Api
@Composable
fun CategoriesItem(category: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(90.dp)
            .width(160.dp)
            .padding(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                2.dp
            )
        ),
        shape = RoundedCornerShape(6.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.padding(2.dp),
                text = category,
                fontSize = 18.sp,
                fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                fontFamily = figeronaFont,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
@Preview(showBackground = true)
fun CategoriesScreenPreview() {
    CategoriesScreen(rememberNavController())
}
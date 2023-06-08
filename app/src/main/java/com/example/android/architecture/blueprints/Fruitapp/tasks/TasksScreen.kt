/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.Fruitapp.fruits

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.architecture.blueprints.Fruitapp.R
import com.example.android.architecture.blueprints.Fruitapp.data.fruit
import com.example.android.architecture.blueprints.Fruitapp.fruits.fruitsFilterType.ACTIVE_fruitS
import com.example.android.architecture.blueprints.Fruitapp.fruits.fruitsFilterType.ALL_fruitS
import com.example.android.architecture.blueprints.Fruitapp.fruits.fruitsFilterType.COMPLETED_fruitS
import com.example.android.architecture.blueprints.Fruitapp.util.LoadingContent
import com.example.android.architecture.blueprints.Fruitapp.util.fruitsTopAppBar
import com.google.accompanist.appcompattheme.AppCompatTheme

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun fruitsScreen(
    @StringRes userMessage: Int,
    onAddfruit: () -> Unit,
    onfruitClick: (fruit) -> Unit,
    onUserMessageDisplayed: () -> Unit,
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: fruitsViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            fruitsTopAppBar(
                openDrawer = openDrawer,
                onFilterAllfruits = { viewModel.setFiltering(ALL_fruitS) },
                onFilterActivefruits = { viewModel.setFiltering(ACTIVE_fruitS) },
                onFilterCompletedfruits = { viewModel.setFiltering(COMPLETED_fruitS) },
                onClearCompletedfruits = { viewModel.clearCompletedfruits() },
                onRefresh = { viewModel.refresh() }
            )
        },
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddfruit) {
                Icon(Icons.Filled.Add, stringResource(id = R.string.add_fruit))
            }
        }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        fruitsContent(
            loading = uiState.isLoading,
            fruits = uiState.items,
            currentFilteringLabel = uiState.filteringUiInfo.currentFilteringLabel,
            nofruitsLabel = uiState.filteringUiInfo.nofruitsLabel,
            nofruitsIconRes = uiState.filteringUiInfo.nofruitIconRes,
            onRefresh = viewModel::refresh,
            onfruitClick = onfruitClick,
            onfruitCheckedChange = viewModel::completefruit,
            modifier = Modifier.padding(paddingValues)
        )

        // Check for user messages to display on the screen
        uiState.userMessage?.let { message ->
            val snackbarText = stringResource(message)
            LaunchedEffect(scaffoldState, viewModel, message, snackbarText) {
                scaffoldState.snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        // Check if there's a userMessage to show to the user
        val currentOnUserMessageDisplayed by rememberUpdatedState(onUserMessageDisplayed)
        LaunchedEffect(userMessage) {
            if (userMessage != 0) {
                viewModel.showEditResultMessage(userMessage)
                currentOnUserMessageDisplayed()
            }
        }
    }
}

@Composable
private fun fruitsContent(
    loading: Boolean,
    fruits: List<fruit>,
    @StringRes currentFilteringLabel: Int,
    @StringRes nofruitsLabel: Int,
    @DrawableRes nofruitsIconRes: Int,
    onRefresh: () -> Unit,
    onfruitClick: (fruit) -> Unit,
    onfruitCheckedChange: (fruit, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LoadingContent(
        loading = loading,
        empty = fruits.isEmpty() && !loading,
        emptyContent = { fruitsEmptyContent(nofruitsLabel, nofruitsIconRes, modifier) },
        onRefresh = onRefresh
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))
        ) {
            Text(
                text = stringResource(currentFilteringLabel),
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.list_item_padding),
                    vertical = dimensionResource(id = R.dimen.vertical_margin)
                ),
                style = MaterialTheme.typography.h6
            )
            LazyColumn {
                items(fruits) { fruit ->
                    fruitItem(
                        fruit = fruit,
                        onfruitClick = onfruitClick,
                        onCheckedChange = { onfruitCheckedChange(fruit, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun fruitItem(
    fruit: fruit,
    onCheckedChange: (Boolean) -> Unit,
    onfruitClick: (fruit) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.horizontal_margin),
                vertical = dimensionResource(id = R.dimen.list_item_padding),
            )
            .clickable { onfruitClick(fruit) }
    ) {
        Checkbox(
            checked = fruit.isCompleted,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = fruit.titleForList,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.horizontal_margin)
            ),
            textDecoration = if (fruit.isCompleted) {
                TextDecoration.LineThrough
            } else {
                null
            }
        )
    }
}

@Composable
private fun fruitsEmptyContent(
    @StringRes nofruitsLabel: Int,
    @DrawableRes nofruitsIconRes: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = nofruitsIconRes),
            contentDescription = stringResource(R.string.no_fruits_image_content_description),
            modifier = Modifier.size(96.dp)
        )
        Text(stringResource(id = nofruitsLabel))
    }
}

@Preview
@Composable
private fun fruitsContentPreview() {
    AppCompatTheme {
        Surface {
            fruitsContent(
                loading = false,
                fruits = listOf(
                    fruit(
                        title = "Title 1",
                        description = "Description 1",
                        categoria = "Description 2",
                        isCompleted = false,
                        id = "ID 1"
                    ),
                    fruit(
                        title = "Title 2",
                        description = "Description 2",
                        categoria = "Description 2",
                        isCompleted = true,
                        id = "ID 2"
                    ),
                    fruit(
                        title = "Title 3",
                        description = "Description 3",
                        categoria = "Description 2",
                        isCompleted = true,
                        id = "ID 3"
                    ),
                    fruit(
                        title = "Title 4",
                        description = "Description 4",
                        categoria = "Description 2",
                        isCompleted = false,
                        id = "ID 4"
                    ),
                    fruit(
                        title = "Title 5",
                        description = "Description 5",
                        categoria = "Description 2",
                        isCompleted = true,
                        id = "ID 5"
                    ),
                ),
                currentFilteringLabel = R.string.label_all,
                nofruitsLabel = R.string.no_fruits_all,
                nofruitsIconRes = R.drawable.logo_no_fill,
                onRefresh = { },
                onfruitClick = { },
                onfruitCheckedChange = { _, _ -> },
            )
        }
    }
}

@Preview
@Composable
private fun fruitsContentEmptyPreview() {
    AppCompatTheme {
        Surface {
            fruitsContent(
                loading = false,
                fruits = emptyList(),
                currentFilteringLabel = R.string.label_all,
                nofruitsLabel = R.string.no_fruits_all,
                nofruitsIconRes = R.drawable.logo_no_fill,
                onRefresh = { },
                onfruitClick = { },
                onfruitCheckedChange = { _, _ -> },
            )
        }
    }
}

@Preview
@Composable
private fun fruitsEmptyContentPreview() {
    AppCompatTheme {
        Surface {
            fruitsEmptyContent(
                nofruitsLabel = R.string.no_fruits_all,
                nofruitsIconRes = R.drawable.logo_no_fill
            )
        }
    }
}

@Preview
@Composable
private fun fruitItemPreview() {
    AppCompatTheme {
        Surface {
            fruitItem(
                fruit = fruit(
                    title = "Title",
                    description = "Description",
                    categoria = "Description 2",
                    id = "ID"
                ),
                onfruitClick = { },
                onCheckedChange = { }
            )
        }
    }
}

@Preview
@Composable
private fun fruitItemCompletedPreview() {
    AppCompatTheme {
        Surface {
            fruitItem(
                fruit = fruit(
                    title = "Title",
                    description = "Description",
                    categoria = "Description 2",
                    isCompleted = true,
                    id = "ID"
                ),
                onfruitClick = { },
                onCheckedChange = { }
            )
        }
    }
}

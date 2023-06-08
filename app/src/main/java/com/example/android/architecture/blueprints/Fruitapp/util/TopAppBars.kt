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

package com.example.android.architecture.blueprints.Fruitapp.util

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.android.architecture.blueprints.Fruitapp.R
import com.google.accompanist.appcompattheme.AppCompatTheme

@Composable
fun fruitsTopAppBar(
    openDrawer: () -> Unit,
    onFilterAllfruits: () -> Unit,
    onFilterActivefruits: () -> Unit,
    onFilterCompletedfruits: () -> Unit,
    onClearCompletedfruits: () -> Unit,
    onRefresh: () -> Unit
) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
            }
        },
        actions = {
            FilterfruitsMenu(onFilterAllfruits, onFilterActivefruits, onFilterCompletedfruits)
            MorefruitsMenu(onClearCompletedfruits, onRefresh)
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FilterfruitsMenu(
    onFilterAllfruits: () -> Unit,
    onFilterActivefruits: () -> Unit,
    onFilterCompletedfruits: () -> Unit
) {
    TopAppBarDropdownMenu(
        iconContent = {
            Icon(
                painterResource(id = R.drawable.ic_filter_list),
                stringResource(id = R.string.menu_filter)
            )
        }
    ) { closeMenu ->
        DropdownMenuItem(onClick = { onFilterAllfruits(); closeMenu() }) {
            Text(text = stringResource(id = R.string.nav_all))
        }
        DropdownMenuItem(onClick = { onFilterActivefruits(); closeMenu() }) {
            Text(text = stringResource(id = R.string.nav_active))
        }
        DropdownMenuItem(onClick = { onFilterCompletedfruits(); closeMenu() }) {
            Text(text = stringResource(id = R.string.nav_completed))
        }
    }
}

@Composable
private fun MorefruitsMenu(
    onClearCompletedfruits: () -> Unit,
    onRefresh: () -> Unit
) {
    TopAppBarDropdownMenu(
        iconContent = {
            Icon(Icons.Filled.MoreVert, stringResource(id = R.string.menu_more))
        }
    ) { closeMenu ->
        DropdownMenuItem(onClick = { onClearCompletedfruits(); closeMenu() }) {
            Text(text = stringResource(id = R.string.menu_clear))
        }
        DropdownMenuItem(onClick = { onRefresh(); closeMenu() }) {
            Text(text = stringResource(id = R.string.refresh))
        }
    }
}

@Composable
private fun TopAppBarDropdownMenu(
    iconContent: @Composable () -> Unit,
    content: @Composable ColumnScope.(() -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        IconButton(onClick = { expanded = !expanded }) {
            iconContent()
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
        ) {
            content { expanded = !expanded }
        }
    }
}

@Composable
fun StatisticsTopAppBar(openDrawer: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.statistics_title)) },
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun fruitDetailTopAppBar(onBack: () -> Unit, onDelete: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.fruit_details))
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.menu_back))
            }
        },
        actions = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, stringResource(id = R.string.menu_delete_fruit))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun AddEditfruitTopAppBar(@StringRes title: Int, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.menu_back))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview
@Composable
private fun fruitsTopAppBarPreview() {
    AppCompatTheme {
        Surface {
            fruitsTopAppBar({}, {}, {}, {}, {}, {})
        }
    }
}

@Preview
@Composable
private fun StatisticsTopAppBarPreview() {
    AppCompatTheme {
        Surface {
            StatisticsTopAppBar { }
        }
    }
}

@Preview
@Composable
private fun fruitDetailTopAppBarPreview() {
    AppCompatTheme {
        Surface {
            fruitDetailTopAppBar({ }, { })
        }
    }
}

@Preview
@Composable
private fun AddEditfruitTopAppBarPreview() {
    AppCompatTheme {
        Surface {
            AddEditfruitTopAppBar(R.string.add_fruit) { }
        }
    }
}

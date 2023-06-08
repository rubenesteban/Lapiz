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

package com.example.android.architecture.blueprints.Fruitapp

import android.app.Activity
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android.architecture.blueprints.Fruitapp.TodoDestinationsArgs.fruit_ID_ARG
import com.example.android.architecture.blueprints.Fruitapp.TodoDestinationsArgs.TITLE_ARG
import com.example.android.architecture.blueprints.Fruitapp.TodoDestinationsArgs.USER_MESSAGE_ARG
import com.example.android.architecture.blueprints.Fruitapp.addeditfruit.AddEditfruitScreen
import com.example.android.architecture.blueprints.Fruitapp.statistics.StatisticsScreen
import com.example.android.architecture.blueprints.Fruitapp.fruitdetail.fruitDetailScreen
import com.example.android.architecture.blueprints.Fruitapp.fruits.fruitsScreen
import com.example.android.architecture.blueprints.Fruitapp.util.AppModalDrawer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TodoNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    startDestination: String = TodoDestinations.fruitS_ROUTE,
    navActions: TodoNavigationActions = remember(navController) {
        TodoNavigationActions(navController)
    }
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            TodoDestinations.fruitS_ROUTE,
            arguments = listOf(
                navArgument(USER_MESSAGE_ARG) { type = NavType.IntType; defaultValue = 0 }
            )
        ) { entry ->
            AppModalDrawer(drawerState, currentRoute, navActions) {
                fruitsScreen(
                    userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
                    onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
                    onAddfruit = { navActions.navigateToAddEditfruit(R.string.add_fruit, null) },
                    onfruitClick = { fruit -> navActions.navigateTofruitDetail(fruit.id) },
                    openDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }
        }
        composable(TodoDestinations.STATISTICS_ROUTE) {
            AppModalDrawer(drawerState, currentRoute, navActions) {
                StatisticsScreen(openDrawer = { coroutineScope.launch { drawerState.open() } })
            }
        }
        composable(
            TodoDestinations.ADD_EDIT_fruit_ROUTE,
            arguments = listOf(
                navArgument(TITLE_ARG) { type = NavType.IntType },
                navArgument(fruit_ID_ARG) { type = NavType.StringType; nullable = true },
            )
        ) { entry ->
            val fruitId = entry.arguments?.getString(fruit_ID_ARG)
            AddEditfruitScreen(
                topBarTitle = entry.arguments?.getInt(TITLE_ARG)!!,
                onfruitUpdate = {
                    navActions.navigateTofruits(
                        if (fruitId == null) ADD_EDIT_RESULT_OK else EDIT_RESULT_OK
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(TodoDestinations.fruit_DETAIL_ROUTE) {
            fruitDetailScreen(
                onEditfruit = { fruitId ->
                    navActions.navigateToAddEditfruit(R.string.edit_fruit, fruitId)
                },
                onBack = { navController.popBackStack() },
                onDeletefruit = { navActions.navigateTofruits(DELETE_RESULT_OK) }
            )
        }
    }
}

// Keys for navigation
const val ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3

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

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.android.architecture.blueprints.Fruitapp.TodoDestinationsArgs.fruit_ID_ARG
import com.example.android.architecture.blueprints.Fruitapp.TodoDestinationsArgs.TITLE_ARG
import com.example.android.architecture.blueprints.Fruitapp.TodoDestinationsArgs.USER_MESSAGE_ARG
import com.example.android.architecture.blueprints.Fruitapp.TodoScreens.ADD_EDIT_fruit_SCREEN
import com.example.android.architecture.blueprints.Fruitapp.TodoScreens.STATISTICS_SCREEN
import com.example.android.architecture.blueprints.Fruitapp.TodoScreens.chitasS_SCREEN
import com.example.android.architecture.blueprints.Fruitapp.TodoScreens.fruitS_SCREEN
import com.example.android.architecture.blueprints.Fruitapp.TodoScreens.fruit_DETAIL_SCREEN
import com.example.android.architecture.blueprints.Fruitapp.TodoScreens.legueS_SCREEN
import com.example.android.architecture.blueprints.Fruitapp.TodoScreens.tigreS_SCREEN
import com.example.android.architecture.blueprints.Fruitapp.TodoScreens.vetteS_SCREEN

/**
 * Screens used in [TodoDestinations]
 */
private object TodoScreens {
    const val fruitS_SCREEN = "fruits"
    const val vetteS_SCREEN = "vegetables"
    const val legueS_SCREEN = "legumes"
    const val tigreS_SCREEN = "grains"
    const val chitasS_SCREEN = "groceries"
    const val STATISTICS_SCREEN = "statistics"
    const val fruit_DETAIL_SCREEN = "fruit"
    const val ADD_EDIT_fruit_SCREEN = "addEditfruit"
}

/**
 * Arguments used in [TodoDestinations] routes
 */
object TodoDestinationsArgs {
    const val USER_MESSAGE_ARG = "userMessage"
    const val fruit_ID_ARG = "fruitId"
    const val TITLE_ARG = "title"
}

/**
 * Destinations used in the [TodoActivity]
 */
object TodoDestinations {
    const val fruitS_ROUTE = "$fruitS_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
    const val vegetteS_ROUTE = "$vetteS_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
    const val legueS_ROUTE = "$legueS_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
    const val tigreS_ROUTE = "$tigreS_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
    const val chitasS_ROUTE = "$chitasS_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
    const val STATISTICS_ROUTE = STATISTICS_SCREEN
    const val fruit_DETAIL_ROUTE = "$fruit_DETAIL_SCREEN/{$fruit_ID_ARG}"
    const val ADD_EDIT_fruit_ROUTE = "$ADD_EDIT_fruit_SCREEN/{$TITLE_ARG}?$fruit_ID_ARG={$fruit_ID_ARG}"
}

/**
 * Models the navigation actions in the app.
 */
class TodoNavigationActions(private val navController: NavHostController) {

    fun navigateTofruits(userMessage: Int = 0) {
        val navigatesFromDrawer = userMessage == 0
        navController.navigate(
            fruitS_SCREEN.let {
                if (userMessage != 0) "$it?$USER_MESSAGE_ARG=$userMessage" else it
            }
        ) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = !navigatesFromDrawer
                saveState = navigatesFromDrawer
            }
            launchSingleTop = true
            restoreState = navigatesFromDrawer
        }
    }


    fun navigateTovegetales(userMessage: Int = 1) {
        val navigatesFromDrawer = userMessage == 1
        navController.navigate(
            vetteS_SCREEN.let {
                if (userMessage != 1) "$it?$USER_MESSAGE_ARG=$userMessage" else it
            }
        ) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = !navigatesFromDrawer
                saveState = navigatesFromDrawer
            }
            launchSingleTop = true
            restoreState = navigatesFromDrawer
        }
    }


    fun navigateToA(userMessage: Int = 2) {
        val navigatesFromDrawer = userMessage == 2
        navController.navigate(
            legueS_SCREEN.let {
                if (userMessage != 2) "$it?$USER_MESSAGE_ARG=$userMessage" else it
            }
        ) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = !navigatesFromDrawer
                saveState = navigatesFromDrawer
            }
            launchSingleTop = true
            restoreState = navigatesFromDrawer
        }
    }


    fun navigateToB(userMessage: Int = 3) {
        val navigatesFromDrawer = userMessage == 3
        navController.navigate(
            tigreS_SCREEN.let {
                if (userMessage != 3) "$it?$USER_MESSAGE_ARG=$userMessage" else it
            }
        ) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = !navigatesFromDrawer
                saveState = navigatesFromDrawer
            }
            launchSingleTop = true
            restoreState = navigatesFromDrawer
        }
    }



    fun navigateToC(userMessage: Int = 4) {
        val navigatesFromDrawer = userMessage == 4
        navController.navigate(
            chitasS_SCREEN.let {
                if (userMessage != 4) "$it?$USER_MESSAGE_ARG=$userMessage" else it
            }
        ) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = !navigatesFromDrawer
                saveState = navigatesFromDrawer
            }
            launchSingleTop = true
            restoreState = navigatesFromDrawer
        }
    }



    fun navigateToStatistics() {
        navController.navigate(TodoDestinations.STATISTICS_ROUTE) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }

    fun navigateTofruitDetail(fruitId: String) {
        navController.navigate("$fruit_DETAIL_SCREEN/$fruitId")
    }

    fun navigateToAddEditfruit(title: Int, fruitId: String?) {
        navController.navigate(
            "$ADD_EDIT_fruit_SCREEN/$title".let {
                if (fruitId != null) "$it?$fruit_ID_ARG=$fruitId" else it
            }
        )
    }
}

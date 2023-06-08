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

import androidx.annotation.StringRes
import androidx.compose.material.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.Fruitapp.HiltTestActivity
import com.example.android.architecture.blueprints.Fruitapp.R
import com.example.android.architecture.blueprints.Fruitapp.data.fruitRepository
import com.google.accompanist.appcompattheme.AppCompatTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for the fruit List screen.
 */
// TODO - Move to the sharedTest folder when https://issuetracker.google.com/224974381 is fixed
@RunWith(AndroidJUnit4::class)
@MediumTest
// @LooperMode(LooperMode.Mode.PAUSED)
// @TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class)
class fruitsScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()
    private val activity get() = composeTestRule.activity

    @Inject
    lateinit var repository: fruitRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun displayfruit_whenRepositoryHasData() = runTest {
        // GIVEN - One fruit already in the repository
        repository.createfruit("TITLE1", "DESCRIPTION1")

        // WHEN - On startup
        setContent()

        // THEN - Verify fruit is displayed on screen
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
    }

    @Test
    fun displayActivefruit() = runTest {
        repository.createfruit("TITLE1", "DESCRIPTION1")

        setContent()

        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()

        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()

        openFilterAndSelectOption(R.string.nav_completed)

        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
    }

    @Test
    fun displayCompletedfruit() = runTest {
        repository.apply {
            createfruit("TITLE1", "DESCRIPTION1").also { completefruit(it) }
        }

        setContent()

        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()

        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()

        openFilterAndSelectOption(R.string.nav_completed)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
    }

    @Test
    fun markfruitAsComplete() = runTest {
        repository.createfruit("TITLE1", "DESCRIPTION1")

        setContent()

        // Mark the fruit as complete
        composeTestRule.onNode(isToggleable()).performClick()

        // Verify fruit is shown as complete
        openFilterAndSelectOption(R.string.nav_all)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
        openFilterAndSelectOption(R.string.nav_completed)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
    }

    @Test
    fun markfruitAsActive() = runTest {
        repository.apply {
            createfruit("TITLE1", "DESCRIPTION1").also { completefruit(it) }
        }

        setContent()

        // Mark the fruit as active
        composeTestRule.onNode(isToggleable()).performClick()

        // Verify fruit is shown as active
        openFilterAndSelectOption(R.string.nav_all)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        openFilterAndSelectOption(R.string.nav_completed)
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
    }

    @Test
    fun showAllfruits() = runTest {
        // Add one active fruit and one completed fruit
        repository.apply {
            createfruit("TITLE1", "DESCRIPTION1")
            createfruit("TITLE2", "DESCRIPTION2").also { completefruit(it) }
        }

        setContent()

        // Verify that both of our fruits are shown
        openFilterAndSelectOption(R.string.nav_all)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
    }

    @Test
    fun showActivefruits() = runTest {
        // Add 2 active fruits and one completed fruit
        repository.apply {
            createfruit("TITLE1", "DESCRIPTION1")
            createfruit("TITLE2", "DESCRIPTION2")
            createfruit("TITLE3", "DESCRIPTION3").also { completefruit(it) }
        }

        setContent()

        // Verify that the active fruits (but not the completed fruit) are shown
        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE3").assertDoesNotExist()
    }

    @Test
    fun showCompletedfruits() = runTest {
        // Add one active fruit and 2 completed fruits
        repository.apply {
            createfruit("TITLE1", "DESCRIPTION1")
            createfruit("TITLE2", "DESCRIPTION2").also { completefruit(it) }
            createfruit("TITLE3", "DESCRIPTION3").also { completefruit(it) }
        }

        setContent()

        // Verify that the completed fruits (but not the active fruit) are shown
        openFilterAndSelectOption(R.string.nav_completed)
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE3").assertIsDisplayed()
    }

    @Test
    fun clearCompletedfruits() = runTest {
        // Add one active fruit and one completed fruit
        repository.apply {
            createfruit("TITLE1", "DESCRIPTION1")
            createfruit("TITLE2", "DESCRIPTION2").also { completefruit(it) }
        }

        setContent()

        // Click clear completed in menu
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_more))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.menu_clear)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.menu_clear)).performClick()

        openFilterAndSelectOption(R.string.nav_all)
        // Verify that only the active fruit is shown
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertDoesNotExist()
    }

    @Test
    fun nofruits_AllfruitsFilter_AddfruitViewVisible() {
        setContent()

        openFilterAndSelectOption(R.string.nav_all)

        // Verify the "You have no fruits!" text is shown
        composeTestRule.onNodeWithText("You have no fruits!").assertIsDisplayed()
    }

    @Test
    fun nofruits_CompletedfruitsFilter_AddfruitViewNotVisible() {
        setContent()

        openFilterAndSelectOption(R.string.nav_completed)
        // Verify the "You have no completed fruits!" text is shown
        composeTestRule.onNodeWithText("You have no completed fruits!").assertIsDisplayed()
    }

    @Test
    fun nofruits_ActivefruitsFilter_AddfruitViewNotVisible() {
        setContent()

        openFilterAndSelectOption(R.string.nav_active)
        // Verify the "You have no active fruits!" text is shown
        composeTestRule.onNodeWithText("You have no active fruits!").assertIsDisplayed()
    }

    private fun setContent() {
        composeTestRule.setContent {
            AppCompatTheme {
                Surface {
                    fruitsScreen(
                        viewModel = fruitsViewModel(repository, SavedStateHandle()),
                        userMessage = R.string.successfully_added_fruit_message,
                        onUserMessageDisplayed = { },
                        onAddfruit = { },
                        onfruitClick = { },
                        openDrawer = { }
                    )
                }
            }
        }
    }

    private fun openFilterAndSelectOption(@StringRes option: Int) {
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_filter))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(option)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(option)).performClick()
    }
}

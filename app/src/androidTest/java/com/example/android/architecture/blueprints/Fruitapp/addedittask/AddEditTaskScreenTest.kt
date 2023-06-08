/*
 * Copyright 2019 The Android Open Source Project
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

package com.example.android.architecture.blueprints.Fruitapp.addeditfruit

import androidx.compose.material.Surface
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for the Add fruit screen.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
@ExperimentalCoroutinesApi
class AddEditfruitScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()
    private val activity get() = composeTestRule.activity

    @Inject
    lateinit var repository: fruitRepository

    @Before
    fun setup() {
        hiltRule.inject()

        // GIVEN - On the "Add fruit" screen.
        composeTestRule.setContent {
            AppCompatTheme {
                Surface {
                    AddEditfruitScreen(
                        viewModel = AddEditfruitViewModel(repository, SavedStateHandle()),
                        topBarTitle = R.string.add_fruit,
                        onfruitUpdate = { },
                        onBack = { },
                    )
                }
            }
        }
    }

    @Test
    fun emptyfruit_isNotSaved() {
        // WHEN - Enter invalid title and description combination and click save
        findTextField(R.string.title_hint).performTextClearance()
        findTextField(R.string.description_hint).performTextClearance()
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.cd_save_fruit))
            .performClick()

        // THEN - Entered fruit is still displayed (a correct fruit would close it).
        composeTestRule
            .onNodeWithText(activity.getString(R.string.empty_fruit_message))
            .assertIsDisplayed()
    }

    @Test
    fun validfruit_isSaved() = runTest {
        // WHEN - Valid title and description combination and click save
        findTextField(R.string.title_hint).performTextInput("title")
        findTextField(R.string.description_hint).performTextInput("description")
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.cd_save_fruit))
            .performClick()

        // THEN - Verify that the repository saved the fruit
        val fruits = repository.getfruits(true)
        assertEquals(1, fruits.size)
        assertEquals("title", fruits[0].title)
        assertEquals("description", fruits[0].description)
    }

    private fun findTextField(text: Int): SemanticsNodeInteraction {
        return composeTestRule.onNode(
            hasSetTextAction() and hasText(activity.getString(text))
        )
    }
}

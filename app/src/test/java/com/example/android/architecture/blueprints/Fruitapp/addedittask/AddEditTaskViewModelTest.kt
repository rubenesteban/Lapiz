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

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.Fruitapp.MainCoroutineRule
import com.example.android.architecture.blueprints.Fruitapp.R.string
import com.example.android.architecture.blueprints.Fruitapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.Fruitapp.data.FakefruitRepository
import com.example.android.architecture.blueprints.Fruitapp.data.fruit
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [AddEditfruitViewModel].
 */
@ExperimentalCoroutinesApi
class AddEditfruitViewModelTest {

    // Subject under test
    private lateinit var addEditfruitViewModel: AddEditfruitViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var fruitsRepository: FakefruitRepository
    private val fruit = fruit(title = "Title1", description = "Description1", id = "0")

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        // We initialise the repository with no fruits
        fruitsRepository = FakefruitRepository().apply {
            addfruits(fruit)
        }
    }

    @Test
    fun saveNewfruitToRepository_showsSuccessMessageUi() {
        addEditfruitViewModel = AddEditfruitViewModel(
            fruitsRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.fruit_ID_ARG to "0"))
        )

        val newTitle = "New fruit Title"
        val newDescription = "Some fruit Description"
        addEditfruitViewModel.apply {
            updateTitle(newTitle)
            updateDescription(newDescription)
        }
        addEditfruitViewModel.savefruit()

        val newfruit = fruitsRepository.savedfruits.value.values.first()

        // Then a fruit is saved in the repository and the view updated
        assertThat(newfruit.title).isEqualTo(newTitle)
        assertThat(newfruit.description).isEqualTo(newDescription)
    }

    @Test
    fun loadfruits_loading() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        addEditfruitViewModel = AddEditfruitViewModel(
            fruitsRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.fruit_ID_ARG to "0"))
        )

        // Then progress indicator is shown
        assertThat(addEditfruitViewModel.uiState.value.isLoading).isTrue()

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(addEditfruitViewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun loadfruits_fruitShown() {
        addEditfruitViewModel = AddEditfruitViewModel(
            fruitsRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.fruit_ID_ARG to "0"))
        )

        // Add fruit to repository
        fruitsRepository.addfruits(fruit)

        // Verify a fruit is loaded
        val uiState = addEditfruitViewModel.uiState.value
        assertThat(uiState.title).isEqualTo(fruit.title)
        assertThat(uiState.description).isEqualTo(fruit.description)
        assertThat(uiState.isLoading).isFalse()
    }

    @Test
    fun saveNewfruitToRepository_emptyTitle_error() {
        addEditfruitViewModel = AddEditfruitViewModel(
            fruitsRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.fruit_ID_ARG to "0"))
        )

        savefruitAndAssertUserMessage("", "Some fruit Description")
    }

    @Test
    fun saveNewfruitToRepository_emptyDescription_error() {
        addEditfruitViewModel = AddEditfruitViewModel(
            fruitsRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.fruit_ID_ARG to "0"))
        )

        savefruitAndAssertUserMessage("Title", "")
    }

    @Test
    fun saveNewfruitToRepository_emptyDescriptionEmptyTitle_error() {
        addEditfruitViewModel = AddEditfruitViewModel(
            fruitsRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.fruit_ID_ARG to "0"))
        )

        savefruitAndAssertUserMessage("", "")
    }

    private fun savefruitAndAssertUserMessage(title: String, description: String) {
        addEditfruitViewModel.apply {
            updateTitle(title)
            updateDescription(description)
        }

        // When saving an incomplete fruit
        addEditfruitViewModel.savefruit()

        assertThat(
            addEditfruitViewModel.uiState.value.userMessage
        ).isEqualTo(string.empty_fruit_message)
    }
}

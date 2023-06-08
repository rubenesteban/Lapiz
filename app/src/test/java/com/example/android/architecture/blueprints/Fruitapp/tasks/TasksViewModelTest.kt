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

package com.example.android.architecture.blueprints.Fruitapp.fruits

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.Fruitapp.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.Fruitapp.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.Fruitapp.EDIT_RESULT_OK
import com.example.android.architecture.blueprints.Fruitapp.MainCoroutineRule
import com.example.android.architecture.blueprints.Fruitapp.R
import com.example.android.architecture.blueprints.Fruitapp.data.FakefruitRepository
import com.example.android.architecture.blueprints.Fruitapp.data.fruit
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [fruitsViewModel]
 */
@ExperimentalCoroutinesApi
class fruitsViewModelTest {

    // Subject under test
    private lateinit var fruitsViewModel: fruitsViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var fruitsRepository: FakefruitRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        // We initialise the fruits to 3, with one active and two completed
        fruitsRepository = FakefruitRepository()
        val fruit1 = fruit(id = "1", title = "Title1", description = "Desc1")
        val fruit2 = fruit(id = "2", title = "Title2", description = "Desc2", isCompleted = true)
        val fruit3 = fruit(id = "3", title = "Title3", description = "Desc3", isCompleted = true)
        fruitsRepository.addfruits(fruit1, fruit2, fruit3)

        fruitsViewModel = fruitsViewModel(fruitsRepository, SavedStateHandle())
    }

    @Test
    fun loadAllfruitsFromRepository_loadingTogglesAndDataLoaded() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        // Given an initialized fruitsViewModel with initialized fruits
        // When loading of fruits is requested
        fruitsViewModel.setFiltering(fruitsFilterType.ALL_fruitS)

        // Trigger loading of fruits
        fruitsViewModel.refresh()

        // Then progress indicator is shown
        assertThat(fruitsViewModel.uiState.first().isLoading).isTrue()

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(fruitsViewModel.uiState.first().isLoading).isFalse()

        // And data correctly loaded
        assertThat(fruitsViewModel.uiState.first().items).hasSize(3)
    }

    @Test
    fun loadActivefruitsFromRepositoryAndLoadIntoView() = runTest {
        // Given an initialized fruitsViewModel with initialized fruits
        // When loading of fruits is requested
        fruitsViewModel.setFiltering(fruitsFilterType.ACTIVE_fruitS)

        // Load fruits
        fruitsViewModel.refresh()

        // Then progress indicator is hidden
        assertThat(fruitsViewModel.uiState.first().isLoading).isFalse()

        // And data correctly loaded
        assertThat(fruitsViewModel.uiState.first().items).hasSize(1)
    }

    @Test
    fun loadCompletedfruitsFromRepositoryAndLoadIntoView() = runTest {
        // Given an initialized fruitsViewModel with initialized fruits
        // When loading of fruits is requested
        fruitsViewModel.setFiltering(fruitsFilterType.COMPLETED_fruitS)

        // Load fruits
        fruitsViewModel.refresh()

        // Then progress indicator is hidden
        assertThat(fruitsViewModel.uiState.first().isLoading).isFalse()

        // And data correctly loaded
        assertThat(fruitsViewModel.uiState.first().items).hasSize(2)
    }

    @Test
    fun loadfruits_error() = runTest {
        // Make the repository throw errors
        fruitsRepository.setShouldThrowError(true)

        // Load fruits
        fruitsViewModel.refresh()

        // Then progress indicator is hidden
        assertThat(fruitsViewModel.uiState.first().isLoading).isFalse()

        // And the list of items is empty
        assertThat(fruitsViewModel.uiState.first().items).isEmpty()
        assertThat(fruitsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.loading_fruits_error)
    }

    @Test
    fun clearCompletedfruits_clearsfruits() = runTest {
        // When completed fruits are cleared
        fruitsViewModel.clearCompletedfruits()

        // Fetch fruits
        fruitsViewModel.refresh()

        // Fetch fruits
        val allfruits = fruitsViewModel.uiState.first().items
        val completedfruits = allfruits?.filter { it.isCompleted }

        // Verify there are no completed fruits left
        assertThat(completedfruits).isEmpty()

        // Verify active fruit is not cleared
        assertThat(allfruits).hasSize(1)

        // Verify snackbar is updated
        assertThat(fruitsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.completed_fruits_cleared)
    }

    @Test
    fun showEditResultMessages_editOk_snackbarUpdated() = runTest {
        // When the viewmodel receives a result from another destination
        fruitsViewModel.showEditResultMessage(EDIT_RESULT_OK)

        // The snackbar is updated
        assertThat(fruitsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.successfully_saved_fruit_message)
    }

    @Test
    fun showEditResultMessages_addOk_snackbarUpdated() = runTest {
        // When the viewmodel receives a result from another destination
        fruitsViewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)

        // The snackbar is updated
        assertThat(fruitsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.successfully_added_fruit_message)
    }

    @Test
    fun showEditResultMessages_deleteOk_snackbarUpdated() = runTest {
        // When the viewmodel receives a result from another destination
        fruitsViewModel.showEditResultMessage(DELETE_RESULT_OK)

        // The snackbar is updated
        assertThat(fruitsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.successfully_deleted_fruit_message)
    }

    @Test
    fun completefruit_dataAndSnackbarUpdated() = runTest {
        // With a repository that has an active fruit
        val fruit = fruit(id = "id", title = "Title", description = "Description")
        fruitsRepository.addfruits(fruit)

        // Complete fruit
        fruitsViewModel.completefruit(fruit, true)

        // Verify the fruit is completed
        assertThat(fruitsRepository.savedfruits.value[fruit.id]?.isCompleted).isTrue()

        // The snackbar is updated
        assertThat(fruitsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.fruit_marked_complete)
    }

    @Test
    fun activatefruit_dataAndSnackbarUpdated() = runTest {
        // With a repository that has a completed fruit
        val fruit = fruit(id = "id", title = "Title", description = "Description", isCompleted = true)
        fruitsRepository.addfruits(fruit)

        // Activate fruit
        fruitsViewModel.completefruit(fruit, false)

        // Verify the fruit is active
        assertThat(fruitsRepository.savedfruits.value[fruit.id]?.isActive).isTrue()

        // The snackbar is updated
        assertThat(fruitsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.fruit_marked_active)
    }
}

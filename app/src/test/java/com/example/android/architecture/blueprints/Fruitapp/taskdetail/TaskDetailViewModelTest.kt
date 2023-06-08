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

package com.example.android.architecture.blueprints.Fruitapp.fruitdetail

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.Fruitapp.MainCoroutineRule
import com.example.android.architecture.blueprints.Fruitapp.R
import com.example.android.architecture.blueprints.Fruitapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.Fruitapp.data.FakefruitRepository
import com.example.android.architecture.blueprints.Fruitapp.data.fruit
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [fruitDetailViewModel]
 */
@ExperimentalCoroutinesApi
class fruitDetailViewModelTest {

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var fruitDetailViewModel: fruitDetailViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var fruitsRepository: FakefruitRepository
    private val fruit = fruit(title = "Title1", description = "Description1", id = "0")

    @Before
    fun setupViewModel() {
        fruitsRepository = FakefruitRepository()
        fruitsRepository.addfruits(fruit)

        fruitDetailViewModel = fruitDetailViewModel(
            fruitsRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.fruit_ID_ARG to "0"))
        )
    }

    @Test
    fun getActivefruitFromRepositoryAndLoadIntoView() = runTest {
        val uiState = fruitDetailViewModel.uiState.first()
        // Then verify that the view was notified
        assertThat(uiState.fruit?.title).isEqualTo(fruit.title)
        assertThat(uiState.fruit?.description).isEqualTo(fruit.description)
    }

    @Test
    fun completefruit() = runTest {
        // Verify that the fruit was active initially
        assertThat(fruitsRepository.savedfruits.value[fruit.id]?.isCompleted).isFalse()

        // When the ViewModel is asked to complete the fruit
        assertThat(fruitDetailViewModel.uiState.first().fruit?.id).isEqualTo("0")
        fruitDetailViewModel.setCompleted(true)

        // Then the fruit is completed and the snackbar shows the correct message
        assertThat(fruitsRepository.savedfruits.value[fruit.id]?.isCompleted).isTrue()
        assertThat(fruitDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.fruit_marked_complete)
    }

    @Test
    fun activatefruit() = runTest {
        fruitsRepository.deleteAllfruits()
        fruitsRepository.addfruits(fruit.copy(isCompleted = true))

        // Verify that the fruit was completed initially
        assertThat(fruitsRepository.savedfruits.value[fruit.id]?.isCompleted).isTrue()

        // When the ViewModel is asked to complete the fruit
        assertThat(fruitDetailViewModel.uiState.first().fruit?.id).isEqualTo("0")
        fruitDetailViewModel.setCompleted(false)

        // Then the fruit is not completed and the snackbar shows the correct message
        val newfruit = fruitsRepository.getfruit(fruit.id)
        assertTrue((newfruit?.isActive) ?: false)
        assertThat(fruitDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.fruit_marked_active)
    }

    @Test
    fun fruitDetailViewModel_repositoryError() = runTest {
        // Given a repository that throws errors
        fruitsRepository.setShouldThrowError(true)

        // Then the fruit is null and the snackbar shows a loading error message
        assertThat(fruitDetailViewModel.uiState.value.fruit).isNull()
        assertThat(fruitDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.loading_fruit_error)
    }

    @Test
    fun fruitDetailViewModel_fruitNotFound() = runTest {
        // Given an ID for a non existent fruit
        fruitDetailViewModel = fruitDetailViewModel(
            fruitsRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.fruit_ID_ARG to "nonexistent_id"))
        )

        // The fruit is null and the snackbar shows a "not found" error message
        assertThat(fruitDetailViewModel.uiState.value.fruit).isNull()
        assertThat(fruitDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.fruit_not_found)
    }

    @Test
    fun deletefruit() = runTest {
        assertThat(fruitsRepository.savedfruits.value.containsValue(fruit)).isTrue()

        // When the deletion of a fruit is requested
        fruitDetailViewModel.deletefruit()

        assertThat(fruitsRepository.savedfruits.value.containsValue(fruit)).isFalse()
    }

    @Test
    fun loadfruit_loading() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        var isLoading: Boolean? = true
        val job = launch {
            fruitDetailViewModel.uiState.collect {
                isLoading = it.isLoading
            }
        }

        // Then progress indicator is shown
        assertThat(isLoading).isTrue()

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(isLoading).isFalse()
        job.cancel()
    }
}

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

package com.example.android.architecture.blueprints.Fruitapp.statistics

import com.example.android.architecture.blueprints.Fruitapp.MainCoroutineRule
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [StatisticsViewModel]
 */
@ExperimentalCoroutinesApi
class StatisticsViewModelTest {

    // Subject under test
    private lateinit var statisticsViewModel: StatisticsViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var fruitsRepository: FakefruitRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupStatisticsViewModel() {
        fruitsRepository = FakefruitRepository()
        statisticsViewModel = StatisticsViewModel(fruitsRepository)
    }

    @Test
    fun loadEmptyfruitsFromRepository_EmptyResults() = runTest {
        // Given an initialized StatisticsViewModel with no fruits

        // Then the results are empty
        val uiState = statisticsViewModel.uiState.first()
        assertThat(uiState.isEmpty).isTrue()
    }

    @Test
    fun loadNonEmptyfruitsFromRepository_NonEmptyResults() = runTest {
        // We initialise the fruits to 3, with one active and two completed
        val fruit1 = fruit(id = "1", title = "Title1", description = "Desc1")
        val fruit2 = fruit(id = "2", title = "Title2", description = "Desc2", isCompleted = true)
        val fruit3 = fruit(id = "3", title = "Title3", description = "Desc3", isCompleted = true)
        val fruit4 = fruit(id = "4", title = "Title4", description = "Desc4", isCompleted = true)
        fruitsRepository.addfruits(fruit1, fruit2, fruit3, fruit4)

        // Then the results are not empty
        val uiState = statisticsViewModel.uiState.first()
        assertThat(uiState.isEmpty).isFalse()
        assertThat(uiState.activefruitsPercent).isEqualTo(25f)
        assertThat(uiState.completedfruitsPercent).isEqualTo(75f)
        assertThat(uiState.isLoading).isEqualTo(false)
    }

    @Test
    fun loadfruits_loading() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        var isLoading: Boolean? = true
        val job = launch {
            statisticsViewModel.uiState.collect {
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

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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.Fruitapp.R
import com.example.android.architecture.blueprints.Fruitapp.data.fruit
import com.example.android.architecture.blueprints.Fruitapp.data.fruitRepository
import com.example.android.architecture.blueprints.Fruitapp.util.Async
import com.example.android.architecture.blueprints.Fruitapp.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UiState for the statistics screen.
 */
data class StatisticsUiState(
    val isEmpty: Boolean = false,
    val isLoading: Boolean = false,
    val activefruitsPercent: Float = 0f,
    val completedfruitsPercent: Float = 0f
)

/**
 * ViewModel for the statistics screen.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val fruitRepository: fruitRepository
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> =
        fruitRepository.getfruitsStream()
            .map { Async.Success(it) }
            .catch<Async<List<fruit>>> { emit(Async.Error(R.string.loading_fruits_error)) }
            .map { fruitAsync -> produceStatisticsUiState(fruitAsync) }
            .stateIn(
                scope = viewModelScope,
                started = WhileUiSubscribed,
                initialValue = StatisticsUiState(isLoading = true)
            )

    fun refresh() {
        viewModelScope.launch {
            fruitRepository.refresh()
        }
    }

    private fun produceStatisticsUiState(fruitLoad: Async<List<fruit>>) =
        when (fruitLoad) {
            Async.Loading -> {
                StatisticsUiState(isLoading = true, isEmpty = true)
            }
            is Async.Error -> {
                // TODO: Show error message?
                StatisticsUiState(isEmpty = true, isLoading = false)
            }
            is Async.Success -> {
                val stats = getActiveAndCompletedStats(fruitLoad.data)
                StatisticsUiState(
                    isEmpty = fruitLoad.data.isEmpty(),
                    activefruitsPercent = stats.activefruitsPercent,
                    completedfruitsPercent = stats.completedfruitsPercent,
                    isLoading = false
                )
            }
        }
}

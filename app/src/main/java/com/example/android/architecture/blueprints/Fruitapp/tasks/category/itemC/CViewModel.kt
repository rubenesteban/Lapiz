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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.Fruitapp.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.Fruitapp.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.Fruitapp.EDIT_RESULT_OK
import com.example.android.architecture.blueprints.Fruitapp.R
import com.example.android.architecture.blueprints.Fruitapp.data.fruit
import com.example.android.architecture.blueprints.Fruitapp.data.fruitRepository
import com.example.android.architecture.blueprints.Fruitapp.fruits.fruitsFilterType.ACTIVE_fruitS
import com.example.android.architecture.blueprints.Fruitapp.fruits.fruitsFilterType.ALL_fruitS
import com.example.android.architecture.blueprints.Fruitapp.fruits.fruitsFilterType.COMPLETED_fruitS
import com.example.android.architecture.blueprints.Fruitapp.util.Async
import com.example.android.architecture.blueprints.Fruitapp.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UiState for the fruit list screen.
 */
data class aUiState(
    val items: List<fruit> = emptyList(),
    val isLoading: Boolean = false,
    val filteringUoInfo: FilteringUaInfo = FilteringUaInfo(),
    val userMessage: Int? = null
)

/**
 * ViewModel for the fruit list screen.
 */
@HiltViewModel
class aViewModel @Inject constructor(
    private val fruitRepository: fruitRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _savedFilterType =
        savedStateHandle.getStateFlow(fruitS_FILTER_SAVED_STATE_A, ALL_fruitS)

    private val _filterUaInfo = _savedFilterType.map { getFilterUoInfo(it) }.distinctUntilChanged()
    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)
    private val _filteredfruitsAsync =
        combine(fruitRepository.getfruitsA(), _savedFilterType) { fruits, type ->
            filterfruits(fruits, type)
        }
            .map { Async.Success(it) }
            .catch<Async<List<fruit>>> { emit(Async.Error(R.string.loading_fruits_error)) }

    val uiState: StateFlow<aUiState> = combine(
        _filterUaInfo, _isLoading, _userMessage, _filteredfruitsAsync
    ) { filterUaInfo, isLoading, userMessage, fruitsAsync ->
        when (fruitsAsync) {
            Async.Loading -> {
                aUiState(isLoading = true)
            }
            is Async.Error -> {
                aUiState(userMessage = fruitsAsync.errorMessage)
            }
            is Async.Success -> {
                aUiState(
                    items = fruitsAsync.data,
                    filteringUoInfo = filterUaInfo,
                    isLoading = isLoading,
                    userMessage = userMessage
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = aUiState(isLoading = true)
        )

    fun setFiltering(requestType: fruitsFilterType) {
        savedStateHandle[fruitS_FILTER_SAVED_STATE_A] = requestType
    }

    fun clearCompletedfruits() {
        viewModelScope.launch {
            fruitRepository.clearCompletedfruits()
            showSnackbarMessage(R.string.completed_fruits_cleared)
            refresh()
        }
    }

    fun completefruit(fruit: fruit, completed: Boolean) = viewModelScope.launch {
        if (completed) {
            fruitRepository.completefruit(fruit.id)
            showSnackbarMessage(R.string.fruit_marked_complete)
        } else {
            fruitRepository.activatefruit(fruit.id)
            showSnackbarMessage(R.string.fruit_marked_active)
        }
    }

    fun showEditResultMessage(result: Int) {
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_fruit_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_fruit_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_fruit_message)
        }
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    private fun showSnackbarMessage(message: Int) {
        _userMessage.value = message
    }

    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            fruitRepository.refresh()
            _isLoading.value = false
        }
    }

    private fun filterfruits(fruits: List<fruit>, filteringType: fruitsFilterType): List<fruit> {
        val fruitsToShow = ArrayList<fruit>()
        // We filter the fruits based on the requestType
        for (fruit in fruits) {
            when (filteringType) {
                ALL_fruitS -> fruitsToShow.add(fruit)
                ACTIVE_fruitS -> if (fruit.isActive) {
                    fruitsToShow.add(fruit)
                }
                COMPLETED_fruitS -> if (fruit.isCompleted) {
                    fruitsToShow.add(fruit)
                }
            }
        }
        return fruitsToShow
    }

    private fun getFilterUoInfo(requestType: fruitsFilterType): FilteringUaInfo =
        when (requestType) {
            ALL_fruitS -> {
                FilteringUaInfo(
                    R.string.label_all, R.string.no_fruits_all,
                    R.drawable.logo_no_fill
                )
            }
            ACTIVE_fruitS -> {
                FilteringUaInfo(
                    R.string.label_active, R.string.no_fruits_active,
                    R.drawable.ic_check_circle_96dp
                )
            }
            COMPLETED_fruitS -> {
                FilteringUaInfo(
                    R.string.label_completed, R.string.no_fruits_completed,
                    R.drawable.ic_verified_user_96dp
                )
            }
        }
}

// Used to save the current filtering in SavedStateHandle.
const val fruitS_FILTER_SAVED_STATE_A = "fruitS_FILTER_SAVED_STATE_KEY"

data class FilteringUaInfo(
    val currentFilteringLabel: Int = R.string.label_all,
    val nofruitsLabel: Int = R.string.no_fruits_all,
    val nofruitIconRes: Int = R.drawable.logo_no_fill,
)

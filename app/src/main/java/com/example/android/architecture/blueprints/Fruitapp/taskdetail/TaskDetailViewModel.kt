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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.Fruitapp.R
import com.example.android.architecture.blueprints.Fruitapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.Fruitapp.data.fruit
import com.example.android.architecture.blueprints.Fruitapp.data.fruitRepository
import com.example.android.architecture.blueprints.Fruitapp.util.Async
import com.example.android.architecture.blueprints.Fruitapp.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UiState for the Details screen.
 */
data class fruitDetailUiState(
    val fruit: fruit? = null,
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
    val isfruitDeleted: Boolean = false
)

/**
 * ViewModel for the Details screen.
 */
@HiltViewModel
class fruitDetailViewModel @Inject constructor(
    private val fruitRepository: fruitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val fruitId: String = savedStateHandle[TodoDestinationsArgs.fruit_ID_ARG]!!

    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)
    private val _isfruitDeleted = MutableStateFlow(false)
    private val _fruitAsync = fruitRepository.getfruitStream(fruitId)
        .map { handlefruit(it) }
        .catch { emit(Async.Error(R.string.loading_fruit_error)) }

    val uiState: StateFlow<fruitDetailUiState> = combine(
        _userMessage, _isLoading, _isfruitDeleted, _fruitAsync
    ) { userMessage, isLoading, isfruitDeleted, fruitAsync ->
        when (fruitAsync) {
            Async.Loading -> {
                fruitDetailUiState(isLoading = true)
            }
            is Async.Error -> {
                fruitDetailUiState(
                    userMessage = fruitAsync.errorMessage,
                    isfruitDeleted = isfruitDeleted
                )
            }
            is Async.Success -> {
                fruitDetailUiState(
                    fruit = fruitAsync.data,
                    isLoading = isLoading,
                    userMessage = userMessage,
                    isfruitDeleted = isfruitDeleted
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = fruitDetailUiState(isLoading = true)
        )

    fun deletefruit() = viewModelScope.launch {
        fruitRepository.deletefruit(fruitId)
        _isfruitDeleted.value = true
    }

    fun setCompleted(completed: Boolean) = viewModelScope.launch {
        val fruit = uiState.value.fruit ?: return@launch
        if (completed) {
            fruitRepository.completefruit(fruit.id)
            showSnackbarMessage(R.string.fruit_marked_complete)
        } else {
            fruitRepository.activatefruit(fruit.id)
            showSnackbarMessage(R.string.fruit_marked_active)
        }
    }

    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            fruitRepository.refreshfruit(fruitId)
            _isLoading.value = false
        }
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    private fun showSnackbarMessage(message: Int) {
        _userMessage.value = message
    }

    private fun handlefruit(fruit: fruit?): Async<fruit?> {
        if (fruit == null) {
            return Async.Error(R.string.fruit_not_found)
        }
        return Async.Success(fruit)
    }
}

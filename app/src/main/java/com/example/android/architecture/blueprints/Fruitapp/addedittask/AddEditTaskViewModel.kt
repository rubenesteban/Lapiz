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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.Fruitapp.R
import com.example.android.architecture.blueprints.Fruitapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.Fruitapp.data.fruitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UiState for the Add/Edit screen
 */
data class AddEditfruitUiState(
    val title: String = "",
    val description: String = "",
    val categoria: String = "",
    val isfruitCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
    val isfruitSaved: Boolean = false
)

/**
 * ViewModel for the Add/Edit screen.
 */
@HiltViewModel
class AddEditfruitViewModel @Inject constructor(
    private val fruitRepository: fruitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val fruitId: String? = savedStateHandle[TodoDestinationsArgs.fruit_ID_ARG]

    // A MutableStateFlow needs to be created in this ViewModel. The source of truth of the current
    // editable fruit is the ViewModel, we need to mutate the UI state directly in methods such as
    // `updateTitle` or `updateDescription`
    private val _uiState = MutableStateFlow(AddEditfruitUiState())
    val uiState: StateFlow<AddEditfruitUiState> = _uiState.asStateFlow()

    init {
        if (fruitId != null) {
            loadfruit(fruitId)
        }
    }

    // Called when clicking on fab.
    fun savefruit() {
        if (uiState.value.title.isEmpty() || uiState.value.categoria.isEmpty() || uiState.value.description.isEmpty()) {
            _uiState.update {
                it.copy(userMessage = R.string.empty_fruit_message)
            }
            return

        }

        if (fruitId == null) {
            createNewfruit()
        } else {
            updatefruit()
        }
    }

    fun snackbarMessageShown() {
        _uiState.update {
            it.copy(userMessage = null)
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update {
            it.copy(title = newTitle)
        }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update {
            it.copy(description = newDescription)
        }
    }


    fun updateCategoria(newDescription: String) {
        _uiState.update {
            it.copy(categoria = newDescription)
        }
    }

    private fun createNewfruit() = viewModelScope.launch {
        fruitRepository.createfruit(uiState.value.title, uiState.value.categoria, uiState.value.description)
        _uiState.update {
            it.copy(isfruitSaved = true)
        }
    }

    private fun updatefruit() {
        if (fruitId == null) {
            throw RuntimeException("updatefruit() was called but fruit is new.")
        }
        viewModelScope.launch {
            fruitRepository.updatefruit(
                fruitId,
                title = uiState.value.title,
                categoria = uiState.value.categoria,
                description = uiState.value.description,

            )
            _uiState.update {
                it.copy(isfruitSaved = true)
            }
        }
    }

    private fun loadfruit(fruitId: String) {
        _uiState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            fruitRepository.getfruit(fruitId).let { fruit ->
                if (fruit != null) {
                    _uiState.update {
                        it.copy(
                            title = fruit.title,
                            categoria = fruit.categoria,
                            description = fruit.description,
                            isfruitCompleted = fruit.isCompleted,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                }
            }
        }
    }
}

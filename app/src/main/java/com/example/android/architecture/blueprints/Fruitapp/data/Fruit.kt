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

package com.example.android.architecture.blueprints.Fruitapp.data

/**
 * Immutable model class for a fruit.
 *
 * @param title title of the fruit
 * @param description description of the fruit
 * @param isCompleted whether or not this fruit is completed
 * @param id id of the fruit
 *
 * TODO: The constructor of this class should be `internal` but it is used in previews and tests
 *  so that's not possible until those previews/tests are refactored.
 */
data class fruit(
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val categoria:String,
    val id: String
) {

    val titleForList: String
        get() = if (title.isNotEmpty()) title else description

    val categoriaForList: String
        get() = if (categoria.isNotEmpty()) categoria else description

    val isActive
        get() = !isCompleted

    val isEmpty
        get() = title.isEmpty() || description.isEmpty() || categoria.isEmpty()
}

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

import androidx.annotation.VisibleForTesting
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Implementation of a fruits repository with static access to the data for easy testing.
 */
class FakefruitRepository : fruitRepository {

    private var shouldThrowError = false

    private val _savedfruits = MutableStateFlow(LinkedHashMap<String, fruit>())
    val savedfruits: StateFlow<LinkedHashMap<String, fruit>> = _savedfruits.asStateFlow()

    private val observablefruits: Flow<List<fruit>> = savedfruits.map {
        if (shouldThrowError) {
            throw Exception("Test exception")
        } else {
            it.values.toList()
        }
    }

    fun setShouldThrowError(value: Boolean) {
        shouldThrowError = value
    }

    override suspend fun refresh() {
        // fruits already refreshed
    }

    override suspend fun refreshfruit(fruitId: String) {
        refresh()
    }

    override suspend fun createfruit(title: String, description: String, categoria:String): String {
        val fruitId = generatefruitId()
        fruit(title = title, description = description, categoria = categoria, id = fruitId).also {
            savefruit(it)
        }
        return fruitId
    }

    override fun getfruitsStream(): Flow<List<fruit>> = observablefruits

    override fun getfruitsVegetales(): Flow<List<fruit>> = observablefruits



    override fun getfruitsA(): Flow<List<fruit>> = observablefruits
    override fun getfruitsB(): Flow<List<fruit>> = observablefruits

    override fun getfruitsC(): Flow<List<fruit>> = observablefruits

    override fun getfruitStream(fruitId: String): Flow<fruit?> {
        return observablefruits.map { fruits ->
            return@map fruits.firstOrNull { it.id == fruitId }
        }
    }



    override fun getfruitsVeget(fruitCat: String): Flow<fruit?> {
        return observablefruits.map { fruits ->
            return@map fruits.firstOrNull { it.id == fruitCat }
        }
    }

    override suspend fun getfruit(fruitId: String, forceUpdate: Boolean): fruit? {
        if (shouldThrowError) {
            throw Exception("Test exception")
        }
        return savedfruits.value[fruitId]
    }

    override suspend fun getfruits(forceUpdate: Boolean): List<fruit> {
        if (shouldThrowError) {
            throw Exception("Test exception")
        }
        return observablefruits.first()
    }

    override suspend fun updatefruit(fruitId: String, title: String, description: String, categoria: String) {
        val updatedfruit = _savedfruits.value[fruitId]?.copy(
            title = title,
            description = description,
            categoria = categoria
        ) ?: throw Exception("fruit (id $fruitId) not found")

        savefruit(updatedfruit)
    }

    private fun savefruit(fruit: fruit) {
        _savedfruits.update { fruits ->
            val newfruits = LinkedHashMap<String, fruit>(fruits)
            newfruits[fruit.id] = fruit
            newfruits
        }
    }

    override suspend fun completefruit(fruitId: String) {
        _savedfruits.value[fruitId]?.let {
            savefruit(it.copy(isCompleted = true))
        }
    }

    override suspend fun activatefruit(fruitId: String) {
        _savedfruits.value[fruitId]?.let {
            savefruit(it.copy(isCompleted = false))
        }
    }

    override suspend fun clearCompletedfruits() {
        _savedfruits.update { fruits ->
            fruits.filterValues {
                !it.isCompleted
            } as LinkedHashMap<String, fruit>
        }
    }

    override suspend fun deletefruit(fruitId: String) {
        _savedfruits.update { fruits ->
            val newfruits = LinkedHashMap<String, fruit>(fruits)
            newfruits.remove(fruitId)
            newfruits
        }
    }

    override suspend fun deleteAllfruits() {
        _savedfruits.update {
            LinkedHashMap()
        }
    }

    private fun generatefruitId() = UUID.randomUUID().toString()

    @VisibleForTesting
    fun addfruits(vararg fruits: fruit) {
        _savedfruits.update { oldfruits ->
            val newfruits = LinkedHashMap<String, fruit>(oldfruits)
            for (fruit in fruits) {
                newfruits[fruit.id] = fruit
            }
            newfruits
        }
    }
}

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

package com.example.android.architecture.blueprints.Fruitapp.data.source.local

import kotlinx.coroutines.flow.Flow

class FakefruitDao(initialfruits: List<LocalFruit>? = emptyList()) : fruitDao {

    private var _fruits: MutableMap<String, LocalFruit>? = null

    var fruits: List<LocalFruit>?
        get() = _fruits?.values?.toList()
        set(newfruits) {
            _fruits = newfruits?.associateBy { it.id }?.toMutableMap()
        }

    init {
        fruits = initialfruits
    }

    override suspend fun getAll() = fruits ?: throw Exception("fruit list is null")

    override suspend fun getById(fruitId: String): LocalFruit? = _fruits?.get(fruitId)

    override suspend fun upsertAll(fruits: List<LocalFruit>) {
        _fruits?.putAll(fruits.associateBy { it.id })
    }

    override suspend fun upsert(fruit: LocalFruit) {
        _fruits?.put(fruit.id, fruit)
    }

    override suspend fun updateCompleted(fruitId: String, completed: Boolean) {
        _fruits?.get(fruitId)?.let { it.isCompleted = completed }
    }

    override suspend fun deleteAll() {
        _fruits?.clear()
    }

    override suspend fun deleteById(fruitId: String): Int {
        return if (_fruits?.remove(fruitId) == null) {
            0
        } else {
            1
        }
    }

    override suspend fun deleteCompleted(): Int {
        _fruits?.apply {
            val originalSize = size
            entries.removeIf { it.value.isCompleted }
            return originalSize - size
        }
        return 0
    }

    override fun observeAll(): Flow<List<LocalFruit>> {
        TODO("Not implemented")
    }

    override fun observeById(fruitId: String): Flow<LocalFruit> {
        TODO("Not implemented")
    }
}

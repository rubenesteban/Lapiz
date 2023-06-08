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

package com.example.android.architecture.blueprints.Fruitapp.data.source.network

class FakeNetworkDataSource(
    var fruits: MutableList<Networkfruit>? = mutableListOf()
) : NetworkDataSource {
    override suspend fun loadfruits() = fruits ?: throw Exception("fruit list is null")

    override suspend fun savefruits(fruits: List<Networkfruit>) {
        this.fruits = fruits.toMutableList()
    }
}

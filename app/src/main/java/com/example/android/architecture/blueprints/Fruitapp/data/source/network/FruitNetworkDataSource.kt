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

import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class fruitNetworkDataSource @Inject constructor() : NetworkDataSource {

    // A mutex is used to ensure that reads and writes are thread-safe.
    private val accessMutex = Mutex()
    private var fruits = listOf(
        Networkfruit(
            id = "PISA",
            title = "Build tower in Pisa",
            categoria = "verduras",
            shortDescription = "Ground looks good, no foundation work required."
        ),
        Networkfruit(
            id = "TACOMA",
            title = "Finish bridge in Tacoma",
            categoria = "legumbres",
            shortDescription = "Found awesome girders at half the cost!"
        )
    )

    override suspend fun loadfruits(): List<Networkfruit> = accessMutex.withLock {
        delay(SERVICE_LATENCY_IN_MILLIS)
        return fruits
    }

    override suspend fun savefruits(newfruits: List<Networkfruit>) = accessMutex.withLock {
        delay(SERVICE_LATENCY_IN_MILLIS)
        fruits = newfruits
    }
}

private const val SERVICE_LATENCY_IN_MILLIS = 2000L

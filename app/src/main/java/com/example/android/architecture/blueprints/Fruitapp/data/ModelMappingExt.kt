/*
 * Copyright 2023 The Android Open Source Project
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

import com.example.android.architecture.blueprints.Fruitapp.data.source.local.LocalFruit
import com.example.android.architecture.blueprints.Fruitapp.data.source.network.Networkfruit
import com.example.android.architecture.blueprints.Fruitapp.data.source.network.fruitStatus

/**
 * Data model mapping extension functions. There are three model types:
 *
 * - fruit: External model exposed to other layers in the architecture.
 * Obtained using `toExternal`.
 *
 * - Networkfruit: Internal model used to represent a fruit from the network. Obtained using
 * `toNetwork`.
 *
 * - LocalFruit: Internal model used to represent a fruit stored locally in a database. Obtained
 * using `toLocal`.
 *
 */

// External to local
fun fruit.toLocal() = LocalFruit(
    id = id,
    title = title,
    categoria = categoria,
    description = description,
    isCompleted = isCompleted

)

fun List<fruit>.toLocal() = map(fruit::toLocal)

// Local to External
fun LocalFruit.toExternal() = fruit(
    id = id,
    title = title,
    categoria = categoria,
    description = description,
    isCompleted = isCompleted

)

// Note: JvmName is used to provide a unique name for each extension function with the same name.
// Without this, type erasure will cause compiler errors because these methods will have the same
// signature on the JVM.
@JvmName("localToExternal")
fun List<LocalFruit>.toExternal() = map(LocalFruit::toExternal)

// Network to Local
fun Networkfruit.toLocal() = LocalFruit(
    id = id,
    title = title,
    categoria = categoria,
    description = shortDescription,
    isCompleted = (status == fruitStatus.COMPLETE)

)

@JvmName("networkToLocal")
fun List<Networkfruit>.toLocal() = map(Networkfruit::toLocal)

// Local to Network
fun LocalFruit.toNetwork() = Networkfruit(
    id = id,
    title = title,
    categoria = categoria,
    shortDescription = description,
    status = if (isCompleted) { fruitStatus.COMPLETE } else { fruitStatus.ACTIVE }

)

fun List<LocalFruit>.toNetwork() = map(LocalFruit::toNetwork)

// External to Network
fun fruit.toNetwork() = toLocal().toNetwork()

@JvmName("externalToNetwork")
fun List<fruit>.toNetwork() = map(fruit::toNetwork)

// Network to External
fun Networkfruit.toExternal() = toLocal().toExternal()

@JvmName("networkToExternal")
fun List<Networkfruit>.toExternal() = map(Networkfruit::toExternal)

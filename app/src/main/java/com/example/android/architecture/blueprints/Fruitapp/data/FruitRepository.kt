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

import kotlinx.coroutines.flow.Flow

/**
 * Interface to the data layer.
 */
interface fruitRepository {

    fun getfruitsStream(): Flow<List<fruit>>

    fun getfruitsVegetales(): Flow<List<fruit>>

    fun getfruitsA(): Flow<List<fruit>>

    fun getfruitsB(): Flow<List<fruit>>

    fun getfruitsC(): Flow<List<fruit>>

    suspend fun getfruits(forceUpdate: Boolean = false): List<fruit>

    suspend fun refresh()

    fun getfruitStream(fruitId: String): Flow<fruit?>


    fun getfruitsVeget(fruitCat: String): Flow<fruit?>

    suspend fun getfruit(fruitId: String, forceUpdate: Boolean = false): fruit?

    suspend fun refreshfruit(fruitId: String)

    suspend fun createfruit(title: String, categoria:String ,description: String ): String

    suspend fun updatefruit(fruitId: String, title: String, categoria:String, description: String )

    suspend fun completefruit(fruitId: String)

    suspend fun activatefruit(fruitId: String)

    suspend fun clearCompletedfruits()

    suspend fun deleteAllfruits()

    suspend fun deletefruit(fruitId: String)
}

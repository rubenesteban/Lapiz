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

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the fruit table.
 */
@Dao
interface fruitDao {

    /**
     * Observes list of fruits.
     *
     * @return all fruits.
     */
    @Query("SELECT * FROM fruit")
    fun observeAll(): Flow<List<LocalFruit>>

    @Query("SELECT * FROM fruit WHERE categoria ='vegetales'")
    fun observeAllVeg(): Flow<List<LocalFruit>>

    @Query("SELECT * FROM fruit WHERE categoria ='A'")
    fun observeAllA(): Flow<List<LocalFruit>>

    @Query("SELECT * FROM fruit WHERE categoria ='B'")
    fun observeAllB(): Flow<List<LocalFruit>>

    @Query("SELECT * FROM fruit WHERE categoria ='C'")
    fun observeAllC(): Flow<List<LocalFruit>>

    /**
     * Observes a single fruit.
     *
     * @param fruitId the fruit id.
     * @return the fruit with fruitId.
     */
    @Query("SELECT * FROM fruit WHERE id = :fruitId")
    fun observeById(fruitId: String): Flow<LocalFruit>



    @Query("SELECT * FROM fruit WHERE categoria = :fruitCat")
    fun getAllVeget(fruitCat: String): Flow<LocalFruit>


    /**
     * Select all fruits from the fruits table.
     *
     * @return all fruits.
     */
    @Query("SELECT * FROM fruit")
    suspend fun getAll(): List<LocalFruit>


    /**
     * Select a fruit by id.
     *
     * @param fruitId the fruit id.
     * @return the fruit with fruitId.
     */
    @Query("SELECT * FROM fruit WHERE id = :fruitId")
    suspend fun getById(fruitId: String): LocalFruit?

    /**
     * Insert or update a fruit in the database. If a fruit already exists, replace it.
     *
     * @param fruit the fruit to be inserted or updated.
     */
    @Upsert
    suspend fun upsert(fruit: LocalFruit)

    /**
     * Insert or update fruits in the database. If a fruit already exists, replace it.
     *
     * @param fruits the fruits to be inserted or updated.
     */
    @Upsert
    suspend fun upsertAll(fruits: List<LocalFruit>)

    /**
     * Update the complete status of a fruit
     *
     * @param fruitId id of the fruit
     * @param completed status to be updated
     */
    @Query("UPDATE fruit SET isCompleted = :completed WHERE id = :fruitId")
    suspend fun updateCompleted(fruitId: String, completed: Boolean)

    /**
     * Delete a fruit by id.
     *
     * @return the number of fruits deleted. This should always be 1.
     */
    @Query("DELETE FROM fruit WHERE id = :fruitId")
    suspend fun deleteById(fruitId: String): Int

    /**
     * Delete all fruits.
     */
    @Query("DELETE FROM fruit")
    suspend fun deleteAll()

    /**
     * Delete all completed fruits from the table.
     *
     * @return the number of fruits deleted.
     */
    @Query("DELETE FROM fruit WHERE isCompleted = 1")
    suspend fun deleteCompleted(): Int
}

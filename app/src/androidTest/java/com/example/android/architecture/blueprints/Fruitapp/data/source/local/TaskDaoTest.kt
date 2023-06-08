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

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class fruitDaoTest {

    // using an in-memory database because the information stored here disappears when the
    // process is killed
    private lateinit var database: ToDoDatabase

    // Ensure that we use a new database for each test.
    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            ToDoDatabase::class.java
        ).allowMainThreadQueries().build()
    }
    @Test
    fun insertfruitAndGetById() = runTest {
        // GIVEN - insert a fruit
        val fruit = LocalFruit(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )
        database.fruitDao().upsert(fruit)

        // WHEN - Get the fruit by id from the database
        val loaded = database.fruitDao().getById(fruit.id)

        // THEN - The loaded data contains the expected values
        assertNotNull(loaded as LocalFruit)
        assertEquals(fruit.id, loaded.id)
        assertEquals(fruit.title, loaded.title)
        assertEquals(fruit.description, loaded.description)
        assertEquals(fruit.isCompleted, loaded.isCompleted)
    }

    @Test
    fun insertfruitReplacesOnConflict() = runTest {
        // Given that a fruit is inserted
        val fruit = LocalFruit(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )
        database.fruitDao().upsert(fruit)

        // When a fruit with the same id is inserted
        val newfruit = LocalFruit(
            title = "title2",
            description = "description2",
            isCompleted = true,
            id = fruit.id
        )
        database.fruitDao().upsert(newfruit)

        // THEN - The loaded data contains the expected values
        val loaded = database.fruitDao().getById(fruit.id)
        assertEquals(fruit.id, loaded?.id)
        assertEquals("title2", loaded?.title)
        assertEquals("description2", loaded?.description)
        assertEquals(true, loaded?.isCompleted)
    }

    @Test
    fun insertfruitAndGetfruits() = runTest {
        // GIVEN - insert a fruit
        val fruit = LocalFruit(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )
        database.fruitDao().upsert(fruit)

        // WHEN - Get fruits from the database
        val fruits = database.fruitDao().getAll()

        // THEN - There is only 1 fruit in the database, and contains the expected values
        assertEquals(1, fruits.size)
        assertEquals(fruits[0].id, fruit.id)
        assertEquals(fruits[0].title, fruit.title)
        assertEquals(fruits[0].description, fruit.description)
        assertEquals(fruits[0].isCompleted, fruit.isCompleted)
    }

    @Test
    fun updatefruitAndGetById() = runTest {
        // When inserting a fruit
        val originalfruit = LocalFruit(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )

        database.fruitDao().upsert(originalfruit)

        // When the fruit is updated
        val updatedfruit = LocalFruit(
            title = "new title",
            description = "new description",
            isCompleted = true,
            id = originalfruit.id
        )
        database.fruitDao().upsert(updatedfruit)

        // THEN - The loaded data contains the expected values
        val loaded = database.fruitDao().getById(originalfruit.id)
        assertEquals(originalfruit.id, loaded?.id)
        assertEquals("new title", loaded?.title)
        assertEquals("new description", loaded?.description)
        assertEquals(true, loaded?.isCompleted)
    }

    @Test
    fun updateCompletedAndGetById() = runTest {
        // When inserting a fruit
        val fruit = LocalFruit(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = true
        )
        database.fruitDao().upsert(fruit)

        // When the fruit is updated
        database.fruitDao().updateCompleted(fruit.id, false)

        // THEN - The loaded data contains the expected values
        val loaded = database.fruitDao().getById(fruit.id)
        assertEquals(fruit.id, loaded?.id)
        assertEquals(fruit.title, loaded?.title)
        assertEquals(fruit.description, loaded?.description)
        assertEquals(false, loaded?.isCompleted)
    }

    @Test
    fun deletefruitByIdAndGettingfruits() = runTest {
        // Given a fruit inserted
        val fruit = LocalFruit(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )
        database.fruitDao().upsert(fruit)

        // When deleting a fruit by id
        database.fruitDao().deleteById(fruit.id)

        // THEN - The list is empty
        val fruits = database.fruitDao().getAll()
        assertEquals(true, fruits.isEmpty())
    }

    @Test
    fun deletefruitsAndGettingfruits() = runTest {
        // Given a fruit inserted
        database.fruitDao().upsert(
            LocalFruit(
                title = "title",
                description = "description",
                id = "id",
                isCompleted = false,
            )
        )

        // When deleting all fruits
        database.fruitDao().deleteAll()

        // THEN - The list is empty
        val fruits = database.fruitDao().getAll()
        assertEquals(true, fruits.isEmpty())
    }

    @Test
    fun deleteCompletedfruitsAndGettingfruits() = runTest {
        // Given a completed fruit inserted
        database.fruitDao().upsert(
            LocalFruit(title = "completed", description = "fruit", id = "id", isCompleted = true)
        )

        // When deleting completed fruits
        database.fruitDao().deleteCompleted()

        // THEN - The list is empty
        val fruits = database.fruitDao().getAll()
        assertEquals(true, fruits.isEmpty())
    }
}

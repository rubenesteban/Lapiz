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

import com.example.android.architecture.blueprints.Fruitapp.data.source.local.FakefruitDao
import com.example.android.architecture.blueprints.Fruitapp.data.source.network.FakeNetworkDataSource
import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
@ExperimentalCoroutinesApi
class DefaultfruitRepositoryTest {

    private val fruit1 = fruit(id = "1", title = "Title1", description = "Description1")
    private val fruit2 = fruit(id = "2", title = "Title2", description = "Description2")
    private val fruit3 = fruit(id = "3", title = "Title3", description = "Description3")

    private val newfruitTitle = "Title new"
    private val newfruitDescription = "Description new"
    private val newfruit = fruit(id = "new", title = newfruitTitle, description = newfruitDescription)
    private val newfruits = listOf(newfruit)

    private val networkfruits = listOf(fruit1, fruit2).toNetwork()
    private val LocalFruits = listOf(fruit3.toLocal())

    // Test dependencies
    private lateinit var networkDataSource: FakeNetworkDataSource
    private lateinit var localDataSource: FakefruitDao

    private var testDispatcher = UnconfinedTestDispatcher()
    private var testScope = TestScope(testDispatcher)

    // Class under test
    private lateinit var fruitRepository: DefaultfruitRepository

    @ExperimentalCoroutinesApi
    @Before
    fun createRepository() {
        networkDataSource = FakeNetworkDataSource(networkfruits.toMutableList())
        localDataSource = FakefruitDao(LocalFruits)
        // Get a reference to the class under test
        fruitRepository = DefaultfruitRepository(
            networkDataSource = networkDataSource,
            localDataSource = localDataSource,
            dispatcher = testDispatcher,
            scope = testScope
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getfruits_emptyRepositoryAndUninitializedCache() = testScope.runTest {
        networkDataSource.fruits?.clear()
        localDataSource.deleteAll()

        assertThat(fruitRepository.getfruits().size).isEqualTo(0)
    }

    @Test
    fun getfruits_repositoryCachesAfterFirstApiCall() = testScope.runTest {
        // Trigger the repository to load fruits from the remote data source
        val initial = fruitRepository.getfruits(forceUpdate = true)

        // Change the remote data source
        networkDataSource.fruits = newfruits.toNetwork().toMutableList()

        // Load the fruits again without forcing a refresh
        val second = fruitRepository.getfruits()

        // Initial and second should match because we didn't force a refresh (no fruits were loaded
        // from the remote data source)
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getfruits_requestsAllfruitsFromRemoteDataSource() = testScope.runTest {
        // When fruits are requested from the fruits repository
        val fruits = fruitRepository.getfruits(true)

        // Then fruits are loaded from the remote data source
        assertThat(fruits).isEqualTo(networkfruits.toExternal())
    }

    @Test
    fun savefruit_savesToLocalAndRemote() = testScope.runTest {
        // When a fruit is saved to the fruits repository
        val newfruitId = fruitRepository.createfruit(newfruit.title, newfruit.description)

        // Then the remote and local sources contain the new fruit
        assertThat(networkDataSource.fruits?.map { it.id }?.contains(newfruitId))
        assertThat(localDataSource.fruits?.map { it.id }?.contains(newfruitId))
    }

    @Test
    fun getfruits_WithDirtyCache_fruitsAreRetrievedFromRemote() = testScope.runTest {
        // First call returns from REMOTE
        val fruits = fruitRepository.getfruits()

        // Set a different list of fruits in REMOTE
        networkDataSource.fruits = newfruits.toNetwork().toMutableList()

        // But if fruits are cached, subsequent calls load from cache
        val cachedfruits = fruitRepository.getfruits()
        assertThat(cachedfruits).isEqualTo(fruits)

        // Now force remote loading
        val refreshedfruits = fruitRepository.getfruits(true)

        // fruits must be the recently updated in REMOTE
        assertThat(refreshedfruits).isEqualTo(newfruits)
    }

    @Test(expected = Exception::class)
    fun getfruits_WithDirtyCache_remoteUnavailable_throwsException() = testScope.runTest {
        // Make remote data source unavailable
        networkDataSource.fruits = null

        // Load fruits forcing remote load
        fruitRepository.getfruits(true)

        // Exception should be thrown
    }

    @Test
    fun getfruits_WithRemoteDataSourceUnavailable_fruitsAreRetrievedFromLocal() =
        testScope.runTest {
            // When the remote data source is unavailable
            networkDataSource.fruits = null

            // The repository fetches from the local source
            assertThat(fruitRepository.getfruits()).isEqualTo(LocalFruits.toExternal())
        }

    @Test(expected = Exception::class)
    fun getfruits_WithBothDataSourcesUnavailable_throwsError() = testScope.runTest {
        // When both sources are unavailable
        networkDataSource.fruits = null
        localDataSource.fruits = null

        // The repository throws an error
        fruitRepository.getfruits()
    }

    @Test
    fun getfruits_refreshesLocalDataSource() = testScope.runTest {
        // Forcing an update will fetch fruits from remote
        val expectedfruits = networkfruits.toExternal()

        val newfruits = fruitRepository.getfruits(true)

        assertEquals(expectedfruits, newfruits)
        assertEquals(expectedfruits, localDataSource.fruits?.toExternal())
    }

    @Test
    fun completefruit_completesfruitToServiceAPIUpdatesCache() = testScope.runTest {
        // Save a fruit
        val newfruitId = fruitRepository.createfruit(newfruit.title, newfruit.description)

        // Make sure it's active
        assertThat(fruitRepository.getfruit(newfruitId)?.isCompleted).isFalse()

        // Mark is as complete
        fruitRepository.completefruit(newfruitId)

        // Verify it's now completed
        assertThat(fruitRepository.getfruit(newfruitId)?.isCompleted).isTrue()
    }

    @Test
    fun completefruit_activefruitToServiceAPIUpdatesCache() = testScope.runTest {
        // Save a fruit
        val newfruitId = fruitRepository.createfruit(newfruit.title, newfruit.description)
        fruitRepository.completefruit(newfruitId)

        // Make sure it's completed
        assertThat(fruitRepository.getfruit(newfruitId)?.isActive).isFalse()

        // Mark is as active
        fruitRepository.activatefruit(newfruitId)

        // Verify it's now activated
        assertThat(fruitRepository.getfruit(newfruitId)?.isActive).isTrue()
    }

    @Test
    fun getfruit_repositoryCachesAfterFirstApiCall() = testScope.runTest {
        // Obtain a fruit from the local data source
        localDataSource = FakefruitDao(mutableListOf(fruit1.toLocal()))
        val initial = fruitRepository.getfruit(fruit1.id)

        // Change the fruits on the remote
        networkDataSource.fruits = newfruits.toNetwork().toMutableList()

        // Obtain the same fruit again
        val second = fruitRepository.getfruit(fruit1.id)

        // Initial and second fruits should match because we didn't force a refresh
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getfruit_forceRefresh() = testScope.runTest {
        // Trigger the repository to load data, which loads from remote and caches
        networkDataSource.fruits = mutableListOf(fruit1.toNetwork())
        val fruit1FirstTime = fruitRepository.getfruit(fruit1.id, forceUpdate = true)
        assertThat(fruit1FirstTime?.id).isEqualTo(fruit1.id)

        // Configure the remote data source to return a different fruit
        networkDataSource.fruits = mutableListOf(fruit2.toNetwork())

        // Force refresh
        val fruit1SecondTime = fruitRepository.getfruit(fruit1.id, true)
        val fruit2SecondTime = fruitRepository.getfruit(fruit2.id, true)

        // Only fruit2 works because fruit1 does not exist on the remote
        assertThat(fruit1SecondTime).isNull()
        assertThat(fruit2SecondTime?.id).isEqualTo(fruit2.id)
    }

    @Test
    fun clearCompletedfruits() = testScope.runTest {
        val completedfruit = fruit1.copy(isCompleted = true)
        localDataSource.fruits = listOf(completedfruit.toLocal(), fruit2.toLocal())
        fruitRepository.clearCompletedfruits()

        val fruits = fruitRepository.getfruits(true)

        assertThat(fruits).hasSize(1)
        assertThat(fruits).contains(fruit2)
        assertThat(fruits).doesNotContain(completedfruit)
    }

    @Test
    fun deleteAllfruits() = testScope.runTest {
        val initialfruits = fruitRepository.getfruits()

        // Verify fruits are returned
        assertThat(initialfruits.size).isEqualTo(1)

        // Delete all fruits
        fruitRepository.deleteAllfruits()

        // Verify fruits are empty now
        val afterDeletefruits = fruitRepository.getfruits()
        assertThat(afterDeletefruits).isEmpty()
    }

    @Test
    fun deleteSinglefruit() = testScope.runTest {
        val initialfruitsSize = fruitRepository.getfruits(true).size

        // Delete first fruit
        fruitRepository.deletefruit(fruit1.id)

        // Fetch data again
        val afterDeletefruits = fruitRepository.getfruits(true)

        // Verify only one fruit was deleted
        assertThat(afterDeletefruits.size).isEqualTo(initialfruitsSize - 1)
        assertThat(afterDeletefruits).doesNotContain(fruit1)
    }
}

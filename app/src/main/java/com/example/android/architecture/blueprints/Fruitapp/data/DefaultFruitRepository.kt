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

import com.example.android.architecture.blueprints.Fruitapp.data.source.local.fruitDao
import com.example.android.architecture.blueprints.Fruitapp.data.source.network.NetworkDataSource
import com.example.android.architecture.blueprints.Fruitapp.di.ApplicationScope
import com.example.android.architecture.blueprints.Fruitapp.di.DefaultDispatcher
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Default implementation of [fruitRepository]. Single entry point for managing fruits' data.
 *
 * @param networkDataSource - The network data source
 * @param localDataSource - The local data source
 * @param dispatcher - The dispatcher to be used for long running or complex operations, such as ID
 * generation or mapping many models.
 * @param scope - The coroutine scope used for deferred jobs where the result isn't important, such
 * as sending data to the network.
 */
@Singleton
class DefaultfruitRepository @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: fruitDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope,
) : fruitRepository {

    override suspend fun createfruit(title: String, categoria:String, description: String): String {
        // ID creation might be a complex operation so it's executed using the supplied
        // coroutine dispatcher
        val fruitId = withContext(dispatcher) {
            UUID.randomUUID().toString()
        }
        val fruit = fruit(
            title = title,
            categoria = categoria,
            description = description,
            id = fruitId

        )
        localDataSource.upsert(fruit.toLocal())
        savefruitsToNetwork()
        return fruitId
    }

    override suspend fun updatefruit(fruitId: String, title: String, categoria: String, description: String ) {
        val fruit = getfruit(fruitId)?.copy(
            title = title,
            categoria = categoria,
            description = description,

        ) ?: throw Exception("fruit (id $fruitId) not found")

        localDataSource.upsert(fruit.toLocal())
        savefruitsToNetwork()
    }

    override suspend fun getfruits(forceUpdate: Boolean): List<fruit> {
        if (forceUpdate) {
            refresh()
        }
        return withContext(dispatcher) {
            localDataSource.getAll().toExternal()
        }
    }

    override fun getfruitsStream(): Flow<List<fruit>> {
        return localDataSource.observeAll().map { fruits ->
            withContext(dispatcher) {
                fruits.toExternal()
            }
        }
    }

    override suspend fun refreshfruit(fruitId: String) {
        refresh()
    }

    override fun getfruitStream(fruitId: String): Flow<fruit?> {
        return localDataSource.observeById(fruitId).map { it.toExternal() }
    }

    /**
     * Get a fruit with the given ID. Will return null if the fruit cannot be found.
     *
     * @param fruitId - The ID of the fruit
     * @param forceUpdate - true if the fruit should be updated from the network data source first.
     */
    override suspend fun getfruit(fruitId: String, forceUpdate: Boolean): fruit? {
        if (forceUpdate) {
            refresh()
        }
        return localDataSource.getById(fruitId)?.toExternal()
    }

    override suspend fun completefruit(fruitId: String) {
        localDataSource.updateCompleted(fruitId = fruitId, completed = true)
        savefruitsToNetwork()
    }

    override suspend fun activatefruit(fruitId: String) {
        localDataSource.updateCompleted(fruitId = fruitId, completed = false)
        savefruitsToNetwork()
    }

    override suspend fun clearCompletedfruits() {
        localDataSource.deleteCompleted()
        savefruitsToNetwork()
    }

    override suspend fun deleteAllfruits() {
        localDataSource.deleteAll()
        savefruitsToNetwork()
    }

    override suspend fun deletefruit(fruitId: String) {
        localDataSource.deleteById(fruitId)
        savefruitsToNetwork()
    }

    /**
     * The following methods load fruits from (refresh), and save fruits to, the network.
     *
     * Real apps may want to do a proper sync, rather than the "one-way sync everything" approach
     * below. See https://developer.android.com/topic/architecture/data-layer/offline-first
     * for more efficient and robust synchronisation strategies.
     *
     * Note that the refresh operation is a suspend function (forces callers to wait) and the save
     * operation is not. It returns immediately so callers don't have to wait.
     */

    /**
     * Delete everything in the local data source and replace it with everything from the network
     * data source.
     *
     * `withContext` is used here in case the bulk `toLocal` mapping operation is complex.
     */
    override suspend fun refresh() {
        withContext(dispatcher) {
            val remotefruits = networkDataSource.loadfruits()
            localDataSource.deleteAll()
            localDataSource.upsertAll(remotefruits.toLocal())
        }
    }

    /**
     * Send the fruits from the local data source to the network data source
     *
     * Returns immediately after launching the job. Real apps may want to suspend here until the
     * operation is complete or (better) use WorkManager to schedule this work. Both approaches
     * should provide a mechanism for failures to be communicated back to the user so that
     * they are aware that their data isn't being backed up.
     */
    private fun savefruitsToNetwork() {
        scope.launch {
            try {
                val LocalFruits = localDataSource.getAll()
                val networkfruits = withContext(dispatcher) {
                    LocalFruits.toNetwork()
                }
                networkDataSource.savefruits(networkfruits)
            } catch (e: Exception) {
                // In a real app you'd handle the exception e.g. by exposing a `networkStatus` flow
                // to an app level UI state holder which could then display a Toast message.
            }
        }
    }
}

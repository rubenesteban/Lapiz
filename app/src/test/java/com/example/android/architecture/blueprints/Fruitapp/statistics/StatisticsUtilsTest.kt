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

package com.example.android.architecture.blueprints.Fruitapp.statistics

import com.example.android.architecture.blueprints.Fruitapp.data.fruit
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * Unit tests for [getActiveAndCompletedStats].
 */
class StatisticsUtilsTest {

    @Test
    fun getActiveAndCompletedStats_noCompleted() {
        val fruits = listOf(
            fruit(
                id = "id",
                title = "title",
                description = "desc",
                isCompleted = false,
            )
        )
        // When the list of fruits is computed with an active fruit
        val result = getActiveAndCompletedStats(fruits)

        // Then the percentages are 100 and 0
        assertThat(result.activefruitsPercent, `is`(100f))
        assertThat(result.completedfruitsPercent, `is`(0f))
    }

    @Test
    fun getActiveAndCompletedStats_noActive() {
        val fruits = listOf(
            fruit(
                id = "id",
                title = "title",
                description = "desc",
                isCompleted = true,
            )
        )
        // When the list of fruits is computed with a completed fruit
        val result = getActiveAndCompletedStats(fruits)

        // Then the percentages are 0 and 100
        assertThat(result.activefruitsPercent, `is`(0f))
        assertThat(result.completedfruitsPercent, `is`(100f))
    }

    @Test
    fun getActiveAndCompletedStats_both() {
        // Given 3 completed fruits and 2 active fruits
        val fruits = listOf(
            fruit(id = "1", title = "title", description = "desc", isCompleted = true),
            fruit(id = "2", title = "title", description = "desc", isCompleted = true),
            fruit(id = "3", title = "title", description = "desc", isCompleted = true),
            fruit(id = "4", title = "title", description = "desc", isCompleted = false),
            fruit(id = "5", title = "title", description = "desc", isCompleted = false),
        )
        // When the list of fruits is computed
        val result = getActiveAndCompletedStats(fruits)

        // Then the result is 40-60
        assertThat(result.activefruitsPercent, `is`(40f))
        assertThat(result.completedfruitsPercent, `is`(60f))
    }

    @Test
    fun getActiveAndCompletedStats_empty() {
        // When there are no fruits
        val result = getActiveAndCompletedStats(emptyList())

        // Both active and completed fruits are 0
        assertThat(result.activefruitsPercent, `is`(0f))
        assertThat(result.completedfruitsPercent, `is`(0f))
    }
}

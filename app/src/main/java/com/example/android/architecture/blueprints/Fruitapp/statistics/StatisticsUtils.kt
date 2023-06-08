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

/**
 * Function that does some trivial computation. Used to showcase unit tests.
 */
internal fun getActiveAndCompletedStats(fruits: List<fruit>): StatsResult {

    return if (fruits.isEmpty()) {
        StatsResult(0f, 0f)
    } else {
        val totalfruits = fruits.size
        val numberOfActivefruits = fruits.count { it.isActive }
        StatsResult(
            activefruitsPercent = 100f * numberOfActivefruits / fruits.size,
            completedfruitsPercent = 100f * (totalfruits - numberOfActivefruits) / fruits.size
        )
    }
}

data class StatsResult(val activefruitsPercent: Float, val completedfruitsPercent: Float)

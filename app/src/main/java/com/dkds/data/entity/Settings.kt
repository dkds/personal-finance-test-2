/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dkds.data.entity

import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

@Entity(primaryKeys = ["id"])
data class Settings(
    @field:SerializedName("id")
    val id: Int = 1
) {
    var inflationRate: Double = 0.0
    var dependentFamilySize: Int = 1
    var age: Int = 25
    var monthsWithHigherSpending: String = "1,4,12"
    var noOfExpensesAMonth: Int = 60
    var mostFrequentExpenseCategories: String = "transport,food"
    var estimatedMonthlyExpenses: BigDecimal = BigDecimal.valueOf(100000.0)
}

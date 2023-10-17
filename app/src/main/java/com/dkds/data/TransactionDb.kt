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

package com.dkds.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dkds.data.converters.BigDecimalToStringTypeConverter
import com.dkds.data.converters.LocalDateTimeTypeConverter
import com.dkds.data.dao.SettingsDao
import com.dkds.data.dao.TransactionDao
import com.dkds.data.entity.Settings
import com.dkds.data.entity.Transaction

/**
 * Main database description.
 */
@Database(
    entities = [Transaction::class, Settings::class], version = 3, exportSchema = false
)
@TypeConverters(BigDecimalToStringTypeConverter::class, LocalDateTimeTypeConverter::class)
abstract class TransactionDb : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun settingsDao(): SettingsDao
}

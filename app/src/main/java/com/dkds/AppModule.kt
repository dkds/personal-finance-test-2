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

package com.dkds

import android.content.Context
import androidx.room.Room
import com.dkds.data.TransactionDb
import com.dkds.data.dao.SettingsDao
import com.dkds.data.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideDb(@ApplicationContext appContext: Context): TransactionDb {
        return Room.databaseBuilder(
            appContext,
            TransactionDb::class.java,
            "TransactionDb"
        ).build()
    }

    @Singleton
    @Provides
    fun provideTransactionDao(db: TransactionDb): TransactionDao {
        return db.transactionDao()
    }

    @Singleton
    @Provides
    fun provideSettingsDao(db: TransactionDb): SettingsDao {
        return db.settingsDao()
    }
}

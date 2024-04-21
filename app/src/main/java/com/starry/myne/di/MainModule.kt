/**
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.starry.myne.di

import android.content.Context
import com.starry.myne.api.BookAPI
import com.starry.myne.database.MyneDatabase
import com.starry.myne.epub.EpubParser
import com.starry.myne.helpers.PreferenceUtil
import com.starry.myne.helpers.book.BookDownloader
import com.starry.myne.ui.screens.welcome.viewmodels.WelcomeDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class MainModule {

    @Provides
    fun provideAppContext(@ApplicationContext context: Context) = context

    @Singleton
    @Provides
    fun provideMyneDatabase(@ApplicationContext context: Context) =
        MyneDatabase.getInstance(context)

    @Provides
    fun provideLibraryDao(myneDatabase: MyneDatabase) = myneDatabase.getLibraryDao()

    @Provides
    fun provideReaderDao(myneDatabase: MyneDatabase) = myneDatabase.getReaderDao()

    @Singleton
    @Provides
    fun provideBooksApi(@ApplicationContext context: Context) = BookAPI(context)

    @Singleton
    @Provides
    fun provideBookDownloader(@ApplicationContext context: Context) = BookDownloader(context)

    @Singleton
    @Provides
    fun providePreferenceUtil(@ApplicationContext context: Context) = PreferenceUtil(context)

    @Singleton
    @Provides
    fun provideEpubParser() = EpubParser()

    @Provides
    @Singleton
    fun provideDataStoreRepository(
        @ApplicationContext context: Context
    ) = WelcomeDataStore(context = context)
}
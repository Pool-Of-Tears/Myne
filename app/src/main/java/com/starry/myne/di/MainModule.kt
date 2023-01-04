package com.starry.myne.di

import android.content.Context
import com.starry.myne.database.MyneDatabase
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
}
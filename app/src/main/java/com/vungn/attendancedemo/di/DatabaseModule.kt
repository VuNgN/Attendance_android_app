package com.vungn.attendancedemo.di

import android.content.Context
import androidx.room.Room
import com.vungn.attendancedemo.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext applicationContext: Context): AppDatabase {
        return Room.databaseBuilder(applicationContext, AppDatabase::class.java, "attendance.db")
            .fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideClassDao(appDatabase: AppDatabase) = appDatabase.ClassDao()
}

package com.vungn.attendancedemo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WiFiModule {

    @Provides
    @Singleton
    fun nearbyWiFiProvide(): MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
}
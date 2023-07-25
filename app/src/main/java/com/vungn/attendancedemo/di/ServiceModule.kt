package com.vungn.attendancedemo.di

import com.vungn.attendancedemo.data.service.AttendanceService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Singleton
    @Provides
    fun serviceProvide(): AttendanceService {
        val retrofit = Retrofit.Builder().baseUrl("https://mytlu.shinchoku.dev/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        return retrofit.create(AttendanceService::class.java)
    }
}
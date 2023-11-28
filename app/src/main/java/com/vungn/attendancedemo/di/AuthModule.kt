package com.vungn.attendancedemo.di

import android.content.Context
import com.vungn.attendancedemo.util.helper.auth.AuthenticationHelper
import com.vungn.attendancedemo.util.helper.graph.GraphHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object AuthModule {

    @ViewModelScoped
    @Provides
    fun authProvide(@ApplicationContext context: Context): AuthenticationHelper = AuthenticationHelper(context)

    @ViewModelScoped
    @Provides
    fun graphProvide(authProvider: AuthenticationHelper): GraphHelper = GraphHelper(authProvider)
}
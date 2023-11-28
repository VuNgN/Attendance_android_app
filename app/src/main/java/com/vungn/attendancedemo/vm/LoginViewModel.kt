package com.vungn.attendancedemo.vm

import android.app.Activity
import com.microsoft.graph.models.User
import com.vungn.attendancedemo.util.LoginState
import kotlinx.coroutines.flow.StateFlow

interface LoginViewModel {
    val user: StateFlow<User?>
    val loginState: StateFlow<LoginState>
    suspend fun login(activity: Activity)
}
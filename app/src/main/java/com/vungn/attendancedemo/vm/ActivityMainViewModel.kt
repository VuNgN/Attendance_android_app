package com.vungn.attendancedemo.vm

import com.vungn.attendancedemo.util.LoginState
import kotlinx.coroutines.flow.StateFlow

interface ActivityMainViewModel {
    val loginState: StateFlow<LoginState>
    fun checkLoginState()
}
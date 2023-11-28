package com.vungn.attendancedemo.vm

import com.microsoft.graph.models.User
import com.vungn.attendancedemo.util.LoginState
import com.vungn.attendancedemo.util.MessageError
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MainViewModel {
    val user            : StateFlow<User?>
    val loginState      : StateFlow<LoginState>
    val loading         : StateFlow<Boolean>
    val isCheckingLogin : StateFlow<Boolean>
    val isOnline        : StateFlow<Boolean>
    val isAllSynced     : StateFlow<Boolean>
    val isSyncedSuccess : SharedFlow<Boolean>
    val numOfNotSyncs   : StateFlow<Int>
    val syncMessage     : StateFlow<MessageError?>
    fun syncAll()
    fun checkLoginState()
}
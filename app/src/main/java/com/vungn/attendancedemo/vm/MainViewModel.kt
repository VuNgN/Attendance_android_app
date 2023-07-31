package com.vungn.attendancedemo.vm

import com.vungn.attendancedemo.util.Message
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MainViewModel {
    val loading: StateFlow<Boolean>
    val isOnline: StateFlow<Boolean>
    val isAllSynced: StateFlow<Boolean>
    val isSyncedSuccess: SharedFlow<Boolean>
    val numOfNotSyncs: StateFlow<Int>
    val syncMessage: StateFlow<Message?>
    fun syncAll()
}

package com.vungn.attendancedemo.vm

import android.app.AlarmManager
import android.content.Context
import com.vungn.attendancedemo.util.MessageError
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MainViewModel {
    val loading: StateFlow<Boolean>
    val isOnline: StateFlow<Boolean>
    val isAllSynced: StateFlow<Boolean>
    val isSyncedSuccess: SharedFlow<Boolean>
    val numOfNotSyncs: StateFlow<Int>
    val syncMessage: StateFlow<MessageError?>
    fun startAlarm(context: Context, alarmManager: AlarmManager)
    fun syncAll()
}
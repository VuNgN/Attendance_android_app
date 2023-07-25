package com.vungn.attendancedemo.vm

import com.vungn.attendancedemo.model.OverviewClass
import com.vungn.attendancedemo.util.MessageError
import kotlinx.coroutines.flow.StateFlow

interface AttendanceViewModel {
    val isAttended: StateFlow<Boolean>
    val overviewClazz: StateFlow<OverviewClass?>
    val message: StateFlow<MessageError?>
    fun attend()
    fun clearQrCode()
}
package com.vungn.attendancedemo.vm

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.vungn.attendancedemo.model.OverviewClass
import com.vungn.attendancedemo.util.Message
import com.vungn.attendancedemo.vm.impl.AttendanceViewModelImpl
import kotlinx.coroutines.flow.StateFlow

interface AttendanceViewModel {
    val qrcode: StateFlow<String?>
    val isWifiEnable: StateFlow<Boolean>
    val isBluetoothEnable: StateFlow<Boolean>
    val isBluetoothScanning: StateFlow<Boolean>
    val overviewClass: StateFlow<OverviewClass?>
    val attendanceState: StateFlow<AttendanceViewModelImpl.AttendanceState>
    val error: StateFlow<Message?>
    val isAttendanceEnable: StateFlow<Boolean>
    fun bindCamera(previewView: PreviewView, lifecycle: LifecycleOwner, width: Int)
    fun unbindCamera()
    fun attend()
    fun enableWifi()
}

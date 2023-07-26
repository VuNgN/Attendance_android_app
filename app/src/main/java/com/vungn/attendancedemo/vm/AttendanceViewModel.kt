package com.vungn.attendancedemo.vm

import com.vungn.attendancedemo.model.NearbyBluetooth
import com.vungn.attendancedemo.model.NearbyWifi
import com.vungn.attendancedemo.model.OverviewClass
import com.vungn.attendancedemo.util.MessageError
import kotlinx.coroutines.flow.StateFlow

interface AttendanceViewModel {
    val isAttended: StateFlow<Boolean>
    val overviewClazz: StateFlow<OverviewClass?>
    val message: StateFlow<MessageError?>
    val nearbyWifi: StateFlow<List<NearbyWifi>>
    val nearbyBluetooth: StateFlow<Set<NearbyBluetooth>>
    val isWifiEnable: StateFlow<Boolean>
    val isBluetoothEnable: StateFlow<Boolean>
    fun attend()
    fun refreshWifi()
    fun refreshBluetooth()
    fun enableWifi()
    fun clearQrCode()
}
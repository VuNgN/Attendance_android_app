package com.vungn.attendancedemo.vm.impl

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vungn.attendancedemo.model.NearbyBluetooth
import com.vungn.attendancedemo.model.NearbyWifi
import com.vungn.attendancedemo.model.OverviewClass
import com.vungn.attendancedemo.repo.AttendRepo
import com.vungn.attendancedemo.repo.SavedResult
import com.vungn.attendancedemo.util.BluetoothHelper
import com.vungn.attendancedemo.util.MessageError
import com.vungn.attendancedemo.util.WifiHelper
import com.vungn.attendancedemo.util.toClazz
import com.vungn.attendancedemo.util.toOverviewClass
import com.vungn.attendancedemo.vm.AttendanceViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModelImpl @Inject constructor(
    private var _qrcode: MutableStateFlow<String?>,
    private val attendRepo: AttendRepo,
    private val wifiHelper: WifiHelper,
    private val bluetoothHelper: BluetoothHelper
) : ViewModel(), AttendanceViewModel {
    private val _isAttended: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _message: MutableStateFlow<MessageError?> = MutableStateFlow(null)
    private val _overviewClazz: MutableStateFlow<OverviewClass?> =
        MutableStateFlow(_qrcode.value?.toOverviewClass())
    private val _nearbyWifi: StateFlow<List<NearbyWifi>> = wifiHelper.nearbyWifi
    private val _nearbyBluetooth: StateFlow<Set<NearbyBluetooth>> = bluetoothHelper.scannedDevices
    private val _isWifiEnable: StateFlow<Boolean> = wifiHelper.isEnable
    private val _isBluetoothEnable: StateFlow<Boolean> = bluetoothHelper.isEnable

    override val isAttended: StateFlow<Boolean>
        get() = _isAttended
    override val overviewClazz: StateFlow<OverviewClass?>
        get() = _overviewClazz
    override val message: StateFlow<MessageError?>
        get() = _message
    override val nearbyWifi: StateFlow<List<NearbyWifi>>
        get() = _nearbyWifi
    override val nearbyBluetooth: StateFlow<Set<NearbyBluetooth>>
        get() = _nearbyBluetooth
    override val isWifiEnable: StateFlow<Boolean>
        get() = _isWifiEnable
    override val isBluetoothEnable: StateFlow<Boolean>
        get() = _isBluetoothEnable

    init {
        viewModelScope.launch {
            _isBluetoothEnable.collect { isEnable ->
                Log.d(TAG, "on bluetooth enable change: $isEnable")
                if (isEnable) {
                    scanBluetooth()
                } else {
                    cancelScanBluetooth()
                }
            }
        }
        viewModelScope.launch {
            _isWifiEnable.collect { isEnable ->
                if (isEnable) {
                    scanWifi()
                }
            }
        }
    }

    override fun attend() {
        if (_overviewClazz.value != null) {
            val clazz = _overviewClazz.value!!.toClazz(
                _nearbyWifi.value.map { it.address },
                _nearbyBluetooth.value.map { it.address },
                Calendar.getInstance().time
            )
            viewModelScope.launch(Dispatchers.IO) {
                attendRepo.execute(clazz = clazz, onSavedResult = object : SavedResult {
                    override fun onSuccess() {
                        viewModelScope.launch {
                            _isAttended.emit(true)
                        }
                    }

                    override fun onError(error: String?) {
                        viewModelScope.launch {
                            Log.e(TAG, "Save to local false: $error")
                            _message.emit(MessageError("Attendance failed"))
                            _isAttended.emit(false)
                        }
                    }
                })
            }
        }
    }

    override fun refreshWifi() {
        scanWifi()
    }

    override fun refreshBluetooth() {
        bluetoothHelper.refreshDiscovery()
    }

    override fun enableWifi() {
        wifiHelper.enable()
    }

    override fun clearQrCode() {
        viewModelScope.launch {
            _qrcode.emit(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        wifiHelper.release()
        bluetoothHelper.release()
    }

    private fun scanWifi() {
        wifiHelper.scanWifi()
    }

    private fun scanBluetooth() {
        bluetoothHelper.startDiscovery()
    }

    private fun cancelScanBluetooth() {
        bluetoothHelper.cancelDiscovery()
    }

    companion object {
        private const val TAG = "AttendanceViewModelImpl"
    }
}
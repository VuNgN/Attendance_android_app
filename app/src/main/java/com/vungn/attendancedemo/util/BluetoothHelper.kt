package com.vungn.attendancedemo.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import com.vungn.attendancedemo.model.NearbyBluetooth
import com.vungn.attendancedemo.receiver.BluetoothReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BluetoothHelper @Inject constructor(@ApplicationContext private val context: Context) {
    private val bluetoothReceiverListener = object : BluetoothReceiver.BluetoothReceiverListener {
        override fun onStateChange(state: Int) {
            _state.update { state }
            _isEnable.update { _bluetoothAdapter.isEnabled }
        }

        override fun onFoundDevice(device: BluetoothDevice) {
            _scannedDevices.update { devices ->
                devices + NearbyBluetooth(device.name, device.address)
            }
        }
    }

    private val bluetoothReceiver = BluetoothReceiver().also {
        it.bluetoothReceiverListener = bluetoothReceiverListener
    }
    private val _bluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val _bluetoothAdapter by lazy {
        _bluetoothManager.adapter
    }
    private val _state = MutableStateFlow(_bluetoothAdapter.state)
    private val _isEnable = MutableStateFlow(_bluetoothAdapter.isEnabled)
    private val _scannedDevices = MutableStateFlow(setOf<NearbyBluetooth>())

    val scannedDevices: StateFlow<Set<NearbyBluetooth>>
        get() = _scannedDevices
    val isEnable: StateFlow<Boolean>
        get() = _isEnable

    init {
        val intent = IntentFilter().also {
            it.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            it.addAction(BluetoothDevice.ACTION_FOUND)
        }
        context.registerReceiver(bluetoothReceiver, intent)
    }

    fun startDiscovery() {
        _bluetoothAdapter.startDiscovery()
    }

    private fun cancelDiscovery() {
        _bluetoothAdapter.cancelDiscovery()
    }

    fun release() {
        cancelDiscovery()
        context.unregisterReceiver(bluetoothReceiver)
    }
}

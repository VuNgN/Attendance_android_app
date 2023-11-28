package com.vungn.attendancedemo.util.helper.wifi

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.vungn.attendancedemo.model.NearbyWifi
import com.vungn.attendancedemo.receiver.WiFiReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
class WifiHelper @Inject constructor(@ApplicationContext private val context: Context) {
    private val _wifiManager: WifiManager =
        context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val _nearbyWifi: MutableStateFlow<List<NearbyWifi>> = MutableStateFlow(emptyList())
    private val _wifiState: MutableStateFlow<Int> = MutableStateFlow(_wifiManager.wifiState)
    private val _isEnable: MutableStateFlow<Boolean> = MutableStateFlow(_wifiManager.isWifiEnabled)
    private val _onWifiScanResult: WiFiReceiver.OnWifiScanResult =
        object : WiFiReceiver.OnWifiScanResult {
            override fun onScanResult(success: Boolean) {
                Log.d(TAG, "Scan Wi-Fi result: $success")
                _nearbyWifi.update {
                    _wifiManager.scanResults.map {
                        NearbyWifi(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                it.wifiSsid.toString()
                            } else {
                                it.SSID
                            }, it.BSSID
                        )
                    }
                }
            }

            override fun onStateChange(state: Int) {
                Log.d(TAG, "Wi-Fi state changed: $state")
                _wifiState.update { state }
                _isEnable.update { _wifiManager.isWifiEnabled }
            }
        }
    private val _wifiReceiver: WiFiReceiver = WiFiReceiver().also {
        it.onWifiScanResult = _onWifiScanResult
    }

    val isEnable: StateFlow<Boolean>
        get() = _isEnable
    val nearbyWifi: StateFlow<List<NearbyWifi>>
        get() = _nearbyWifi

    init {
        val intentFilter = IntentFilter().also {
            it.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            it.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        }
        context.registerReceiver(_wifiReceiver, intentFilter)
    }

    fun scanWifi() {
        _wifiManager.startScan()
    }

    fun enable() {
        _wifiManager.isWifiEnabled = true
    }

    fun release() {
        context.unregisterReceiver(_wifiReceiver)
    }

    companion object {
        private const val TAG = "WifiHelper"
    }
}

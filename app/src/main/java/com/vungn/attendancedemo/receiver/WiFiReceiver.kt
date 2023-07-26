package com.vungn.attendancedemo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.util.Log

class WiFiReceiver : BroadcastReceiver() {
    private var _onWifiScanResult: OnWifiScanResult? = null
    var onWifiScanResult: OnWifiScanResult?
        get() = _onWifiScanResult
        set(value) {
            _onWifiScanResult = value
        }

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
            Log.d(TAG, "onReceive: Wi-Fi scanning")
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            Log.d(TAG, "Wi-Fi scanning: $success")
            _onWifiScanResult?.onScanResult(success)
        }
        if (intent.action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
            val state =
                intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
            Log.d(TAG, "onReceive: Wi-Fi state changed: $state")
            _onWifiScanResult?.onStateChange(state)
        }
    }

    interface OnWifiScanResult {
        fun onScanResult(success: Boolean)
        fun onStateChange(state: Int)
    }

    companion object {
        private const val TAG = "WiFiReceiver"
    }
}
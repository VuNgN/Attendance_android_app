package com.vungn.attendancedemo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.util.Log

class WiFiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
            Log.d(TAG, "onReceive: Wi-Fi scanning")
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            Log.d(TAG, "Wi-Fi scanning: $success")
        }
    }

    companion object {
        private const val TAG = "WiFiReceiver"
    }
}
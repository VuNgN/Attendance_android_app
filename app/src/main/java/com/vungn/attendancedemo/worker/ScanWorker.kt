package com.vungn.attendancedemo.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

@HiltWorker
class ScanWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val nearbyWiFis: MutableStateFlow<Set<String>>
) : CoroutineWorker(appContext, workerParams) {
    private val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    override suspend fun doWork() = withContext(Dispatchers.Main) {
        Log.d(TAG, "doWork: Start scanning")
        val success = wifiManager.startScan()
        if (checkPermission()) {
            Result.failure()
        }
        nearbyWiFis.emit(wifiManager.scanResults.map {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                it.wifiSsid.toString()
            } else {
                it.SSID
            }
        }.toSet())
        if (success) {
            Result.success()
        }
        Result.failure()
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    companion object {
        const val TAG = "ScanWorker"
    }
}
package com.vungn.attendancedemo.worker

import android.app.Notification
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.vungn.attendancedemo.MyApplication
import com.vungn.attendancedemo.R
import com.vungn.attendancedemo.data.dao.ClassDao
import com.vungn.attendancedemo.data.service.AttendanceService
import com.vungn.attendancedemo.util.BluetoothHelper
import com.vungn.attendancedemo.util.WifiHelper
import com.vungn.attendancedemo.util.toAttendClass
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse

@HiltWorker
class AttendWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val service: AttendanceService,
    private val dao: ClassDao,
    private val wifiHelper: WifiHelper,
    private val bluetoothHelper: BluetoothHelper
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Start attend work")
        setForeground(createForegroundInfo())
        wifiHelper.scanWifi()
        bluetoothHelper.startDiscovery()
        delay(12000)
        Log.d(
            TAG, "Nearby Wi-Fi: ${Gson().toJson(wifiHelper.nearbyWifi.value)}\nNearby Bluetooth: ${
                Gson().toJson(
                    bluetoothHelper.scannedDevices.value
                )
            }"
        )
        val id = inputData.getLong(ID_INPUT, -1L)
        Log.d(TAG, "Id: $id")
        if (id == -1L) {
            Result.failure()
        }
        val clazz = dao.getClassById(id)
        val newClass =
            clazz.copy(nearbyBluetooth = bluetoothHelper.scannedDevices.value.map { it.address },
                nearbyWiFi = wifiHelper.nearbyWifi.value.map { it.address })

        dao.update(newClass)
        val call = service.attendAll(listOf(newClass.toAttendClass()))
        call.clone().enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Work success")
                } else {
                    Log.e(TAG, "Work failure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e(TAG, "Work failure: ${t.message}")
            }
        })
        call.awaitResponse().let {
            if (it.isSuccessful) {
                val syncedData = newClass.copy(isSync = true)
                dao.update(syncedData)
                Log.d(TAG, "Data with id($id) was updated: ${Gson().toJson(syncedData)}")
                Result.success()
            } else {
                val data = Data.Builder().putString(MESSAGE_OUTPUT, it.message()).build()
                Result.failure(data)
            }
        }.also {
            wifiHelper.release()
            bluetoothHelper.release()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification: Notification =
            NotificationCompat.Builder(applicationContext, MyApplication.CHANNEL_ID)
                .setContentTitle("Attendance").setContentText("Attending")
                .setSmallIcon(R.drawable.ic_launcher_foreground).setTicker("")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE).setOngoing(true).build()

        //  Notification ID cannot be 0.
        return ForegroundInfo(ONGOING_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "AttendWorker"
        const val ID_INPUT = "id"
        const val MESSAGE_OUTPUT = "message"
        private const val ONGOING_NOTIFICATION_ID = 1
    }
}

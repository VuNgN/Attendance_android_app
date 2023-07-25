package com.vungn.attendancedemo.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vungn.attendancedemo.data.dao.ClassDao
import com.vungn.attendancedemo.data.service.AttendanceService
import com.vungn.attendancedemo.model.Clazz
import com.vungn.attendancedemo.util.toAttendClass
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@HiltWorker
class AttendWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val _dao: ClassDao,
    private val _server: AttendanceService
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "doWork: Start")
        var result: Result = Result.failure()
        try {
            _dao.getNotSyncs().collect { classes ->
                val attendClasses = classes.map {
                    it.toAttendClass()
                }
                val response = _server.attendAll(attendClasses)
                response.enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        result = if (response.isSuccessful) {
                            Result.success()
                        } else {
                            Result.retry()
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            updateLocalDatabase(classes)
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        result = Result.retry()
                    }
                })
            }
        } catch (e: Exception) {
            result = Result.retry()
        }
        result
    }

    private fun updateLocalDatabase(classes: List<Clazz>) {
        val syncedClass = classes.map {
            it.copy(isSync = true)
        }
        _dao.updateAll(syncedClass)
    }

    companion object {
        private const val TAG = "AttendWorker"
    }
}
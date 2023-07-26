package com.vungn.attendancedemo.repo

import android.util.Log
import com.vungn.attendancedemo.data.dao.ClassDao
import com.vungn.attendancedemo.data.service.AttendanceService
import com.vungn.attendancedemo.model.Clazz
import com.vungn.attendancedemo.util.toAttendClass
import com.vungn.attendancedemo.vm.impl.MainViewModelImpl
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class AttendAllRepo @Inject constructor(
    private val _service: AttendanceService, private val _dao: ClassDao
) : Execute() {
    private lateinit var _data: List<Clazz>
    private lateinit var _onPushedResult: PushedResult<Boolean>

    suspend fun checkSyncAll(onCheckSyncAll: MainViewModelImpl.OnCheckSyncAll) {
        _dao.getNotSyncs().collect {
            if (it.isEmpty()) {
                onCheckSyncAll.onEmpty()
            } else {
                onCheckSyncAll.onNotEmpty(it.size)
            }
        }
    }

    suspend fun execute(onPushedResult: PushedResult<Boolean>) {
        _onPushedResult = onPushedResult
        execute()
    }

    override suspend fun save(): Boolean {
        // Save nothing
        return true
    }

    override suspend fun push(): Flow<Boolean> = callbackFlow {
        _data = _dao.getNotSyncs().stateIn(this@callbackFlow).value
        val call = _service.attendAll(_data.map { clazz -> clazz.toAttendClass() })
        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    _onPushedResult.onSuccess(true)
                    launch { send(true) }
                } else {
                    _onPushedResult.onError(response.message())
                    launch { send(false) }
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                _onPushedResult.onError(t.message)
                launch { send(false) }
            }
        })
        awaitClose { call.cancel() }
    }.onCompletion {
        Log.d(TAG, "Push all data completed")
        _onPushedResult.onReleased()
    }

    override suspend fun updateLocalDatabase() {
        val newData = _data.map { clazz ->
            clazz.apply {
                isSync = true
            }
        }
        _dao.updateAll(newData)
        Log.d(TAG, "All data was updated")
    }

    companion object {
        private const val TAG = "AttendAllRepo"
    }
}
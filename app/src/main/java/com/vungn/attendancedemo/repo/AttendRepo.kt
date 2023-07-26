package com.vungn.attendancedemo.repo

import android.util.Log
import com.google.gson.Gson
import com.vungn.attendancedemo.data.dao.ClassDao
import com.vungn.attendancedemo.data.service.AttendanceService
import com.vungn.attendancedemo.model.Clazz
import com.vungn.attendancedemo.util.toAttendClass
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class AttendRepo @Inject constructor(
    private val _service: AttendanceService, private val _dao: ClassDao
) : Execute() {
    private lateinit var _data: Clazz
    private var _id: Long? = null
    private var _onPushedResult: PushedResult<Unit> = object : PushedResult<Unit> {
        override fun onSuccess(result: Unit?) {
            Log.d(TAG, "On Responded Success: ${Gson().toJson(result)}")
        }

        override fun onError(error: String?) {
            Log.d(TAG, "On Responded Error: $error")
        }

        override fun onReleased() {
            Log.d(TAG, "On Responded Released")
        }
    }
    private var _onSavedResult: SavedResult = object : SavedResult {
        override fun onSuccess() {
            Log.d(TAG, "Save to local success")
        }

        override fun onError(error: String?) {
            Log.d(TAG, "Save to local false: $error")
        }
    }

    suspend fun execute(clazz: Clazz, onSavedResult: SavedResult) {
        _data = clazz
        _onSavedResult = onSavedResult
        execute()
    }

    override suspend fun save(): Boolean {
        _id = _dao.insert(_data)
        if (_id == -1L) {
            _onSavedResult.onError("Insert error")
            return false
        }
        _onSavedResult.onSuccess()
        return true
    }

    override suspend fun push(): Flow<Boolean> = callbackFlow {
        val call = _service.attendAll(listOf(_data.toAttendClass()))
        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    _onPushedResult.onSuccess(response.body())
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
        _onPushedResult.onReleased()
    }

    override suspend fun updateLocalDatabase() {
        if (_id == null) {
            Log.e(TAG, "updateLocalDatabase: Null id")
            return
        }
        val data = _dao.getClassById(_id!!)
        val syncedData = data.copy(isSync = true)
        _dao.update(syncedData)
        Log.d(TAG, "Data with id($_id) was updated: ${Gson().toJson(syncedData)}")
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}
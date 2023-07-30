package com.vungn.attendancedemo.repo

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.gson.Gson
import com.vungn.attendancedemo.data.dao.ClassDao
import com.vungn.attendancedemo.model.Clazz
import com.vungn.attendancedemo.worker.AttendWorker
import com.vungn.attendancedemo.worker.AttendWorker.Companion.ID_INPUT
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class AttendRepo @Inject constructor(
    @ApplicationContext private val context: Context, private val _dao: ClassDao
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
        val uuid = UUID.randomUUID()
        val workManager = WorkManager.getInstance(context)
        val attendWorker: OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<AttendWorker>().setId(uuid).setExpedited(
                OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
            ).setInputData(workDataOf(ID_INPUT to _id)).build()
        workManager.enqueue(attendWorker)
        launch { send(false) }
        awaitClose { }
    }.onCompletion {
        _onPushedResult.onReleased()
    }

    /**
     * Do nothing because database is updated in [AttendWorker]
     */
    override suspend fun updateLocalDatabase() {}

    companion object {
        private val TAG = this::class.simpleName
    }
}

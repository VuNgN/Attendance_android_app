package com.vungn.attendancedemo.repo

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

abstract class Execute {
    /**
     * Execute save and push
     */
    suspend fun execute() {
        withContext(Dispatchers.IO) {
            val isSaved = save()
            if (isSaved) {
                push().collect { isPushed ->
                    Log.d(TAG, "execute: pushing: $isPushed")
                    if (isPushed) {
                        withContext(Dispatchers.IO) {
                            updateLocalDatabase()
                        }
                    }
                }
            }
        }
    }

    /**
     * Save data to local database
     */
    abstract suspend fun save(): Boolean

    /**
     * Push data to server
     */
    abstract suspend fun push(): Flow<Boolean>

    /**
     * Update local database
     */
    abstract suspend fun updateLocalDatabase()

    companion object {
        private const val TAG = "Execute"
    }
}
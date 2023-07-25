package com.vungn.attendancedemo.repo

interface SavedResult {
    /**
     * Save to local database success
     */
    fun onSuccess()

    /**
     * Save to local database error
     */
    fun onError(error: String?)
}
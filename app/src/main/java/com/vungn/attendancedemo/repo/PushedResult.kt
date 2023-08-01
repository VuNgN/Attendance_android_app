package com.vungn.attendancedemo.repo

interface PushedResult<T> {
    /**
     * Callback when success
     */
    fun onSuccess(result: T?)

    /**
     * Callback when error
     */
    fun onError(error: String?)

    /**
     * Callback when released
     */
    fun onReleased()
}

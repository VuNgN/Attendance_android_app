package com.vungn.attendancedemo.util.helper.auth

import com.microsoft.identity.client.IAccount
import java.lang.Exception

interface CurrentAccountCallback {
    fun onAccountLoaded()
    fun onError(exception: Exception)
}
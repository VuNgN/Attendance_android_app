package com.vungn.attendancedemo.vm.impl

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vungn.attendancedemo.util.LoginState
import com.vungn.attendancedemo.util.helper.auth.AuthenticationHelper
import com.vungn.attendancedemo.util.helper.auth.CurrentAccountCallback
import com.vungn.attendancedemo.vm.ActivityMainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityMainViewModelImpl @Inject constructor(
    private val authHelper: AuthenticationHelper
) : ActivityMainViewModel, ViewModel() {
    private val _loginState = MutableStateFlow(LoginState.CHECKING)
    override val loginState: StateFlow<LoginState>
        get() = _loginState

    override fun checkLoginState() {
        viewModelScope.launch(Dispatchers.IO) {
            authHelper.loadAccount(object : CurrentAccountCallback {
                override fun onAccountLoaded() {
                    authHelper.acquireTokenSilently().thenAccept {
                        val token = it.accessToken
                        viewModelScope.launch {
                            _loginState.emit(LoginState.LOGGED_IN)
                        }
                        Log.d(TAG, "onAccountLoaded: token: $token")
                    }.exceptionally {
                        it.printStackTrace()
                        null
                    }
                }

                override fun onError(exception: Exception) {
                    Log.d(TAG, "onError: ", exception)
                    viewModelScope.launch {
                        _loginState.emit(LoginState.NOT_LOGGED_IN)
                    }
                }
            })
        }
    }

    companion object {
        private val TAG = ActivityMainViewModelImpl::class.simpleName
    }
}
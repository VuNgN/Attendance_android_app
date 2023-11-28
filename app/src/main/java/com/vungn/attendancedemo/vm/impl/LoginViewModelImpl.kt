package com.vungn.attendancedemo.vm.impl

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsoft.graph.models.User
import com.vungn.attendancedemo.share.UserSharePreference
import com.vungn.attendancedemo.util.LoginState
import com.vungn.attendancedemo.util.helper.auth.AuthenticationHelper
import com.vungn.attendancedemo.util.helper.graph.GraphHelper
import com.vungn.attendancedemo.vm.LoginViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModelImpl @Inject constructor(
    private val authHelper: AuthenticationHelper,
    private val graphHelper: GraphHelper,
    private val userSharePreference: UserSharePreference
) : ViewModel(), LoginViewModel {
    private val _user: MutableStateFlow<User?> = MutableStateFlow(null)
    private val _loginState: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.CHECKING)
    override val user: StateFlow<User?>
        get() = _user
    override val loginState: StateFlow<LoginState>
        get() = _loginState

    override suspend fun login(activity: Activity) {
        withContext(Dispatchers.IO) {
            authHelper.acquireTokenInteractively(activity).thenAccept {
                val accessToken = it.accessToken
                Log.d(TAG, "login access token: $accessToken")
                viewModelScope.launch {
                    userSharePreference.save(accessToken)
                    _loginState.emit(LoginState.LOGGED_IN)
                }
            }.exceptionally {
                Log.e(TAG, "login exception: ", it)
                viewModelScope.launch {
                    _loginState.emit(LoginState.NOT_LOGGED_IN)
                }
                null
            }
        }
    }

    private suspend fun getUser() {
        withContext(Dispatchers.IO) {
            graphHelper.getUser().thenAccept { user ->
                viewModelScope.launch {
                    _user.emit(user)
                }
            }.exceptionally {
                Log.e(TAG, "getUser exception:", it)
                null
            }
        }
    }

    companion object {
        private const val TAG = "LoginViewModelImpl"
    }
}
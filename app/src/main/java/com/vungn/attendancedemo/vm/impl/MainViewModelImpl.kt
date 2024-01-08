package com.vungn.attendancedemo.vm.impl

import android.content.Context
import android.net.Network
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsoft.graph.models.User
import com.vungn.attendancedemo.repo.AttendAllRepo
import com.vungn.attendancedemo.repo.PushedResult
import com.vungn.attendancedemo.util.MessageError
import com.vungn.attendancedemo.vm.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModelImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val attendAllRepo: AttendAllRepo,
) : ViewModel(), MainViewModel {
    private val _isOnline        = checkIsOnline(context = context)
    private val _user            = MutableStateFlow<User?>(null)
    private val _loading         = MutableStateFlow(false)
    private val _isAllSynced     = MutableStateFlow(false)
    private val _isSyncedSuccess = MutableSharedFlow<Boolean>()
    private val _numOfNotSyncs   = MutableStateFlow(0)
    private val _syncMessage     = MutableStateFlow<MessageError?>(null)

    private val _onCheckSyncAll = object : OnCheckSyncAll {
        override fun onEmpty() {
            viewModelScope.launch(Dispatchers.Main) {
                _isAllSynced.emit(true)
            }
        }

        override fun onNotEmpty(size: Int) {
            viewModelScope.launch(Dispatchers.Main) {
                _isAllSynced.emit(false)
                _numOfNotSyncs.emit(size)
            }
        }
    }
    private val _onPushedResult = object : PushedResult<Boolean> {
        override fun onSuccess(result: Boolean?) {
            viewModelScope.launch(Dispatchers.Main) {
                _isSyncedSuccess.emit(true)
                delay(3000)
                _isSyncedSuccess.emit(false)
                _syncMessage.emit(null)
            }
        }

        override fun onError(error: String?) {
            viewModelScope.launch(Dispatchers.Main) {
                Log.e(TAG, "Push all data fail: $error")
                _isSyncedSuccess.emit(false)
                _syncMessage.emit(
                    MessageError(
                        error
                            ?: "Unknown error"
                    )
                )
                delay(3000)
                _syncMessage.emit(null)
            }
        }

        override fun onReleased() {
            viewModelScope.launch(Dispatchers.Main) {
                _loading.emit(false)
            }
        }
    }
    override val user: StateFlow<User?>
        get() = _user
    override val loading: StateFlow<Boolean>
        get() = _loading
    override val isOnline: StateFlow<Boolean>
        get() = _isOnline
    override val isAllSynced: StateFlow<Boolean>
        get() = _isAllSynced
    override val isSyncedSuccess: SharedFlow<Boolean>
        get() = _isSyncedSuccess
    override val numOfNotSyncs: StateFlow<Int>
        get() = _numOfNotSyncs
    override val syncMessage: StateFlow<MessageError?>
        get() = _syncMessage

    init {
        viewModelScope.launch {
            _isOnline.collect { isOnline ->
                if (isOnline) {
                    checkSyncAll()
                }
            }
        }
    }

    override fun syncAll() {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            attendAllRepo.execute(onPushedResult = _onPushedResult)
        }
    }

    private fun checkSyncAll() {
        viewModelScope.launch(Dispatchers.IO) {
            attendAllRepo.checkSyncAll(onCheckSyncAll = _onCheckSyncAll)
        }
    }

    private fun checkIsOnline(context: Context): StateFlow<Boolean> = callbackFlow {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val callback = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "onAvailable")
                launch { send(true) }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "onLost")
                launch { send(false) }
            }

            override fun onUnavailable() {
                Log.d(TAG, "onUnavailable")
                launch { send(false) }
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                Log.d(TAG, "onLosing")
                launch { send(false) }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    interface OnCheckSyncAll {
        fun onEmpty()
        fun onNotEmpty(size: Int)
    }

    companion object {
        private const val TAG = "MainViewModelImpl"
    }
}
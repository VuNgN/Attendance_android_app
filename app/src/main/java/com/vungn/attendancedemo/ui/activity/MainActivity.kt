package com.vungn.attendancedemo.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vungn.attendancedemo.ui.nav.MainNavHost
import com.vungn.attendancedemo.ui.theme.AttendanceDemoTheme
import com.vungn.attendancedemo.util.LoginState
import com.vungn.attendancedemo.vm.ActivityMainViewModel
import com.vungn.attendancedemo.vm.impl.ActivityMainViewModelImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: ActivityMainViewModel by viewModels<ActivityMainViewModelImpl>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            val loginState = vm.loginState.collectAsState()
            AttendanceDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    when (loginState.value) {
                        LoginState.CHECKING -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        LoginState.LOGGED_IN -> {
                            MainNavHost(modifier = Modifier.fillMaxSize())
                        }
                        LoginState.NOT_LOGGED_IN -> {
                            LaunchedEffect(key1 = true, block = {
                                scope.launch {
                                    goToAuth()
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (vm.loginState.value != LoginState.LOGGED_IN) {
            vm.checkLoginState()
        }
    }

    private fun goToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        this.startActivity(intent)
    }
}
package com.vungn.attendancedemo.ui.destination

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.vungn.attendancedemo.util.LoginState
import com.vungn.attendancedemo.vm.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun Login(
    viewModel: LoginViewModel,
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit = {}
) {
    val activity = LocalContext.current as Activity
    val scope = rememberCoroutineScope()

    val loginState = viewModel.loginState.collectAsState()

    LaunchedEffect(key1 = loginState.value) {
        if (loginState.value == LoginState.LOGGED_IN) {
            navigateToHome()
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = {
            scope.launch { viewModel.login(activity) }
        }) {
            Icon(
                imageVector = Icons.Rounded.Mail,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = "Login with Outlook")
        }
    }
}
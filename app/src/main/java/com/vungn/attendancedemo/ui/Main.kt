@file:OptIn(ExperimentalMaterial3Api::class)

package com.vungn.attendancedemo.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vungn.attendancedemo.vm.MainViewModel

@SuppressLint("MissingPermission")
@Composable
fun Main(
    modifier: Modifier = Modifier, navigateToCamera: () -> Unit = {}, viewModel: MainViewModel
) {
    val permissionList = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    )
    val context = LocalContext.current

    val loading = viewModel.loading.collectAsState()
    val isOnline = viewModel.isOnline.collectAsState()
    val isAllSynced = viewModel.isAllSynced.collectAsState()
    val isSyncedSuccess = viewModel.isSyncedSuccess.collectAsState(false)
    val numOfNotSyncs = viewModel.numOfNotSyncs.collectAsState()
    val syncMessage = viewModel.syncMessage.collectAsState()

    RequestPermissions(permissions = permissionList) {
        val alarmMgr = remember {
            context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
        val snackBarHostState = remember {
            SnackbarHostState()
        }

        LaunchedEffect(key1 = true, block = {
            viewModel.startAlarm(context, alarmMgr)
        })

        LaunchedEffect(key1 = syncMessage.value, block = {
            if (syncMessage.value != null) {
                snackBarHostState.showSnackbar(syncMessage.value!!.message)
            }
        })

        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (!isAllSynced.value && isOnline.value) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "You have ${numOfNotSyncs.value} not synced classes")
                        Button(onClick = { viewModel.syncAll() }, enabled = !loading.value) {
                            if (loading.value) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(25.dp)
                                )
                            } else {
                                Text(text = "Sync all")
                            }
                        }
                    }
                }
                if (isSyncedSuccess.value) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .background(MaterialTheme.colorScheme.primary),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 5.dp),
                            text = "All classes was synced",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Button(onClick = {
                    navigateToCamera()
                }) {
                    Text(text = "Camera")
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(if (isOnline.value) MaterialTheme.colorScheme.primary else Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isOnline.value) "You're online" else "You're offline",
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
package com.vungn.attendancedemo.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.vungn.attendancedemo.vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@SuppressLint("MissingPermission")
@Composable
fun Main(
    modifier: Modifier = Modifier, navigateToCamera: () -> Unit = {}, viewModel: MainViewModel
) {
    val permissionList =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.BLUETOOTH,
            )
        }

    val loading = viewModel.loading.collectAsState()
    val isOnline = viewModel.isOnline.collectAsState()
    val isAllSynced = viewModel.isAllSynced.collectAsState()
    val isSyncedSuccess = viewModel.isSyncedSuccess.collectAsState(false)
    val numOfNotSyncs = viewModel.numOfNotSyncs.collectAsState()
    val syncMessage = viewModel.syncMessage.collectAsState()
    val density = LocalDensity.current

    RequestPermissions(permissions = permissionList) {
        val snackBarHostState = remember { SnackbarHostState() }

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
                AnimatedVisibility(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                    visible = !isAllSynced.value && isOnline.value,
                    enter = slideInVertically {
                        // Slide in from 40 dp from the top.
                        with(density) { -40.dp.roundToPx() }
                    } + expandVertically(
                        // Expand from the top.
                        expandFrom = Alignment.Top
                    ) + fadeIn(
                        // Fade in with the initial alpha of 0.3f.
                        initialAlpha = 0.3f
                    ),
                    exit = slideOutVertically() + shrinkVertically() + fadeOut()) {
                    Row(
                        modifier = Modifier.padding(10.dp),
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
                AnimatedVisibility(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                    visible = isSyncedSuccess.value,
                    enter = slideInVertically {
                        // Slide in from 40 dp from the top.
                        with(density) { -40.dp.roundToPx() }
                    } + expandVertically(
                        // Expand from the top.
                        expandFrom = Alignment.Top
                    ) + fadeIn(
                        // Fade in with the initial alpha of 0.3f.
                        initialAlpha = 0.3f
                    ),
                    exit = slideOutVertically() + shrinkVertically() + fadeOut()) {
                    Row(
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary),
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
                AnimatedContent(modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                    targetState = isOnline.value,
                    contentAlignment = Alignment.BottomCenter,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInVertically { height -> height } + fadeIn() with slideOutVertically { height -> -height } + fadeOut()
                        } else {
                            slideInVertically { height -> -height } + fadeIn() with slideOutVertically { height -> height } + fadeOut()
                        }.using(
                            sizeTransform = SizeTransform(clip = false)
                        )
                    }) { isOnline ->
                    Box(
                        modifier = Modifier.background(if (isOnline) MaterialTheme.colorScheme.primary else Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isOnline) "You're online" else "You're offline",
                            modifier = Modifier.padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

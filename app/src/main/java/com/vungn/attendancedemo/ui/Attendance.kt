package com.vungn.attendancedemo.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.vungn.attendancedemo.vm.AttendanceViewModel
import com.vungn.attendancedemo.vm.impl.AttendanceViewModelImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun Attendance(
    modifier: Modifier = Modifier, viewModel: AttendanceViewModel, navigateBack: () -> Unit = {}
) {
    val permissions = arrayOf(android.Manifest.permission.CAMERA)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context)
    }
    val qrcode = viewModel.qrcode.collectAsState()
    val errorMessage = viewModel.error.collectAsState(initial = null)
    val overviewClass = viewModel.overviewClass.collectAsState()
    val attendanceState = viewModel.attendanceState.collectAsState()
    val isWifiEnable = viewModel.isWifiEnable.collectAsState()
    val isBluetoothEnable = viewModel.isBluetoothEnable.collectAsState()
    val isBluetoothScanning = viewModel.isBluetoothScanning.collectAsState()
    val enable = viewModel.isAttendanceEnable.collectAsState()
    val loading = remember { mutableStateOf(false) }
    val isAttended = remember { mutableStateOf(false) }
    val snackBarState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val bluetoothRequest =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {})
    val wifiRequest =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {})

    LaunchedEffect(key1 = true, block = {
        coroutineScope.launch(Dispatchers.IO) {
            viewModel.bindCamera(previewView,
                lifecycleOwner,
                with(density) { configuration.screenWidthDp.dp.roundToPx() })
        }
    })
    LaunchedEffect(key1 = qrcode.value, block = {
        if (qrcode.value != null) {
            viewModel.unbindCamera()
        }
    })
    LaunchedEffect(key1 = errorMessage.value, block = {
        if (errorMessage.value != null) {
            snackBarState.showSnackbar(message = errorMessage.value!!.message)
        }
    })
    LaunchedEffect(key1 = attendanceState.value, block = {
        if (isWifiEnable.value && isBluetoothEnable.value && qrcode.value != null) {
            when (attendanceState.value) {
                AttendanceViewModelImpl.AttendanceState.ATTENDING -> {
                    loading.value = true
                }

                AttendanceViewModelImpl.AttendanceState.SYNCED -> {
                    loading.value = false
                    isAttended.value = true
                }

                AttendanceViewModelImpl.AttendanceState.UNKNOWN -> {
                    loading.value = false
                }

                AttendanceViewModelImpl.AttendanceState.UN_SYNCED -> {
                    loading.value = false
                    isAttended.value = true
                }
            }
        }
    })
    RequestPermissions(permissions = permissions) {
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.primary) {
            Scaffold(modifier = Modifier,
                snackbarHost = { SnackbarHost(hostState = snackBarState) },
                topBar = {
                    TopAppBar(title = { Text(text = "Attendance") }, navigationIcon = {
                        IconButton(
                            onClick = navigateBack, modifier = Modifier
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back to home",
                            )
                        }
                    })
                }) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier
                            .size(configuration.screenWidthDp.dp)
                            .padding(20.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Room:")
                        AnimatedVisibility(overviewClass.value != null) {
                            Text(
                                "${overviewClass.value?.room}-${overviewClass.value?.area}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Wi-Fi:")
                        AnimatedContent(targetState = isWifiEnable.value) { isWifiEnable ->
                            if (isWifiEnable) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.Green
                                )
                            } else {
                                OutlinedButton(onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        val wifiIntent =
                                            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                                        wifiRequest.launch(wifiIntent)
                                    } else {
                                        viewModel.enableWifi()
                                    }
                                }) {
                                    Text(text = "Enable")
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Bluetooth:")
                        AnimatedContent(targetState = isBluetoothEnable.value) { isBluetoothEnable ->
                            if (isBluetoothEnable) {
                                AnimatedContent(targetState = isBluetoothScanning.value) { isBluetoothScanning ->
                                    if (isBluetoothScanning) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp), strokeWidth = 1.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.Green
                                        )
                                    }
                                }
                            } else {
                                OutlinedButton(onClick = {
                                    val bluetoothIntent =
                                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                    bluetoothRequest.launch(bluetoothIntent)
                                }) {
                                    Text(text = "Enable")
                                }
                            }
                        }
                    }
                    Button(
                        onClick = { viewModel.attend() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        enabled = enable.value,
                    ) {
                        AnimatedContent(targetState = loading.value) { loading ->
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp), strokeWidth = 1.dp
                                )
                            } else {
                                if (isAttended.value) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null
                                    )
                                } else {
                                    Text(text = "Attend")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

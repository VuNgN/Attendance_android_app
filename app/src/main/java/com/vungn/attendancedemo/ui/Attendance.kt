package com.vungn.attendancedemo.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.addCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vungn.attendancedemo.vm.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Attendance(
    modifier: Modifier = Modifier, viewModel: AttendanceViewModel, navigateBack: () -> Unit = {}
) {
    val overviewClazz = viewModel.overviewClazz.collectAsState(initial = null)
    val backPressDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val isAttended = viewModel.isAttended.collectAsState()
    val message = viewModel.message.collectAsState()
    val nearbyWifi = viewModel.nearbyWifi.collectAsState()
    val nearbyBluetooth = viewModel.nearbyBluetooth.collectAsState()
    val isWifiEnable = viewModel.isWifiEnable.collectAsState()
    val isBluetoothEnable = viewModel.isBluetoothEnable.collectAsState()
    val enable = remember {
        mutableStateOf(true)
    }
    val snackBarHostState = remember {
        SnackbarHostState()
    }
    val bluetoothRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {})
    val wifiRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {})

    DisposableEffect(key1 = backPressDispatcher, effect = {
        backPressDispatcher?.addCallback {
            navigateBack()
        }
        onDispose {}
    })

    LaunchedEffect(key1 = isAttended.value, block = {
        if (isAttended.value) {
            snackBarHostState.showSnackbar("Attend success")
        } else {
            enable.value = true
        }
    })

    LaunchedEffect(key1 = message.value, block = {
        if (message.value != null) {
            snackBarHostState.showSnackbar(message = message.value!!.message)
        }
    })

    Scaffold(modifier = modifier, topBar = {
        TopAppBar(modifier = Modifier, title = { Text(text = "Attendance") }, navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back to home"
                )
            }
        }, colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
        )
    }, snackbarHost = { SnackbarHost(hostState = snackBarHostState) }, bottomBar = {
        Button(
            onClick = {
                enable.value = false
                viewModel.attend()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enable.value,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAttended.value) Color.Green else MaterialTheme.colorScheme.primary,
            )
        ) {
            if (isAttended.value) Icon(
                imageVector = Icons.Rounded.CheckCircle, contentDescription = "Điểm danh thành công"
            ) else {
                Text(text = "Điểm danh")
            }
        }
    }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Room: ${overviewClazz.value?.room} - ${overviewClazz.value?.area}")
                Text(text = "Token: ${overviewClazz.value?.token}")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nearby Wi-Fi", style = MaterialTheme.typography.titleMedium
                    )
                    if (isWifiEnable.value) {
                        IconButton(onClick = { viewModel.refreshWifi() }) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh Wi-Fi"
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val wifiIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                                wifiRequest.launch(wifiIntent)
                            } else {
                                viewModel.enableWifi()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Wifi,
                                contentDescription = "Enable Wi-Fi"
                            )
                        }
                    }
                }
                if (isWifiEnable.value) {
                    LazyColumn(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        content = {
                            items(nearbyWifi.value) { wifi ->
                                Text(text = wifi.name)
                            }
                        })
                } else {
                    Text(text = "Wi-Fi need to be enabled to attend")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nearby Bluetooth", style = MaterialTheme.typography.titleMedium
                    )
                    if (isBluetoothEnable.value) {
                        IconButton(onClick = { viewModel.refreshBluetooth() }) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh Bluetooth"
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            val bluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            bluetoothRequest.launch(bluetoothIntent)
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Bluetooth,
                                contentDescription = "Enable Bluetooth"
                            )
                        }
                    }
                }
                if (isBluetoothEnable.value) {
                    LazyColumn(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        content = {
                            items(nearbyBluetooth.value.toList()) { bluetooth ->
                                Text(text = bluetooth.name ?: "<UNKNOWN-Name>")
                            }
                        })
                } else {
                    Text(text = "Bluetooth need to be enabled to attend")
                }
            }
        }
    }
}
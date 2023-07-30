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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
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
    val isWifiEnable = viewModel.isWifiEnable.collectAsState()
    val isBluetoothEnable = viewModel.isBluetoothEnable.collectAsState()
    val enable = remember {
        mutableStateOf(true)
    }
    val snackBarHostState = remember {
        SnackbarHostState()
    }
    val bluetoothRequest =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {})
    val wifiRequest =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(),
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

    LaunchedEffect(key1 = isWifiEnable.value, key2 = isBluetoothEnable.value, block = {
        if (!isAttended.value) {
            enable.value = isWifiEnable.value && isBluetoothEnable.value
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
                imageVector = Icons.Rounded.CheckCircle, contentDescription = "Attend success"
            ) else {
                Text(text = "Attend")
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
                    .align(Alignment.TopStart)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Room: ${overviewClazz.value?.room} - ${overviewClazz.value?.area}")
                Text(text = "This feature is only available when you enable: ")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wi-Fi", style = MaterialTheme.typography.titleMedium
                    )
                    if (isWifiEnable.value) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.Green
                        )
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
                if (!isWifiEnable.value) {
                    Text(text = "Wi-Fi need to be enabled to attend")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bluetooth", style = MaterialTheme.typography.titleMedium
                    )
                    if (isBluetoothEnable.value) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.Green
                        )
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
                if (!isBluetoothEnable.value) {
                    Text(text = "Bluetooth need to be enabled to attend")
                }
            }
        }
    }
}

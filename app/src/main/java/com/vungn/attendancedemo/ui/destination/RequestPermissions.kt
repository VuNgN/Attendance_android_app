package com.vungn.attendancedemo.ui.destination

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.Manifest.permission.BLUETOOTH
import android.Manifest.permission.BLUETOOTH_ADMIN
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.Manifest.permission.CAMERA
import android.Manifest.permission.CHANGE_WIFI_STATE
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.vungn.attendancedemo.R
import com.vungn.attendancedemo.util.DateUtil
import com.vungn.attendancedemo.util.Permissions
import com.vungn.attendancedemo.util.Permissions.LOCATION
import com.vungn.attendancedemo.util.Permissions.NEARBY_DEVICES
import com.vungn.attendancedemo.util.Permissions.NOTIFICATION
import com.vungn.attendancedemo.util.getRequestAutoStartIntent
import com.vungn.attendancedemo.util.isAutoStartSupported
import com.vungn.attendancedemo.util.manufacturers
import com.vungn.attendancedemo.worker.BackgroundCheckWorker
import com.vungn.attendancedemo.worker.BackgroundCheckWorker.Companion.AUTO_START_PREF_KEY
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun RequestPermissions(
    modifier: Modifier = Modifier, permissions: Array<String>, content: @Composable () -> Unit,
) {
    val permissionList = remember {
        mutableStateMapOf(
            Pair(Permissions.CAMERA.title, false),
            Pair(LOCATION.title, false),
            Pair(NEARBY_DEVICES.title, false),
            Pair(NOTIFICATION.title, false)
        )
    }
    val loading = remember { mutableStateOf(true) }
    val isGranted = remember { mutableStateOf(false) }
    val isOpenDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val pref = remember {
        context.getSharedPreferences("auto_start_pref", Context.MODE_PRIVATE)
    }
    val checkPermissions =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissions ->
                isGranted.value = permissions.all { it.value }
                if (permissions[CAMERA] == true) {
                    permissionList[Permissions.CAMERA.title] = true
                }
                if (permissions[ACCESS_COARSE_LOCATION] == true
                    && permissions[ACCESS_FINE_LOCATION] == true) {
                    permissionList[LOCATION.title] = true
                }
                if (permissions[ACCESS_WIFI_STATE] == true
                    && permissions[CHANGE_WIFI_STATE] == true
                    && permissions[BLUETOOTH] == true
                    && permissions[BLUETOOTH_SCAN] == true
                    && permissions[BLUETOOTH_ADMIN] == true
                    && permissions[BLUETOOTH_CONNECT] == true) {
                    permissionList[NEARBY_DEVICES.title] = true
                }
                if (permissions[POST_NOTIFICATIONS] == true) {
                    permissionList[NOTIFICATION.title] = true
                }
                loading.value = false
            })

    val requestAutoStart = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {})

    LaunchedEffect(key1 = true, block = {
        checkPermissions.launch(permissions)
    })
    LaunchedEffect(key1 = true, block = {
        if (manufacturers.isAutoStartSupported()) {
            val lastUpdate = pref.getString(AUTO_START_PREF_KEY, "")
            val updated = if (lastUpdate == null || lastUpdate == "") {
                val editor = pref.edit()
                editor.putString(AUTO_START_PREF_KEY, Calendar.getInstance().time.toString())
                editor.apply()
                false
            } else DateUtil.minAgo(
                SimpleDateFormat(
                    "EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH
                ).parse(lastUpdate) !!
            ) <= 20
            if (! updated) {
                isOpenDialog.value = true
            }
            val backgroundCheckRequest =
                PeriodicWorkRequestBuilder<BackgroundCheckWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "background_check", ExistingPeriodicWorkPolicy.KEEP, backgroundCheckRequest
            )
        }
    })

    if (loading.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        if (isGranted.value) {
            content()
            if (isOpenDialog.value) {
                AlertDialog(onDismissRequest = { // Dismiss the dialog when the user clicks outside the dialog or on the back
                    // button. If you want to disable that functionality, simply use an empty
                    // onDismissRequest.
                }, title = {
                    Text(text = "Background Services Required for Attendance")
                }, text = {
                    Text(text = "Services should be allowed to run in the background, otherwise you can miss your attendance")
                }, confirmButton = {
                    TextButton(onClick = {
                        try {
                            val editor = pref.edit()
                            editor.putString(
                                AUTO_START_PREF_KEY, Calendar.getInstance().time.toString()
                            )
                            editor.apply()
                            val intent = Intent().getRequestAutoStartIntent()
                            requestAutoStart.launch(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        isOpenDialog.value = false
                    }) {
                        Text("Yes")
                    }
                }, dismissButton = {
                    TextButton(onClick = {
                        isOpenDialog.value = false
                    }) {
                        Text("Dismiss")
                    }
                })
            }
        } else {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier.size(width = 26.dp, height = 24.dp),
                        painter = painterResource(id = R.drawable.logo_thuy_loi),
                        contentDescription = null
                    )
                    Text(style = MaterialTheme.typography.titleMedium, text = "Đại học Thủy Lợi")
                }
                LazyColumn(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            style = MaterialTheme.typography.titleMedium,
                            text = "Các quyền cần được cấp"
                        )
                    }
                    item {
                        permissionList.entries.forEach {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = it.key)
                                    Icon(
                                        imageVector = if (it.value) Icons.Rounded.Check else Icons.Rounded.Close,
                                        tint = if (it.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        contentDescription = null
                                    )
                                }
                                Divider(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                    item {
                        Button(modifier = Modifier.fillMaxWidth(),
                            onClick = { checkPermissions.launch(permissions) }) {
                            Text(text = "Cấp quyền")
                        }
                    }
                }
            }
        }
    }
}

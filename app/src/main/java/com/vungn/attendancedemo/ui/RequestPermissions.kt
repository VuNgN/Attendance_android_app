package com.vungn.attendancedemo.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.vungn.attendancedemo.util.DateUtil
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
    val isGranted = remember { mutableStateOf(false) }
    val isOpenDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val pref = remember {
        context.getSharedPreferences("auto_start_pref", Context.MODE_PRIVATE)
    }
    val checkPermissions =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissionList -> isGranted.value = permissionList.all { it.value } })

    val requestAutoStart =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(),
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
                ).parse(lastUpdate)!!
            ) <= 20
            if (!updated) {
                isOpenDialog.value = true
            }
            val backgroundCheckRequest =
                PeriodicWorkRequestBuilder<BackgroundCheckWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "background_check", ExistingPeriodicWorkPolicy.KEEP, backgroundCheckRequest
            )
        }
    })

    if (isGranted.value) {
        content()
        if (isOpenDialog.value) {
            AlertDialog(onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
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
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Text(text = "You need to grant all follow permissions")
            }
            items(permissions) {
                Text(text = it.removePrefix("android.permission."))
            }
            item {
                Button(onClick = { checkPermissions.launch(permissions) }) {
                    Text(text = "Request Permissions")
                }
            }
        }
    }
}

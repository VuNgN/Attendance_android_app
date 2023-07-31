package com.vungn.attendancedemo.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun RequestPermissions(
    modifier: Modifier = Modifier, permissions: Array<String>, content: @Composable () -> Unit,
) {
    val isGranted = remember { mutableStateOf(false) }
    val checkPermissions =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissionList -> isGranted.value = permissionList.all { it.value } })

    LaunchedEffect(key1 = true, block = {
        checkPermissions.launch(permissions)
    })

    if (isGranted.value) {
        content()
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

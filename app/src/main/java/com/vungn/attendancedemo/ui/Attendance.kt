package com.vungn.attendancedemo.ui

import androidx.activity.addCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
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
    val enable = remember {
        mutableStateOf(true)
    }
    val snackBarHostState = remember {
        SnackbarHostState()
    }

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
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Room: ${overviewClazz.value?.room} - ${overviewClazz.value?.area}")
                Text(text = "Token: ${overviewClazz.value?.token}")
            }
        }
    }
}
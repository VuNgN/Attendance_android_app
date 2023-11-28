package com.vungn.attendancedemo.ui.destination

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.vungn.attendancedemo.vm.CameraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Camera(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel,
    navigateBack: () -> Unit = {},
    navigateToAttendance: () -> Unit = {}
) {
    val permissions = arrayOf(android.Manifest.permission.CAMERA)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context)
    }
    val qrcode = viewModel.qrcode.collectAsState(null)
    val errorMessage = viewModel.error.collectAsState(initial = null)
    val snackBarState = remember { SnackbarHostState() }
    LaunchedEffect(key1 = true, block = {
        viewModel.bindCamera(previewView, lifecycleOwner)
    })
    LaunchedEffect(key1 = qrcode.value, block = {
        if (qrcode.value != null) {
            viewModel.unbindCamera()
            navigateToAttendance()
        }
    })
    LaunchedEffect(key1 = errorMessage.value, block = {
        if (errorMessage.value != null) {
            snackBarState.showSnackbar(message = errorMessage.value!!.message)
        }
    })
    RequestPermissions(permissions = permissions) {
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.primary) {
            Scaffold(
                modifier = Modifier,
                snackbarHost = { SnackbarHost(hostState = snackBarState) }) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = paddingValues)
                ) {
                    AndroidView(
                        factory = { previewView }, modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = navigateBack, modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back to home",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
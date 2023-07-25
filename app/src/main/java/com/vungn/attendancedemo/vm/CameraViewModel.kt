package com.vungn.attendancedemo.vm

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.vungn.attendancedemo.util.MessageError
import kotlinx.coroutines.flow.StateFlow

interface CameraViewModel {
    val qrcode: StateFlow<String?>
    val error: StateFlow<MessageError?>
    fun bindCamera(previewView: PreviewView, lifecycle: LifecycleOwner)
    fun unbindCamera()
}
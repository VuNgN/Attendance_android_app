package com.vungn.attendancedemo.vm.impl

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.common.Barcode
import com.vungn.attendancedemo.util.MessageError
import com.vungn.attendancedemo.util.QrCodeAnalyzer
import com.vungn.attendancedemo.vm.CameraViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModelImpl @Inject constructor(private val _qrcode: MutableStateFlow<String?>) :
    ViewModel(), CameraViewModel {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var camera: Camera
    private val _error: MutableStateFlow<MessageError?> = MutableStateFlow(null)
    override val qrcode: StateFlow<String?>
        get() = _qrcode
    override val error: StateFlow<MessageError?>
        get() = _error

    override fun bindCamera(previewView: PreviewView, lifecycle: LifecycleOwner) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(previewView.context), QrCodeAnalyzer(scannedResult)
            )
            camera =
                cameraProvider.bindToLifecycle(lifecycle, cameraSelector, preview, imageAnalysis)
        }, ContextCompat.getMainExecutor(previewView.context))
    }

    override fun unbindCamera() {
        cameraProviderFuture.get().unbindAll()
    }

    private val scannedResult = object : QrCodeAnalyzer.ScannedResult {
        override fun onScanned(barcode: Barcode) {
            Log.d(TAG, "onScanned: $barcode")
            viewModelScope.launch {
                _qrcode.emit(barcode.rawValue)
                _error.emit(null)
            }
        }

        override fun onError(message: MessageError) {
            Log.e(TAG, "onError: ${message.message}")
            viewModelScope.launch {
                _qrcode.emit(null)
                _error.emit(message)
            }
        }
    }

    companion object {
        private val TAG = CameraViewModelImpl::class.simpleName
    }
}
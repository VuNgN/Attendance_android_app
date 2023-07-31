package com.vungn.attendancedemo.vm.impl

import android.app.Notification
import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.common.Barcode
import com.vungn.attendancedemo.MyApplication
import com.vungn.attendancedemo.R
import com.vungn.attendancedemo.model.NearbyBluetooth
import com.vungn.attendancedemo.model.NearbyWifi
import com.vungn.attendancedemo.model.OverviewClass
import com.vungn.attendancedemo.repo.AttendRepo
import com.vungn.attendancedemo.repo.PushedResult
import com.vungn.attendancedemo.repo.SavedResult
import com.vungn.attendancedemo.util.BluetoothHelper
import com.vungn.attendancedemo.util.Message
import com.vungn.attendancedemo.util.QrCodeAnalyzer
import com.vungn.attendancedemo.util.WifiHelper
import com.vungn.attendancedemo.util.toClazz
import com.vungn.attendancedemo.util.toOverviewClass
import com.vungn.attendancedemo.vm.AttendanceViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@Suppress("DEPRECATION")
@HiltViewModel
class AttendanceViewModelImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val attendRepo: AttendRepo,
    private val wifiHelper: WifiHelper,
    private val bluetoothHelper: BluetoothHelper
) : ViewModel(), AttendanceViewModel {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var camera: Camera
    private val _qrcode: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _overviewClass: MutableStateFlow<OverviewClass?> = MutableStateFlow(null)
    private val _error: MutableStateFlow<Message?> = MutableStateFlow(null)
    private val _isSaved: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _attendanceState: MutableStateFlow<AttendanceState> =
        MutableStateFlow(AttendanceState.UNKNOWN)
    private val _message: MutableStateFlow<Message?> = MutableStateFlow(null)
    private val _isWifiEnable: StateFlow<Boolean> = wifiHelper.isEnable
    private val _isBluetoothEnable: StateFlow<Boolean> = bluetoothHelper.isEnable
    private val _isBluetoothScanning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _nearbyWifi: StateFlow<List<NearbyWifi>> = wifiHelper.nearbyWifi
    private val _nearbyBluetooth: StateFlow<Set<NearbyBluetooth>> = bluetoothHelper.scannedDevices
    private val _isAttendanceEnable: StateFlow<Boolean> = combine(
        _isWifiEnable, _isBluetoothEnable, _isBluetoothScanning, _qrcode, _attendanceState
    ) { isWifiEnable, isBluetoothEnable, isBluetoothScanning, qrcode, attendanceState ->
        isWifiEnable && isBluetoothEnable && !isBluetoothScanning && qrcode != null && attendanceState == AttendanceState.UNKNOWN
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    override val qrcode: StateFlow<String?>
        get() = _qrcode
    override val isWifiEnable: StateFlow<Boolean>
        get() = _isWifiEnable
    override val isBluetoothEnable: StateFlow<Boolean>
        get() = _isBluetoothEnable
    override val isBluetoothScanning: StateFlow<Boolean>
        get() = _isBluetoothScanning
    override val overviewClass: StateFlow<OverviewClass?>
        get() = _overviewClass
    override val attendanceState: StateFlow<AttendanceState>
        get() = _attendanceState
    override val error: StateFlow<Message?>
        get() = _error
    override val isAttendanceEnable: StateFlow<Boolean>
        get() = _isAttendanceEnable

    init {
        viewModelScope.launch {
            _isWifiEnable.collect { isEnable ->
                if (isEnable) {
                    wifiHelper.scanWifi()
                }
            }
        }
        viewModelScope.launch {
            _isBluetoothEnable.collect { isEnable ->
                if (isEnable) {
                    _isBluetoothScanning.emit(true)
                    bluetoothHelper.startDiscovery()
                    delay(12000)
                    _isBluetoothScanning.emit(false)
                }
            }
        }
    }

    override fun bindCamera(previewView: PreviewView, lifecycle: LifecycleOwner, width: Int) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().setTargetResolution(Size(width, width)).build()
            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val imageAnalysis = ImageAnalysis.Builder().setTargetResolution(Size(width, width))
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

    override fun attend() {
        viewModelScope.launch {
            _attendanceState.emit(AttendanceState.ATTENDING)
        }
        val wifiAddresses = _nearbyWifi.value.map { it.address }
        val bluetoothAddresses = _nearbyBluetooth.value.map { it.address }
        if (_overviewClass.value != null) {
            val clazz = _overviewClass.value!!.toClazz(
                wifiAddresses, bluetoothAddresses, Calendar.getInstance().time
            )
            viewModelScope.launch(Dispatchers.IO) {
                attendRepo.execute(clazz = clazz, onPushedResult = object : PushedResult<Unit> {
                    override fun onSuccess(result: Unit?) {
                        viewModelScope.launch {
                            pushNotification("You're attended successfully")
                            _attendanceState.emit(AttendanceState.SYNCED)
                        }
                    }

                    override fun onError(error: String?) {
                        viewModelScope.launch {
                            pushNotification("You're attended offline, please sync later")
                            Log.w(TAG, "Push to server false: $error")
                            _attendanceState.emit(AttendanceState.UN_SYNCED)
                        }
                    }

                    override fun onReleased() {
                        // Do nothing
                    }

                }, onSavedResult = object : SavedResult {
                    override fun onSuccess() {
                        viewModelScope.launch {
                            _isSaved.emit(true)
                        }
                    }

                    override fun onError(error: String?) {
                        viewModelScope.launch {
                            Log.e(TAG, "Save to local false: $error")
                            _message.emit(Message("Attendance failed"))
                            _isSaved.emit(false)
                        }
                    }
                })
            }
        }
    }

    override fun enableWifi() {
        wifiHelper.enable()
    }

    private val scannedResult = object : QrCodeAnalyzer.ScannedResult {
        override fun onScanned(barcode: Barcode) {
            Log.d(TAG, "onScanned: $barcode")
            viewModelScope.launch {
                _qrcode.emit(barcode.rawValue)
                _overviewClass.emit(barcode.rawValue?.toOverviewClass())
                _error.emit(null)
            }
        }

        override fun onError(message: Message) {
            Log.e(TAG, "onError: ${message.message}")
            viewModelScope.launch {
                _qrcode.emit(null)
                _error.emit(message)
            }
        }
    }

    override fun onCleared() {
        wifiHelper.release()
        bluetoothHelper.release()
    }

    private fun pushNotification(message: String) {
        val notification: Notification =
            NotificationCompat.Builder(context, MyApplication.CHANNEL_ID)
                .setContentTitle("Attendance").setContentText(message).setStyle(
                    NotificationCompat.BigTextStyle().bigText(message)
                ).setSmallIcon(R.drawable.ic_launcher_foreground).setTicker(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH).setOngoing(false).build()
        with(NotificationManagerCompat.from(context)) {
            notify(1, notification)
        }
    }

    enum class AttendanceState {
        SYNCED, UN_SYNCED, ATTENDING, UNKNOWN
    }

    companion object {
        private val TAG = AttendanceViewModelImpl::class.simpleName
    }
}

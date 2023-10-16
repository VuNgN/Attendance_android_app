package com.vungn.attendancedemo.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.TimeUnit

class QrCodeAnalyzer(private val scannedResult: ScannedResult) : ImageAnalysis.Analyzer {
    private val lastAnalyzedTimeStamp = 0L
    private lateinit var scanError: MessageError
    private val looper = Handler(Looper.getMainLooper())

    init {
        looper.post(object : Runnable {
            override fun run() {
                scanError = MessageError(message = "QR code is not correct")
                looper.postDelayed(this, 3000)
            }
        })
    }

    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimeStamp >= TimeUnit.SECONDS.toMillis(1)) {
            image.image?.let { imageToAnalyze ->
                val option =
                    BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                val barcodeScanner = BarcodeScanning.getClient(option)
                val imageToProcess =
                    InputImage.fromMediaImage(imageToAnalyze, image.imageInfo.rotationDegrees)

                barcodeScanner.process(imageToProcess).addOnSuccessListener { barcodes ->
                    barcodes.forEach { barcode ->
                        barcode.rawValue?.let { barcodeValue ->
                            val isCorrect = barcodeValue.contains("mytlu://")
                            if (isCorrect) {
                                try {
                                    barcode.rawValue?.toOverviewClass()
                                    Log.d(TAG, "analyze QR code: $barcodeValue is correct")
                                    scannedResult.onScanned(barcode)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Analyze error: ${e.message}")
                                    scannedResult.onError(scanError)
                                }
                            } else {
                                scannedResult.onError(scanError)
                            }
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "BarcodeAnalyzer: somethings wrong -> $exception")
                }.addOnCompleteListener {
                    image.close()
                }
            }
        }
    }

    companion object {
        private val TAG = QrCodeAnalyzer::class.simpleName
    }

    interface ScannedResult {
        fun onScanned(barcode: Barcode)
        fun onError(message: MessageError)
    }
}
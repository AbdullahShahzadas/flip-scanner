package com.flipscanner.app.ui.scan

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.flipscanner.app.data.ScanSession
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val session: ScanSession,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    var flashOn by mutableStateOf(false)
        private set
    var analyzing by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var imageCapture: ImageCapture? = null

    fun onCaptureReady(capture: ImageCapture) {
        imageCapture = capture
    }

    fun toggleFlash() {
        flashOn = !flashOn
    }

    fun takePhoto(onDone: () -> Unit) {
        val capture = imageCapture ?: return
        analyzing = true
        error = null
        val file = File(appContext.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        val output = ImageCapture.OutputFileOptions.Builder(file).build()
        capture.takePicture(
            output,
            ContextCompat.getMainExecutor(appContext),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                    session.lastPhotoPath = file.absolutePath
                    analyzing = false
                    onDone()
                }

                override fun onError(e: ImageCaptureException) {
                    analyzing = false
                    error = "Capture failed: ${e.message}"
                }
            }
        )
    }

    fun useGalleryImage(uri: Uri, onDone: () -> Unit) {
        session.lastPhotoPath = uri.toString()
        onDone()
    }
}

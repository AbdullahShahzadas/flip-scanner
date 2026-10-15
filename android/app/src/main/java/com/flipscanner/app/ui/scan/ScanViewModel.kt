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

    companion object {
        const val MAX_PHOTOS = 3
    }

    var flashOn by mutableStateOf(false)
        private set
    var capturing by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    /** Observable snapshot of the session's photo paths for the UI. */
    var photoPaths by mutableStateOf<List<String>>(emptyList())
        private set

    val photoCount: Int
        get() = photoPaths.size

    val canAddMore: Boolean
        get() = photoCount < MAX_PHOTOS

    private var imageCapture: ImageCapture? = null

    init {
        refreshPhotos()
    }

    fun onCaptureReady(capture: ImageCapture) {
        imageCapture = capture
    }

    fun toggleFlash() {
        flashOn = !flashOn
    }

    /** Captures a photo and appends it to the session (max [MAX_PHOTOS]). */
    fun takePhoto() {
        if (!canAddMore) {
            error = "Maximum $MAX_PHOTOS photos per scan."
            return
        }
        val capture = imageCapture ?: return
        capturing = true
        error = null
        val file = File(appContext.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        val output = ImageCapture.OutputFileOptions.Builder(file).build()
        capture.takePicture(
            output,
            ContextCompat.getMainExecutor(appContext),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                    session.photoPaths.add(file.absolutePath)
                    refreshPhotos()
                    capturing = false
                }

                override fun onError(e: ImageCaptureException) {
                    capturing = false
                    error = "Capture failed: ${e.message}"
                }
            }
        )
    }

    /**
     * Copies a gallery content URI into a temp file in cacheDir, then
     * appends the file path to the session so it can be uploaded.
     */
    fun useGalleryImage(uri: Uri) {
        if (!canAddMore) {
            error = "Maximum $MAX_PHOTOS photos per scan."
            return
        }
        error = null
        try {
            val file = File(appContext.cacheDir, "gallery_${System.currentTimeMillis()}.jpg")
            val copied = appContext.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
                true
            } ?: false
            if (!copied || !file.exists()) {
                error = "Couldn't read that image."
                return
            }
            session.photoPaths.add(file.absolutePath)
            refreshPhotos()
        } catch (e: Exception) {
            error = "Couldn't import that image: ${e.message}"
        }
    }

    fun removePhoto(index: Int) {
        if (index in session.photoPaths.indices) {
            val removed = session.photoPaths.removeAt(index)
            try {
                File(removed).delete()
            } catch (_: Exception) {
                // Best effort; the path is already removed from the session.
            }
            refreshPhotos()
        }
    }

    fun clearPhotos() {
        session.photoPaths.forEach { path ->
            try {
                File(path).delete()
            } catch (_: Exception) {
                // Best effort cleanup.
            }
        }
        session.clearPhotos()
        refreshPhotos()
    }

    private fun refreshPhotos() {
        photoPaths = session.photoPaths.toList()
    }
}

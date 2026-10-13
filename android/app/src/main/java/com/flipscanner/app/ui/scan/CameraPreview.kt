package com.flipscanner.app.ui.scan

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * CameraX preview. Binds once; flash toggles via torch without rebinding.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    flashOn: Boolean,
    onCaptureReady: (ImageCapture) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(flashOn) {
        camera?.cameraControl?.enableTorch(flashOn)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }.also { pv ->
                val executor = ContextCompat.getMainExecutor(ctx)
                ProcessCameraProvider.getInstance(ctx).addListener({
                    val provider = ProcessCameraProvider.getInstance(ctx).get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(pv.surfaceProvider)
                    }
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    provider.unbindAll()
                    camera = provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        capture
                    )
                    camera?.cameraControl?.enableTorch(flashOn)
                    onCaptureReady(capture)
                }, executor)
            }
        },
        update = {}
    )
}

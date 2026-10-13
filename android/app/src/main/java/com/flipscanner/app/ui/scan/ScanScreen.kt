package com.flipscanner.app.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.flipscanner.app.ui.theme.Emerald

/**
 * Camera scanner: live preview, guide frame, flash toggle, gallery import.
 */
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    vm: ScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { vm.useGalleryImage(it) { onDone() } } }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasPermission) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                flashOn = vm.flashOn,
                onCaptureReady = vm::onCaptureReady
            )

            // Guide frame
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(300.dp, 220.dp)
                    .border(2.dp, Emerald, RoundedCornerShape(16.dp))
            )

            // Top controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack) { Text("Close", color = Color.White) }
                TextButton(onClick = { vm.toggleFlash() }) {
                    Text(if (vm.flashOn) "Flash on" else "Flash off", color = Color.White)
                }
            }

            // Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Text("Gallery", color = Color.White)
                }
                Button(
                    onClick = { vm.takePhoto(onDone) },
                    shape = CircleShape,
                    modifier = Modifier.size(76.dp)
                ) {
                    Text("Scan")
                }
                TextButton(onClick = { /* 3-photo mode comes in Part 3 */ }) {
                    Text("1/3", color = Color.White)
                }
            }

            vm.error?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Camera permission is needed to scan items.", color = Color.White)
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant permission")
                }
                TextButton(onClick = onBack) { Text("Back", color = Color.White) }
            }
        }

        if (vm.analyzing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Emerald)
                    Text(
                        "Analyzing...",
                        color = Color.White,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}

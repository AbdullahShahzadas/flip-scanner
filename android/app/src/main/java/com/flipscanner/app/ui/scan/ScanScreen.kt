package com.flipscanner.app.ui.scan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Camera scanner screen. Part 2 adds CameraX preview, guide frame,
 * flash toggle and gallery import.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(onBack: () -> Unit, onDone: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Scan item") }) }
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera scanner comes in Part 2")
        }
    }
}

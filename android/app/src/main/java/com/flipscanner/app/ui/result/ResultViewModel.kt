package com.flipscanner.app.ui.result

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flipscanner.app.data.ScanSession
import com.flipscanner.app.data.model.ScanResult
import com.flipscanner.app.data.model.mockScanResult
import com.flipscanner.app.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val session: ScanSession,
    private val repository: ScanRepository
) : ViewModel() {

    var result by mutableStateOf<ScanResult?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var usingDemoData by mutableStateOf(false)
        private set

    var buyPrice by mutableStateOf("")

    init {
        loadResult()
    }

    fun retry() {
        loadResult()
    }

    private fun loadResult() {
        val photoPath = session.lastPhotoPath
        if (photoPath.isNullOrBlank()) {
            error = "No photo found. Please scan again."
            isLoading = false
            return
        }

        viewModelScope.launch {
            isLoading = true
            error = null
            usingDemoData = false

            // Gallery URIs can't be sent as files; fall back to demo data.
            val file = try {
                File(photoPath).takeIf { it.exists() && it.isFile }
            } catch (_: Exception) {
                null
            }

            if (file == null) {
                // Demo fallback for gallery URIs in v1.
                result = mockScanResult(photoPath)
                usingDemoData = true
                isLoading = false
                return@launch
            }

            when (val r = repository.scanPhotos(listOf(file))) {
                is ScanRepository.Result.Success -> {
                    result = r.scan
                }
                is ScanRepository.Result.Error -> {
                    // Fall back to demo data so the UI still works offline.
                    result = mockScanResult(photoPath)
                    usingDemoData = true
                    error = r.message
                }
            }
            isLoading = false
        }
    }

    private val buy: Double
        get() = buyPrice.toDoubleOrNull() ?: 0.0

    // Estimate: 13% marketplace fees + $8 shipping on the typical price
    private val fees: Double
        get() = (result?.priceRange?.mid ?: 0.0) * 0.13
    private val shipping = 8.0

    val netProfit: Double
        get() = (result?.priceRange?.mid ?: 0.0) - fees - shipping - buy

    val verdict: String
        get() = when {
            buy <= 0.0 -> "Enter what you'd pay"
            netProfit >= 20.0 -> "Great flip"
            netProfit > 0.0 -> "OK flip"
            else -> "Skip"
        }
}

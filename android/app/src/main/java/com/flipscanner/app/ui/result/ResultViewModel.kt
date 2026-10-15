package com.flipscanner.app.ui.result

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flipscanner.app.data.ScanSession
import com.flipscanner.app.data.model.ScanResult
import com.flipscanner.app.data.model.mockScanResult
import com.flipscanner.app.data.repository.InventoryRepository
import com.flipscanner.app.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val session: ScanSession,
    private val repository: ScanRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    var result by mutableStateOf<ScanResult?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var usingDemoData by mutableStateOf(false)
        private set
    var saveMessage by mutableStateOf<String?>(null)
        private set

    var buyPrice by mutableStateOf("")

    init {
        loadResult()
    }

    fun retry() {
        loadResult()
    }

    private fun loadResult() {
        val paths = session.photoPaths
        if (paths.isEmpty()) {
            error = "No photos found. Please scan again."
            isLoading = false
            return
        }

        viewModelScope.launch {
            isLoading = true
            error = null
            usingDemoData = false
            saveMessage = null

            // Convert every session photo path to an existing file.
            val files = paths.mapNotNull { path ->
                try {
                    File(path).takeIf { it.exists() && it.isFile }
                } catch (_: Exception) {
                    null
                }
            }

            if (files.isEmpty()) {
                // Demo fallback when no usable files exist.
                result = mockScanResult(paths.lastOrNull())
                usingDemoData = true
                isLoading = false
                return@launch
            }

            when (val r = repository.scanPhotos(files)) {
                is ScanRepository.Result.Success -> {
                    result = r.scan
                }
                is ScanRepository.Result.Error -> {
                    // Fall back to demo data so the UI still works offline.
                    result = mockScanResult(files.firstOrNull()?.absolutePath)
                    usingDemoData = true
                    error = r.message
                }
            }
            isLoading = false
        }
    }

    /** Saves the current scan result into the Room inventory. */
    fun saveToInventory() {
        val r = result ?: return
        viewModelScope.launch {
            inventoryRepository.addItem(
                itemName = r.itemName,
                brand = r.brand,
                category = r.category,
                condition = r.condition,
                photoPath = r.photoPath,
                buyPrice = buyPrice.toDoubleOrNull() ?: 0.0
            )
            saveMessage = "Saved to inventory."
        }
    }

    fun dismissSaveMessage() {
        saveMessage = null
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

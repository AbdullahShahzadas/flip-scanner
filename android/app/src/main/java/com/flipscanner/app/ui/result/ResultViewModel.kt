package com.flipscanner.app.ui.result

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.flipscanner.app.data.ScanSession
import com.flipscanner.app.data.model.ScanResult
import com.flipscanner.app.data.model.mockScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    session: ScanSession
) : ViewModel() {

    val result: ScanResult = mockScanResult(session.lastPhotoPath)

    var buyPrice by mutableStateOf("")

    private val buy: Double
        get() = buyPrice.toDoubleOrNull() ?: 0.0

    // Estimate: 13% marketplace fees + $8 shipping on the typical price
    private val fees: Double
        get() = result.priceRange.mid * 0.13
    private val shipping = 8.0

    val netProfit: Double
        get() = result.priceRange.mid - fees - shipping - buy

    val verdict: String
        get() = when {
            buy <= 0.0 -> "Enter what you'd pay"
            netProfit >= 20.0 -> "Great flip"
            netProfit > 0.0 -> "OK flip"
            else -> "Skip"
        }
}

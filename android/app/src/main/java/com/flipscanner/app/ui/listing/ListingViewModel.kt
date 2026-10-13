package com.flipscanner.app.ui.listing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.flipscanner.app.data.ScanSession
import com.flipscanner.app.data.model.ListingDraft
import com.flipscanner.app.data.model.mockScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * AI listing generator UI. Drafts are mock-built locally in Part 2;
 * Part 3 generates them on the backend per platform rules.
 */
@HiltViewModel
class ListingViewModel @Inject constructor(
    session: ScanSession
) : ViewModel() {

    val platforms = listOf("eBay", "Vinted", "Poshmark", "Facebook")

    var selected by mutableStateOf(0)

    private val result = mockScanResult(session.lastPhotoPath)

    private val variants = mutableStateMapOf<String, Int>()

    fun draftFor(platform: String): ListingDraft =
        buildDraft(platform, variants[platform] ?: 0)

    fun regenerate(platform: String) {
        variants[platform] = (variants[platform] ?: 0) + 1
    }

    private fun buildDraft(platform: String, variant: Int): ListingDraft {
        val r = result
        val price = "$${"%.2f".format(r.priceRange.mid)}"
        val alt = if (variant % 2 == 1) " - Great Deal" else ""
        return when (platform) {
            "eBay" -> ListingDraft(
                title = "${r.brand} ${r.model ?: ""} ${r.itemName}$alt".trim().take(80),
                description = "${r.itemName} by ${r.brand}.\nCondition: ${r.condition}.\n\nShips within 2 business days.$alt",
                tags = listOf(r.category, r.brand, "vintage", "resale"),
                suggestedPrice = price
            )
            "Vinted" -> ListingDraft(
                title = "${r.itemName}$alt".take(80),
                description = "Selling my ${r.itemName} (${r.brand}). Condition: ${r.condition}. Feel free to ask questions!",
                tags = listOf(r.category.lowercase(), r.brand.lowercase(), "thrifted"),
                suggestedPrice = price
            )
            "Poshmark" -> ListingDraft(
                title = "${r.brand} ${r.itemName}$alt".take(80),
                description = "Just in: ${r.itemName} by ${r.brand}!\nCondition: ${r.condition}.\nFrom a smoke-free home.",
                tags = listOf("poshmark", r.category.lowercase(), r.brand.lowercase()),
                suggestedPrice = price
            )
            else -> ListingDraft(
                title = "${r.itemName} - ${r.condition}$alt",
                description = "For sale: ${r.itemName} (${r.brand}).\nCondition: ${r.condition}.\nPickup available, message me!",
                tags = listOf("forsale", r.category.lowercase()),
                suggestedPrice = price
            )
        }
    }
}

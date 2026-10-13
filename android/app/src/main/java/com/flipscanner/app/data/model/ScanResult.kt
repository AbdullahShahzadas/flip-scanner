package com.flipscanner.app.data.model

data class PriceRange(
    val low: Double,
    val mid: Double,
    val high: Double,
    val currency: String = "USD"
)

data class ComparableListing(
    val title: String,
    val price: Double,
    val condition: String,
    val platform: String
)

data class ScanResult(
    val itemName: String,
    val brand: String,
    val model: String?,
    val category: String,
    val condition: String,
    val confidence: Float,
    val priceRange: PriceRange,
    val comparables: List<ComparableListing>,
    val photoPath: String?
)

data class ListingDraft(
    val title: String,
    val description: String,
    val tags: List<String>,
    val suggestedPrice: String
)

/** Clearly-marked mock data until Part 3 wires the real backend. */
fun mockScanResult(photoPath: String?): ScanResult {
    val range = PriceRange(low = 45.0, mid = 68.0, high = 95.0)
    return ScanResult(
        itemName = "Vintage film camera (demo)",
        brand = "DemoBrand",
        model = "X-100",
        category = "Cameras",
        condition = "Good",
        confidence = 0.82f,
        priceRange = range,
        comparables = listOf(
            ComparableListing("Vintage film camera X-100, tested", 72.0, "Used - Good", "eBay"),
            ComparableListing("Retro X-100 camera, works", 59.99, "Used", "eBay"),
            ComparableListing("Film camera X100 vintage", 88.5, "Refurbished", "eBay")
        ),
        photoPath = photoPath
    )
}

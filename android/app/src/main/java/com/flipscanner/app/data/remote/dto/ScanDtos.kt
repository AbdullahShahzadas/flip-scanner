package com.flipscanner.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComparableDto(
    val title: String = "",
    val price: Double = 0.0,
    val currency: String = "USD",
    val url: String = "",
    val image: String = "",
    val condition: String = ""
)

@Serializable
data class ScanResponseDto(
    @SerialName("scan_id") val scanId: Int = 0,
    val item: String = "",
    val brand: String = "",
    val model: String = "",
    val condition: String = "",
    val confidence: Float = 0f,
    val category: String = "",
    @SerialName("price_low") val priceLow: Double = 0.0,
    @SerialName("price_typical") val priceTypical: Double = 0.0,
    @SerialName("price_high") val priceHigh: Double = 0.0,
    val comparables: List<ComparableDto> = emptyList(),
    @SerialName("purchase_price") val purchasePrice: Double = 0.0,
    @SerialName("estimated_profit") val estimatedProfit: Double = 0.0,
    val verdict: String = "UNKNOWN",
    val photos: Int = 0
)

@Serializable
data class ErrorDto(
    val error: String = "",
    val message: String = ""
)

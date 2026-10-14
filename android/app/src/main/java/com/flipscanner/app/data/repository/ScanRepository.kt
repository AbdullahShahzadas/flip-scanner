package com.flipscanner.app.data.repository

import com.flipscanner.app.data.model.ComparableListing
import com.flipscanner.app.data.model.PriceRange
import com.flipscanner.app.data.model.ScanResult
import com.flipscanner.app.data.remote.FlipScannerApi
import com.flipscanner.app.data.remote.dto.ScanResponseDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Talks to the Flip Scanner backend.
 * Falls back to a clear error when the backend is unreachable
 * (e.g. API keys not configured server-side).
 */
@Singleton
class ScanRepository @Inject constructor(
    private val api: FlipScannerApi
) {
    sealed interface Result {
        data class Success(val scan: ScanResult) : Result
        data class Error(val message: String) : Result
    }

    suspend fun scanPhotos(
        photoFiles: List<File>,
        purchasePrice: Double? = null
    ): Result {
        if (photoFiles.isEmpty() || photoFiles.size > 3) {
            return Result.Error("Select 1 to 3 photos.")
        }

        return try {
            val parts = photoFiles.map { file ->
                val body = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("photos[]", file.name, body)
            }
            val pricePart = purchasePrice
                ?.takeIf { it > 0 }
                ?.toString()
                ?.toRequestBody("text/plain".toMediaTypeOrNull())

            val dto = api.scan(parts, pricePart)
            Result.Success(dto.toDomain(photoFiles.firstOrNull()?.absolutePath))
        } catch (e: retrofit2.HttpException) {
            val msg = when (e.code()) {
                503 -> "Service temporarily unavailable. Check server API keys."
                400 -> "Bad request: ${e.message()}"
                else -> "Server error (${e.code()})."
            }
            Result.Error(msg)
        } catch (e: java.io.IOException) {
            Result.Error("Cannot reach the server. Check your connection and API URL.")
        } catch (e: Exception) {
            Result.Error("Scan failed: ${e.message ?: "unknown error"}")
        }
    }

    private fun ScanResponseDto.toDomain(photoPath: String?): ScanResult {
        return ScanResult(
            itemName = item.ifBlank { "Unknown item" },
            brand = brand,
            model = model.ifBlank { null },
            category = category,
            condition = condition,
            confidence = confidence,
            priceRange = PriceRange(
                low = priceLow,
                mid = priceTypical,
                high = priceHigh
            ),
            comparables = comparables.map {
                ComparableListing(
                    title = it.title,
                    price = it.price,
                    condition = it.condition,
                    platform = "eBay"
                )
            },
            photoPath = photoPath
        )
    }
}

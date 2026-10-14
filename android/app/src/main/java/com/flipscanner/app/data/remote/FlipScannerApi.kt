package com.flipscanner.app.data.remote

import com.flipscanner.app.data.remote.dto.ScanResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * Flip Scanner backend API.
 * Base URL is configured in NetworkModule (BuildConfig or default).
 */
interface FlipScannerApi {

    /**
     * Upload 1-3 photos, get AI identification + eBay comparables + price range.
     */
    @Multipart
    @POST("api/scan")
    suspend fun scan(
        @Part photos: List<MultipartBody.Part>,
        @Part("purchase_price") purchasePrice: RequestBody? = null
    ): ScanResponseDto
}

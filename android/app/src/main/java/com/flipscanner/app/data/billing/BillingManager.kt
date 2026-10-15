package com.flipscanner.app.data.billing

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Play Billing for Flip Scanner subscriptions.
 *
 * Products (create these in Play Console with the exact IDs):
 * - flip_scanner_weekly : $6.99/week with a 3-day free trial (trial is
 *   configured on the offer in Play Console, not in code)
 * - flip_scanner_yearly : $39.99/year
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_WEEKLY = "flip_scanner_weekly"
        const val PRODUCT_YEARLY = "flip_scanner_yearly"
    }

    var isConnected by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var products by mutableStateOf<Map<String, ProductDetails>>(emptyMap())
        private set
    var hasActiveSubscription by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var billingClient: BillingClient? = null

    fun connect() {
        if (billingClient != null) return
        val pendingParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(pendingParams)
            .build()
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    isConnected = true
                    error = null
                    queryProducts()
                    queryActiveSubscriptions()
                } else {
                    error = "Billing unavailable: ${result.debugMessage}"
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnected = false
            }
        })
    }

    fun disconnect() {
        billingClient?.endConnection()
        billingClient = null
        isConnected = false
    }

    private fun queryProducts() {
        val client = billingClient ?: return
        isLoading = true
        val productList = listOf(PRODUCT_WEEKLY, PRODUCT_YEARLY).map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        client.queryProductDetailsAsync(params) { result, detailsList ->
            isLoading = false
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                products = detailsList.associateBy { it.productId }
            } else {
                error = "Couldn't load products: ${result.debugMessage}"
            }
        }
    }

    fun launchPurchase(activity: Activity, productId: String) {
        val client = billingClient ?: return
        val details = products[productId] ?: run {
            error = "Product not available yet."
            return
        }
        // Use the first offer (base plan). The 3-day free trial is
        // configured on the offer in Play Console.
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: run {
                error = "No offer available for this product."
                return
            }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        client.launchBillingFlow(activity, flowParams)
    }

    fun queryActiveSubscriptions() {
        val client = billingClient ?: return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                hasActiveSubscription = purchases.any {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
                queryActiveSubscriptions()
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                // User backed out; nothing to do.
            }
            else -> {
                error = "Purchase failed: ${result.debugMessage}"
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient?.acknowledgePurchase(params) { _ -> }
        }
    }

    fun clearError() {
        error = null
    }
}

/** Human-readable recurring price, e.g. "$6.99/week". */
fun ProductDetails.recurringPriceLabel(): String {
    val phase = subscriptionOfferDetails
        ?.firstOrNull()
        ?.pricingPhases
        ?.pricingPhaseList
        ?.lastOrNull()
        ?: return ""
    val period = when (phase.billingPeriod) {
        "P1W" -> "/week"
        "P1M" -> "/month"
        "P1Y" -> "/year"
        else -> ""
    }
    return "${phase.formattedPrice}$period"
}

/** True if any offer has a free ($0) introductory phase, e.g. a free trial. */
fun ProductDetails.hasFreeTrial(): Boolean {
    return subscriptionOfferDetails?.any { offer ->
        offer.pricingPhases.pricingPhaseList.any { it.priceAmountMicros == 0L }
    } == true
}

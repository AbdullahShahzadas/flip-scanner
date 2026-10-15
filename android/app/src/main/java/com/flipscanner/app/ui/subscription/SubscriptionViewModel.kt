package com.flipscanner.app.ui.subscription

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.flipscanner.app.data.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    val billing: BillingManager
) : ViewModel() {

    init {
        billing.connect()
    }

    fun subscribe(activity: Activity, productId: String) {
        billing.launchPurchase(activity, productId)
    }

    fun refresh() {
        billing.queryActiveSubscriptions()
    }

    override fun onCleared() {
        // Keep the singleton connection alive for the app session.
    }
}

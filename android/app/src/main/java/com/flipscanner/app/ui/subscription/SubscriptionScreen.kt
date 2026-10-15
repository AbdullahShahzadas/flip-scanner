package com.flipscanner.app.ui.subscription

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flipscanner.app.data.billing.BillingManager
import com.flipscanner.app.data.billing.hasFreeTrial
import com.flipscanner.app.data.billing.recurringPriceLabel

/**
 * Subscription options: weekly ($6.99, 3-day trial) and yearly ($39.99).
 * Product IDs and trial are configured in Play Console.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    vm: SubscriptionViewModel = hiltViewModel()
) {
    val billing = vm.billing
    val context = LocalContext.current
    val activity = context as? Activity

    Scaffold(
        topBar = { TopAppBar(title = { Text("Flip Scanner Pro") }) }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Unlimited scans, AI identification and listing generator.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (billing.hasActiveSubscription) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            "You're subscribed — enjoy unlimited scans!",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (billing.isLoading) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            item {
                PlanCard(
                    title = "Weekly",
                    description = "Billed weekly. Cancel anytime.",
                    priceLabel = billing.products[BillingManager.PRODUCT_WEEKLY]
                        ?.recurringPriceLabel()
                        .takeIf { !it.isNullOrBlank() } ?: "$6.99/week",
                    trialLabel = if (billing.products[BillingManager.PRODUCT_WEEKLY]?.hasFreeTrial() == true)
                        "Includes free trial" else "3-day free trial",
                    buttonLabel = "Subscribe weekly",
                    enabled = billing.isConnected && activity != null && !billing.hasActiveSubscription,
                    onSubscribe = {
                        activity?.let { vm.subscribe(it, BillingManager.PRODUCT_WEEKLY) }
                    }
                )
            }

            item {
                PlanCard(
                    title = "Yearly",
                    description = "Best value — billed once a year.",
                    priceLabel = billing.products[BillingManager.PRODUCT_YEARLY]
                        ?.recurringPriceLabel()
                        .takeIf { !it.isNullOrBlank() } ?: "$39.99/year",
                    trialLabel = null,
                    buttonLabel = "Subscribe yearly",
                    enabled = billing.isConnected && activity != null && !billing.hasActiveSubscription,
                    onSubscribe = {
                        activity?.let { vm.subscribe(it, BillingManager.PRODUCT_YEARLY) }
                    }
                )
            }

            billing.error?.let { msg ->
                item {
                    Text(
                        msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (!billing.isConnected && !billing.isLoading) {
                item {
                    Text(
                        "Subscriptions are available in the Play Store version of the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    description: String,
    priceLabel: String,
    trialLabel: String?,
    buttonLabel: String,
    enabled: Boolean,
    onSubscribe: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(priceLabel, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            trialLabel?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = onSubscribe,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(buttonLabel)
            }
        }
    }
}

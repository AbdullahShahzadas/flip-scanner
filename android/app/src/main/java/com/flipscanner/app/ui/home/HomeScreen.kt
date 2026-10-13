package com.flipscanner.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Home screen (Part 1): profit card, big Scan button, recent scans.
 * Wiring to real data comes in Part 4 (inventory + dashboard).
 */
@Composable
fun HomeScreen(
    onScan: () -> Unit,
    onInventory: () -> Unit,
    onDashboard: () -> Unit
) {
    Scaffold { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ProfitCard(onDashboard) }
            item {
                Button(
                    onClick = onScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Text("Scan an item", style = MaterialTheme.typography.titleLarge)
                }
            }
            item {
                Text(
                    "Recent scans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(listOf("Vintage film camera", "Leather jacket", "Retro game console")) { name ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(name, modifier = Modifier.padding(16.dp))
                }
            }
            item {
                TextButton(onClick = onInventory) {
                    Text("Open inventory")
                }
            }
        }
    }
}

@Composable
private fun ProfitCard(onDashboard: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onDashboard
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Profit this month",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "$0.00",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

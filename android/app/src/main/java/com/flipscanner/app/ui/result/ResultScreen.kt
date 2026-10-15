package com.flipscanner.app.ui.result

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.io.File

/**
 * Scan result: item info, value range, comparables, "Worth flipping?"
 * calculator. Uses the real backend API (Part 3); falls back to demo
 * data with a badge when the backend is unreachable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    onBack: () -> Unit,
    onGenerateListing: () -> Unit,
    vm: ResultViewModel = hiltViewModel()
) {
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(vm.saveMessage) {
        vm.saveMessage?.let {
            snackbar.showSnackbar(it)
            vm.dismissSaveMessage()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Scan result") }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        when {
            vm.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(pad),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(
                            "Identifying item…",
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            vm.result == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            vm.error ?: "Something went wrong.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = { vm.retry() },
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                val r = vm.result!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (vm.usingDemoData) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFEF3C7)
                                )
                            ) {
                                Text(
                                    "Demo data — connect the backend for real results." +
                                        (vm.error?.let { " ($it)" } ?: ""),
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    item {
                        r.photoPath?.let { photo ->
                            AsyncImage(
                                model = if (photo.startsWith("content://")) Uri.parse(photo) else File(photo),
                                contentDescription = "Scanned item",
                                modifier = Modifier.fillMaxWidth().height(220.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    r.itemName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${r.brand} ${r.model ?: ""}".trim(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Condition: ${r.condition} · Confidence: ${(r.confidence * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Estimated resale value",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    PriceBox("Low", r.priceRange.low)
                                    PriceBox("Typical", r.priceRange.mid)
                                    PriceBox("High", r.priceRange.high)
                                }
                            }
                        }
                    }
                    item {
                        Text(
                            "Similar listings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(r.comparables.size) { i ->
                        val c = r.comparables[i]
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    c.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "$${c.price} · ${c.condition} · ${c.platform}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Worth flipping?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                    value = vm.buyPrice,
                                    onValueChange = { vm.buyPrice = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                    label = { Text("What would you pay? ($)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    "Est. net profit: $${"%.2f".format(vm.netProfit)}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    vm.verdict,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = verdictColor(vm.verdict)
                                )
                            }
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = { vm.saveToInventory() },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("Save to inventory")
                        }
                    }
                    item {
                        Button(
                            onClick = onGenerateListing,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("Generate listing")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceBox(label: String, value: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(
            "$${"%.0f".format(value)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun verdictColor(verdict: String): Color = when (verdict) {
    "Great flip" -> Color(0xFF10B981)
    "OK flip" -> Color(0xFFF59E0B)
    "Skip" -> Color(0xFFEF4444)
    else -> Color.Gray
}

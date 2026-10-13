package com.flipscanner.app.ui.result

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
 * calculator. Real backend data arrives in Part 3 (mock data for now).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    onBack: () -> Unit,
    onGenerateListing: () -> Unit,
    vm: ResultViewModel = hiltViewModel()
) {
    val r = vm.result
    Scaffold(
        topBar = { TopAppBar(title = { Text("Scan result") }) }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

package com.flipscanner.app.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.flipscanner.app.data.local.InventoryItem
import java.io.File

/**
 * Inventory: All / In stock / Sold tabs, mark-as-sold, delete.
 * Backed by the Room database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onBack: () -> Unit,
    vm: InventoryViewModel = hiltViewModel()
) {
    val items by vm.items.collectAsState()
    var tab by remember { mutableStateOf(0) }
    var sellTarget by remember { mutableStateOf<InventoryItem?>(null) }

    val visible = when (tab) {
        1 -> items.filter { !it.isSold }
        2 -> items.filter { it.isSold }
        else -> items
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Inventory") }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("All") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("In stock") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Sold") })
            }
            if (visible.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        when (tab) {
                            1 -> "Nothing in stock."
                            2 -> "Nothing sold yet."
                            else -> "Inventory is empty. Scan an item and save it here."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visible, key = { it.id }) { item ->
                        InventoryRow(
                            item = item,
                            onSell = { sellTarget = item },
                            onRelist = { vm.markAsUnsold(item.id) },
                            onDelete = { vm.delete(item) }
                        )
                    }
                }
            }
        }
    }

    sellTarget?.let { item ->
        var price by remember(item.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { sellTarget = null },
            title = { Text("Mark as sold") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.itemName, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Sold price ($)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        price.toDoubleOrNull()?.let {
                            vm.markAsSold(item.id, it)
                            sellTarget = null
                        }
                    },
                    enabled = (price.toDoubleOrNull() ?: 0.0) > 0.0
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { sellTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun InventoryRow(
    item: InventoryItem,
    onSell: () -> Unit,
    onRelist: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item.photoPath?.let { photo ->
                AsyncImage(
                    model = File(photo),
                    contentDescription = item.itemName,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.itemName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Paid $${"%.2f".format(item.buyPrice)}" +
                        if (item.isSold) " · Sold $${"%.2f".format(item.soldPrice)}" else "",
                    style = MaterialTheme.typography.bodySmall
                )
                item.profit?.let { profit ->
                    Text(
                        "Profit $${"%.2f".format(profit)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (profit >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
                Text(
                    if (item.isSold) "Sold" else "In stock",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (item.isSold) {
                    TextButton(onClick = onRelist) { Text("Relist") }
                } else {
                    TextButton(onClick = onSell) { Text("Sell") }
                }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

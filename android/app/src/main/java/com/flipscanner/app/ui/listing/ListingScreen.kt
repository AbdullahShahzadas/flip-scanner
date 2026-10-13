package com.flipscanner.app.ui.listing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingScreen(
    onBack: () -> Unit,
    vm: ListingViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Listing generator") }) }
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad)) {
            TabRow(selectedTabIndex = vm.selected) {
                vm.platforms.forEachIndexed { i, platform ->
                    Tab(
                        selected = vm.selected == i,
                        onClick = { vm.selected = i },
                        text = { Text(platform) }
                    )
                }
            }
            val platform = vm.platforms[vm.selected]
            val draft = vm.draftFor(platform)
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { CopyableField("Title", draft.title) }
                item { CopyableField("Description", draft.description) }
                item { CopyableField("Tags", draft.tags.joinToString(", ")) }
                item { CopyableField("Suggested price", draft.suggestedPrice) }
                item {
                    OutlinedButton(
                        onClick = { vm.regenerate(platform) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Regenerate")
                    }
                }
            }
        }
    }
}

@Composable
private fun CopyableField(label: String, text: String) {
    val clipboard = LocalClipboardManager.current
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(label, style = MaterialTheme.typography.labelMedium)
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = { clipboard.setText(AnnotatedString(text)) }) {
                Text("Copy")
            }
        }
    }
}

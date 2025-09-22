package com.example.arsip.ui.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.arsip.data.Book

@Composable
fun MyBooksScreen(
    onAdd: () -> Unit = {},         // FAB sudah di AppNav
    vm: MyBooksViewModel = hiltViewModel()
) {
    val items by vm.items.collectAsState()
    val loading by vm.loading.collectAsState()

    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Belum ada buku.\nTekan tombol + untuk menambah.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        else -> LazyColumn(Modifier.fillMaxSize()) {
            items(items) { b -> BookRow(b) }
        }
    }
}

@Composable
private fun BookRow(b: Book, onClick: () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AsyncImage(
            model = b.imageUrls.firstOrNull(),
            contentDescription = null,
            modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.medium)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(b.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text("Rp ${b.price}", style = MaterialTheme.typography.bodyMedium)
        }
    }
    Divider()
}

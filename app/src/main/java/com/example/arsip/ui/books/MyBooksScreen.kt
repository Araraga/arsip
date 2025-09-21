@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip.ui.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.arsip.data.Book
import java.text.NumberFormat

@Composable
fun MyBooksScreen(
    onAdd: () -> Unit,
    onOpen: (String) -> Unit = {},
    vm: MyBooksViewModel = hiltViewModel()
) {
    val list by vm.books.collectAsState()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Buku Saya") }) },
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Text("+") } }
    ) { inner ->
        LazyColumn(
            Modifier.padding(inner).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = list, key = { it.id ?: it.title }) { b ->
                BookRow(b = b, onClick = { b.id?.let(onOpen) })
            }
        }
    }
}

@Composable
fun BookRow(b: Book, onClick: () -> Unit = {}) {
    Card(onClick = onClick) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(
                model = b.imageUrls.firstOrNull(),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    b.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Rp " + NumberFormat.getInstance().format(b.price),
                    style = MaterialTheme.typography.labelLarge
                )
                if (b.description.isNotBlank()) {
                    Text(b.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyBooksPreview() {
    MaterialTheme {
        val sample = listOf(
            Book(id = "1", title = "Kotlin in Action", price = 180000, description = "Buku Kotlin.", imageUrls = emptyList(), ownerId = "u"),
            Book(id = "2", title = "Clean Architecture", price = 200000, description = "Uncle Bob.", imageUrls = emptyList(), ownerId = "u")
        )
        Scaffold { inner ->
            LazyColumn(Modifier.padding(inner).padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sample, key = { it.id!! }) { b -> BookRow(b) }
            }
        }
    }
}

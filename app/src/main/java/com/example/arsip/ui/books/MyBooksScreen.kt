package com.example.arsip.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.arsip.data.Book

@Composable
fun MyBooksScreen(
    onClickBook: (String) -> Unit, // <-- Parameter baru untuk navigasi
    vm: MyBooksViewModel = hiltViewModel()
) {
    val items by vm.items.collectAsState()
    val loading by vm.loading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Buku Saya",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF673AB7), // Warna ungu
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Belum ada buku.\nTekan tombol + untuk menambah.",
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { book ->
                        BookCard(
                            book = book,
                            onClick = { onClickBook(book.id) } // <-- Panggil callback di sini
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookCard(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Box untuk menampung gambar dan status ketersediaan
            Box(modifier = Modifier.size(90.dp)) {
                AsyncImage(
                    model = book.imageUrls.firstOrNull(),
                    contentDescription = "Gambar sampul ${book.title}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )

                // --- STATUS KETERSEDIAAN (BADGE) ---
                val (statusText, statusColor) = if (book.isAvailable) {
                    "Tersedia" to Color(0xFF4CAF50) // Hijau
                } else {
                    "Dipinjam" to Color.Gray // Abu-abu
                }

                Text(
                    text = statusText,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Posisi di pojok kanan atas
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- KUMPULAN FUNGSI PREVIEW ---

@Preview(showBackground = true, name = "Pratinjau Kartu Buku")
@Composable
private fun BookCardPreview() {
    val dummyBook = Book(
        id = "123",
        title = "Belajar Jetpack Compose Untuk Pemula Hingga Mahir",
        author = "Ahmad Dahlan",
        imageUrls = listOf(""),
        isAvailable = false // Contoh status "Dipinjam"
    )
    MaterialTheme {
        BookCard(book = dummyBook, onClick = {})
    }
}

@Preview(showBackground = true, name = "Pratinjau Halaman Penuh", heightDp = 800)
@Composable
private fun MyBooksScreenPreview() {
    val dummyBooks = listOf(
        Book(id = "1", title = "Mahir Kotlin dalam 24 Jam", author = "Budi Doremi", imageUrls = listOf(""), isAvailable = true),
        Book(id = "2", title = "Resep Rahasia Masakan Nusantara", author = "Sisca Soewitomo", imageUrls = listOf(""), isAvailable = false),
        Book(id = "3", title = "Panduan Lengkap Merawat Tanaman Hias", author = "Farah Quinn", imageUrls = listOf(""), isAvailable = true)
    )

    MaterialTheme {
        MyBooksScreen(onClickBook = {})
    }
}
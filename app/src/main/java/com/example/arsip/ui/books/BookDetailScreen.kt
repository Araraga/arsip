package com.example.arsip.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun BookDetailScreen(
    navController: NavController,
    bookId: String, // Menerima bookId dari AppNav
    vm: BookDetailViewModel = hiltViewModel()
) {
    val book by vm.book.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Buku") },
            text = { Text("Apakah Anda yakin ingin menghapus buku '${book?.title}'? Aksi ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteBook { navController.popBackStack() }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
            }
        )
    }

    // Menggunakan book?.let untuk mencegah crash saat data null setelah dihapus
    book?.let { currentBook ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {

                // --- HEADER GAMBAR BUKU ---
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                        AsyncImage(
                            model = currentBook.imageUrls.firstOrNull(),
                            contentDescription = "Sampul buku",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient agar tombol lebih terlihat
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    startY = 300f
                                )
                            )
                        )
                    }
                }

                // --- KONTEN DETAIL BUKU ---
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp, bottom = 80.dp) // Padding bawah agar tidak tertutup tombol
                    ) {
                        Text(
                            text = currentBook.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "oleh ${currentBook.author}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        // Status Ketersediaan
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tersedia", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.weight(1f))
                                Switch(
                                    checked = currentBook.isAvailable,
                                    onCheckedChange = { vm.toggleAvailability(it) }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        // Deskripsi
                        Text("Deskripsi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (currentBook.desc.isNotBlank()) currentBook.desc else "Tidak ada deskripsi.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(16.dp))

                        // Lokasi
                        Text("Lokasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(currentBook.addressText, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            // --- TOMBOL AKSI (BACK, EDIT, DELETE) ---
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 32.dp), // Mendorong tombol ke bawah
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { navController.navigate("edit/$bookId") },
                        modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.White)
                    }
                }
            }
        }
    } ?: run {
        // Tampilan loading, juga berfungsi sebagai fallback untuk mencegah crash
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
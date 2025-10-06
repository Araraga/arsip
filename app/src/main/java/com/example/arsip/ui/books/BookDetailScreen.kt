package com.example.arsip.ui.books

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.net.URLEncoder

@Composable
fun BookDetailScreen(
    navController: NavController,
    bookId: String, // Menerima bookId dari AppNav
    vm: BookDetailViewModel = hiltViewModel()
) {
    val book by vm.book.collectAsState()
    val owner by vm.owner.collectAsState() // ✅ NEW: Owner data for WhatsApp
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ✅ NEW: WhatsApp function
    fun openWhatsApp(phoneNumber: String, bookTitle: String) {
        try {
            val message = "Halo, saya tertarik meminjam buku \"$bookTitle\" yang Anda tawarkan di Buku Keliling. Apakah masih tersedia?"
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val formattedPhone = phoneNumber.replace("+", "").replace("-", "").replace(" ", "")

            // Add +62 if phone starts with 08
            val internationalPhone = if (formattedPhone.startsWith("08")) {
                "62${formattedPhone.substring(1)}"
            } else {
                formattedPhone
            }

            val whatsappUrl = "https://wa.me/$internationalPhone?text=$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle error - maybe show a toast
        }
    }

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
                            .padding(top = 16.dp, bottom = 140.dp) // ✅ Increased bottom padding for WhatsApp button
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

                        // ✅ NEW: Show book category
                        if (currentBook.category.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = currentBook.category,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        // Status Ketersediaan - Hanya pemilik yang bisa mengubah
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tersedia", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.weight(1f))
                                if (vm.isOwner) {
                                    // Hanya pemilik yang bisa mengubah status
                                    Switch(
                                        checked = currentBook.isAvailable,
                                        onCheckedChange = { vm.toggleAvailability(it) }
                                    )
                                } else {
                                    // Non-pemilik hanya melihat status
                                    Text(
                                        text = if (currentBook.isAvailable) "Ya" else "Tidak",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (currentBook.isAvailable) Color(0xFF4CAF50) else Color(0xFFFF5722),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
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

                        // ✅ NEW: Owner Information (if available)
                        owner?.let { ownerProfile ->
                            Spacer(Modifier.height(16.dp))
                            Text("Pemilik", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = ownerProfile.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (ownerProfile.address.isNotBlank()) {
                                        Text(
                                            text = ownerProfile.address,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
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

            // ✅ NEW: Pinjam Sekarang Button - Bottom positioned
            if (currentBook.isAvailable && owner?.phoneNumber?.isNotBlank() == true && !vm.isOwner) {
                Button(
                    onClick = { openWhatsApp(owner!!.phoneNumber, currentBook.title) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366) // WhatsApp green
                    )
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "💬 PINJAM SEKARANG",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
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
package com.example.arsip.ui.books

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.arsip.R
import java.net.URLEncoder

@Composable
fun BookDetailScreen(
    navController: NavController,
    bookId: String,
    vm: BookDetailViewModel = hiltViewModel()
) {
    val book by vm.book.collectAsState()
    val owner by vm.owner.collectAsState()
    val isOwner by remember { derivedStateOf { vm.isOwner } }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun openWhatsApp(phoneNumber: String, bookTitle: String) {
        try {
            val message = "Halo, saya tertarik meminjam buku \"$bookTitle\" yang Anda tawarkan di Buku Keliling. Apakah masih tersedia?"
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val formattedPhone = phoneNumber.replace("+", "").replace("-", "").replace(" ", "")

            val internationalPhone = if (formattedPhone.startsWith("08")) {
                "62${formattedPhone.substring(1)}"
            } else {
                formattedPhone
            }

            val whatsappUrl = "https://wa.me/$internationalPhone?text=$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle error
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Buku", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus buku '${book?.title}'? Aksi ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteBook { navController.popBackStack() }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) { Text("Hapus", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    book?.let { currentBook ->
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA))) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                    ) {
                        AsyncImage(
                            model = currentBook.imageUrls.firstOrNull(),
                            contentDescription = "Sampul buku",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.8f)
                                        ),
                                        startY = 0f,
                                        endY = 1200f
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp)
                        ) {
                            if (currentBook.category.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF6200EE).copy(alpha = 0.9f),
                                    modifier = Modifier.shadow(4.dp, RoundedCornerShape(20.dp))
                                ) {
                                    Text(
                                        text = currentBook.category,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                            }

                            Text(
                                text = currentBook.title,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                lineHeight = 38.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "oleh ${currentBook.author}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .offset(y = (10).dp)
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 20.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (currentBook.isAvailable)
                                                Color(0xFF4CAF50).copy(alpha = 0.15f)
                                            else
                                                Color(0xFFFF5252).copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (currentBook.isAvailable) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (currentBook.isAvailable) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Status Ketersediaan",
                                        fontSize = 14.sp,
                                        color = Color(0xFF79747E),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = if (currentBook.isAvailable) "Tersedia untuk dipinjam" else "Sedang tidak tersedia",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentBook.isAvailable) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                    )
                                }

                                if (isOwner) {
                                    Switch(
                                        checked = currentBook.isAvailable,
                                        onCheckedChange = { vm.toggleAvailability(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF4CAF50),
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color(0xFFFF5252)
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Tentang Buku",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1B1F)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = if (currentBook.desc.isNotBlank()) currentBook.desc else "Tidak ada deskripsi tersedia untuk buku ini.",
                                    fontSize = 15.sp,
                                    color = Color(0xFF49454F),
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF3E8FF)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6200EE).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF6200EE),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Lokasi Buku",
                                        fontSize = 14.sp,
                                        color = Color(0xFF6200EE),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = currentBook.addressText,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1C1B1F),
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        if (!isOwner && owner != null) {
                            val ownerProfile = owner!!
                            Spacer(Modifier.height(20.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = ownerProfile.photoUrl,
                                        contentDescription = "Foto Pemilik",
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Pemilik Buku",
                                            fontSize = 13.sp,
                                            color = Color(0xFF79747E),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = ownerProfile.displayName,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1C1B1F)
                                        )
                                        if (ownerProfile.phoneNumber.isNotBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF49454F))
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = ownerProfile.phoneNumber,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF49454F)
                                                )
                                            }
                                        }
                                    }

                                    if (currentBook.isAvailable && ownerProfile.phoneNumber.isNotBlank()) {
                                        IconButton(
                                            onClick = { openWhatsApp(ownerProfile.phoneNumber, currentBook.title) },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF25D366))
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_whatsapp),
                                                contentDescription = "Hubungi via WhatsApp",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingActionButton(
                    onClick = { navController.popBackStack() },
                    containerColor = Color.White.copy(alpha = 0.95f),
                    contentColor = Color(0xFF1C1B1F),
                    modifier = Modifier.size(48.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (isOwner) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FloatingActionButton(
                            onClick = { navController.navigate("edit/$bookId") },
                            containerColor = Color(0xFF6200EE),
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp),
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        FloatingActionButton(
                            onClick = { showDeleteDialog = true },
                            containerColor = Color(0xFFFF5252),
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp),
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    } ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF6200EE),
                    strokeWidth = 4.dp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Memuat detail buku...",
                    fontSize = 16.sp,
                    color = Color(0xFF79747E),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
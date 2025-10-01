@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip.ui.books

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun AddBookScreen(
    onDone: () -> Unit,
    onPickMap: () -> Unit,
    vm: AddBookViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val pickImages = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> -> vm.onImagesSelected(uris) }

    LaunchedEffect(vm.message) {
        vm.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.message = null
        }
    }

    // Menggunakan Scaffold untuk layout yang lebih baik dan penempatan FAB yang benar
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // TopBar kustom agar header bisa turun
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 45.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tombol kembali
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                    // Judul halaman
                    Text(
                        text = "Tambah Buku Baru",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF673AB7)
                    )
                }
            }
        },
        floatingActionButton = {
            // Tombol Simpan (FAB) yang posisinya sudah benar
            ExtendedFloatingActionButton(
                onClick = { scope.launch { vm.addBook(onDone) } },
                icon = { Icon(Icons.Outlined.Check, contentDescription = null) },
                text = { Text("Simpan") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding), // Terapkan padding dari Scaffold
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FormSectionCard(title = "Detail Buku") {
                    // ... (Konten form tidak berubah)
                    OutlinedTextField(
                        value = vm.title, onValueChange = { vm.title = it },
                        label = { Text("Judul Buku") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = vm.author, onValueChange = { vm.author = it },
                        label = { Text("Penulis") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = vm.desc, onValueChange = { vm.desc = it },
                        label = { Text("Deskripsi") }, minLines = 4, modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                FormSectionCard(title = "Alamat") {
                    // ... (Konten form tidak berubah)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = vm.useProfileAddr, onCheckedChange = { vm.toggleUseProfileAddr(it) })
                        Text("Gunakan alamat dari Profil")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = vm.addressText, onValueChange = { vm.setManualAddress(it) },
                        label = { Text("Alamat (teks)") }, enabled = !vm.useProfileAddr, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onPickMap, enabled = !vm.useProfileAddr, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pilih Lokasi di Peta")
                    }
                }
            }
            item {
                FormSectionCard(title = "Foto Sampul") {
                    // ... (Konten form tidak berubah)
                    if (vm.images.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(vm.images) { uri ->
                                AsyncImage(
                                    model = uri, contentDescription = null,
                                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    } else {
                        Text(
                            "Belum ada foto yang dipilih.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { pickImages.launch("image/*") }, enabled = !vm.busy, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pilih Foto")
                    }
                }
            }
            // Spacer di akhir tidak lagi diperlukan karena Scaffold menanganinya
        }
    }
}


@Composable
private fun FormSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    // ... (Fungsi FormSectionCard tidak berubah)
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}


@Preview(showBackground = true, name = "Halaman Tambah Buku")
@Composable
private fun AddBookScreenPreview() {
    // ... (Fungsi Preview tidak perlu diubah)
}
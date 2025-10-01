@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt // <-- Import ikon kamera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
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

@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    onPickMap: () -> Unit, // Callback untuk membuka peta
    vm: ProfileViewModel = hiltViewModel()
) {
    val snap by vm.snap.collectAsState()
    val displayName = snap?.getString("displayName") ?: "-"
    val photoUrl = snap?.getString("photoUrl")
    val addressText = snap?.getString("addressText") ?: "Alamat belum diatur"

    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) vm.updatePhoto(uri) }

    // Memindahkan semua konten ke dalam LazyColumn agar bisa di-scroll
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally // Posisikan elemen di tengah secara horizontal
    ) {
        // --- HEADER PROFIL (SETENGAH LINGKARAN) ---
        item {
            ProfileHeaderContent(
                photoUrl = photoUrl,
                displayName = displayName,
                nameInput = vm.name,
                onNameChange = { vm.name = it },
                onSaveName = { vm.saveName() },
                onPickPhoto = { pickPhoto.launch("image/*") } // Melewatkan aksi pick photo
            )
        }

        // --- Kartu untuk Alamat ---
        item {
            Spacer(modifier = Modifier.height(16.dp)) // Jarak antara header dan kartu alamat
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Alamat Profil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(addressText) // Menampilkan alamat yang sudah ada
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    OutlinedTextField(
                        value = vm.tmpAddr,
                        onValueChange = { vm.tmpAddr = it },
                        label = { Text("Ubah Alamat (teks)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onPickMap,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pilih Lokasi di Peta")
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { vm.saveAddress() },
                        enabled = !vm.busy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simpan Alamat")
                    }
                }
            }
        }

        // --- Tombol Logout ---
        item {
            Spacer(modifier = Modifier.height(24.dp)) // Jarak ke tombol logout
            OutlinedButton(
                onClick = { vm.logout(); onLoggedOut() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
            Spacer(modifier = Modifier.height(16.dp)) // Padding bawah untuk scroll
        }
    }
}

@Composable
private fun ProfileHeaderContent(
    photoUrl: String?,
    displayName: String,
    nameInput: String,
    onNameChange: (String) -> Unit,
    onSaveName: () -> Unit,
    onPickPhoto: () -> Unit // Callback baru untuk memilih foto
) {
    var isEditingName by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStartPercent = 50, bottomEndPercent = 50)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Box untuk Foto Profil dengan ikon edit
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd // Posisikan ikon edit di kanan bawah
            ) {
                // Foto Profil
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(4.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Foto Profil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = displayName.firstOrNull()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Icon Edit Foto (menimpa foto profil)
                IconButton(
                    onClick = onPickPhoto,
                    modifier = Modifier
                        .size(32.dp) // Ukuran ikon edit
                        .clip(CircleShape)
                        .background(Color.White) // Latar belakang lingkaran putih
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape) // Border opsional
                        .align(Alignment.BottomEnd) // Posisikan di pojok kanan bawah foto
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt, // Ikon kamera
                        contentDescription = "Ubah Foto",
                        tint = MaterialTheme.colorScheme.primary, // Warna ikon
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Nama Pengguna dan Tombol Edit
            if (isEditingName) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = onNameChange,
                        label = { Text("Nama Baru") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            cursorColor = MaterialTheme.colorScheme.onPrimary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.width(200.dp)
                    )
                    IconButton(onClick = { onSaveName(); isEditingName = false }) {
                        Icon(Icons.Default.Check, contentDescription = "Simpan Nama", tint = Color.White)
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    IconButton(onClick = { isEditingName = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Ubah Nama", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}


// --- FUNGSI PREVIEW ---

@Preview(showBackground = true, name = "Halaman Profil")
@Composable
private fun ProfileScreenPreview() {
    val dummyDisplayName by remember { mutableStateOf("Budi Setiawan") }
    // Ganti dengan URL gambar asli jika ingin melihat foto profil di preview
    val dummyPhotoUrl by remember { mutableStateOf<String?>("https://randomuser.me/api/portraits/men/1.jpg") }
    val dummyAddressText by remember { mutableStateOf("Jl. Pahlawan No. 45, Purwokerto, Jawa Tengah, Indonesia") }

    MaterialTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ProfileHeaderContent(
                    photoUrl = dummyPhotoUrl,
                    displayName = dummyDisplayName,
                    nameInput = dummyDisplayName, // Untuk simulasi edit
                    onNameChange = { /* Aksi ubah nama */ },
                    onSaveName = { /* Aksi simpan nama */ },
                    onPickPhoto = { /* Aksi pick photo */ }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Alamat Profil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(dummyAddressText)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        OutlinedTextField(
                            value = dummyAddressText, // Menggunakan dummyAddressText untuk simulasi input
                            onValueChange = { /* Aksi ubah teks alamat */ },
                            label = { Text("Ubah Alamat (teks)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { /* Aksi pilih lokasi */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pilih Lokasi di Peta")
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { /* Aksi simpan alamat */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simpan Alamat")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { /* Aksi logout */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
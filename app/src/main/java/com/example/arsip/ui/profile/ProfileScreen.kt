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
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    navController: NavController,
    onLoggedOut: () -> Unit,
    onPickMap: () -> Unit,
    vm: ProfileViewModel = hiltViewModel()
) {
    val snap by vm.snap.collectAsState()
    val displayName = snap?.getString("displayName") ?: "-"
    val photoUrl = snap?.getString("photoUrl")
    val addressText = snap?.getString("addressText") ?: "Alamat belum diatur"

    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) vm.updatePhoto(uri) }

    LaunchedEffect(navController) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        savedStateHandle?.let { handle ->
            handle.getLiveData<Double>("picked_lat").observeForever { lat ->
                handle.getLiveData<Double>("picked_lng").observeForever { lng ->
                    handle.getLiveData<String>("picked_address").observeForever { address ->
                        if (lat != null && lng != null && address != null) {
                            vm.onLatLngSelected(lat, lng)
                            vm.tmpAddr = address
                            // Reset data untuk mencegah trigger berulang
                            handle.remove<Double>("picked_lat")
                            handle.remove<Double>("picked_lng")
                            handle.remove<String>("picked_address")
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ProfileHeaderContent(
                photoUrl = photoUrl,
                displayName = displayName,
                nameInput = vm.name,
                onNameChange = { vm.name = it },
                onSaveName = { vm.saveName() },
                onPickPhoto = { pickPhoto.launch("image/*") }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Nomor WhatsApp", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    val phoneNumber = snap?.getString("phoneNumber") ?: "Belum diatur"
                    Text(
                        text = if (phoneNumber.isNotBlank() && phoneNumber != "Belum diatur") phoneNumber else "Nomor WhatsApp belum diatur",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (phoneNumber.isNotBlank() && phoneNumber != "Belum diatur")
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (phoneNumber.isBlank() || phoneNumber == "Belum diatur") {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "💡 Nomor WhatsApp diperlukan agar orang lain dapat menghubungi Anda untuk meminjam buku",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Alamat Profil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(addressText)
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

        item {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = { vm.logout(); onLoggedOut() },
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

@Composable
private fun ProfileHeaderContent(
    photoUrl: String?,
    displayName: String,
    nameInput: String,
    onNameChange: (String) -> Unit,
    onSaveName: () -> Unit,
    onPickPhoto: () -> Unit
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
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
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

                IconButton(
                    onClick = onPickPhoto,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Ubah Foto",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


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
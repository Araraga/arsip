@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    vm: ProfileViewModel = hiltViewModel()
) {
    val snap by vm.snap.collectAsState()

    val displayName = snap?.getString("displayName") ?: "-"
    val photoUrl = snap?.getString("photoUrl")

    val pick = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) vm.updatePhoto(uri)
    }

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Profil") }) }) { pv ->
        Column(
            Modifier
                .padding(pv)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                )
            } else {
                Box(
                    Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) { Text(displayName.firstOrNull()?.uppercase() ?: "U") }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { pick.launch("image/*") }, enabled = !vm.busy) {
                Text("Ubah Foto")
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = if (vm.name.isEmpty()) displayName else vm.name,
                onValueChange = { vm.name = it },
                label = { Text("Nama") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { vm.saveName() },
                enabled = !vm.busy,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Simpan Nama") }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Alamat profil:", style = MaterialTheme.typography.labelLarge)
            Text(snap?.getString("addressText") ?: "Belum diset")

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = vm.tmpAddr,
                onValueChange = { vm.tmpAddr = it },
                label = { Text("Alamat (teks)") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = vm.tmpLat,
                    onValueChange = { vm.tmpLat = it },
                    label = { Text("Lat") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = vm.tmpLng,
                    onValueChange = { vm.tmpLng = it },
                    label = { Text("Lng") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    vm.saveAddress(
                        vm.tmpAddr,
                        vm.tmpLat.toDoubleOrNull(),
                        vm.tmpLng.toDoubleOrNull()
                    )
                },
                enabled = !vm.busy,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Simpan Alamat") }

            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = { vm.logout(); onLoggedOut() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Logout") }
        }
    }
}

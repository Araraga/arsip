@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip.ui.books

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch

@Composable
fun AddBookScreen(
    onDone: () -> Unit,
    onPickMap: () -> Unit,                // ⬅ callback buka peta
    vm: AddBookViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    val pickImages = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> -> vm.onImagesSelected(uris) }

    LaunchedEffect(vm.message) {
        vm.message?.let { snackbar.showSnackbar(it); vm.message = null }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Tambah Buku") }) },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { scope.launch { vm.addBook(onDone) } },
                icon = { Icon(Icons.Outlined.Check, contentDescription = null) },
                text = { Text("Simpan") }
            )
        }
    ) { inner ->
        Column(
            Modifier.padding(inner).padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            OutlinedTextField(
                value = vm.title, onValueChange = { vm.title = it },
                label = { Text("Judul") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = vm.price, onValueChange = { vm.price = it },
                label = { Text("Harga (Rp)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = vm.desc, onValueChange = { vm.desc = it },
                label = { Text("Deskripsi") }, minLines = 3, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("Alamat", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = vm.useProfileAddr, onCheckedChange = { vm.toggleUseProfileAddr(it) })
                Text("Gunakan alamat dari Profil")
            }
            OutlinedTextField(
                value = vm.addressText,
                onValueChange = { vm.setManualAddress(it) },
                label = { Text("Alamat (teks)") },
                enabled = !vm.useProfileAddr,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPickMap, enabled = !vm.useProfileAddr) { Text("Pilih di Peta") }
                if (vm.lat != null && vm.lng != null) {
                    AssistChip(onClick = {}, label = { Text("LatLng: ${"%.5f".format(vm.lat)} , ${"%.5f".format(vm.lng)}") })
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Foto", style = MaterialTheme.typography.titleMedium)
            if (vm.images.isEmpty()) {
                Text("Belum ada foto.")
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vm.images) { uri ->
                        AsyncImage(model = uri, contentDescription = null,
                            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(14.dp)))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { pickImages.launch("image/*") }, enabled = !vm.busy) { Text("Pilih Foto") }

            Spacer(Modifier.height(80.dp)) // biar tidak ketutup FAB
        }
    }
}

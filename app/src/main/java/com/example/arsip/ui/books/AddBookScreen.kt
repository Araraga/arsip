@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip.ui.books

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AddBookScreen(onDone: () -> Unit, vm: AddBookViewModel = hiltViewModel()) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        vm.onImagesSelected(uris)
    }
    AddBookContent(
        title = vm.title,
        price = vm.price,
        desc = vm.desc,
        images = vm.images,
        busy = vm.busy,
        onTitle = { vm.title = it },
        onPrice = { vm.price = it },
        onDesc  = { vm.desc  = it },
        onPickImages = { launcher.launch("image/*") },
        onSave = { vm.addBook(); onDone() }
    )
}

@Composable
private fun AddBookContent(
    title: String,
    price: String,
    desc: String,
    images: List<Uri>,
    busy: Boolean,
    onTitle: (String) -> Unit,
    onPrice: (String) -> Unit,
    onDesc:  (String) -> Unit,
    onPickImages: () -> Unit,
    onSave: () -> Unit,
) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Tambah Buku") }) }) { inner ->
        Column(
            Modifier.padding(inner).padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            OutlinedTextField(value = title, onValueChange = onTitle, label = { Text("Judul") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = price, onValueChange = onPrice, label = { Text("Harga (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = desc, onValueChange = onDesc, label = { Text("Deskripsi") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Text("Foto:", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            if (images.isEmpty()) {
                Text("Belum ada foto dipilih", style = MaterialTheme.typography.bodySmall)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(images) { uri ->
                        AsyncImage(model = uri, contentDescription = null, modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onPickImages, enabled = !busy) { Text("Pilih Foto") }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onSave, enabled = !busy && title.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Simpan") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddBookPreview() {
    MaterialTheme {
        AddBookContent(
            title = "Clean Architecture",
            price = "125000",
            desc = "Buku tentang arsitektur perangkat lunak oleh Uncle Bob.",
            images = emptyList(),
            busy = false,
            onTitle = {}, onPrice = {}, onDesc = {},
            onPickImages = {}, onSave = {}
        )
    }
}


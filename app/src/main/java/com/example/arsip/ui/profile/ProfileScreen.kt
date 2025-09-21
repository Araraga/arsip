@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.arsip.data.UserProfile
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ProfileScreen(onLoggedOut: () -> Unit, vm: ProfileViewModel = hiltViewModel()) {
    val me by vm.me.collectAsState(initial = null)
    ProfileContent(
        name = me?.displayName ?: "-",
        photo = me?.photoUrl?.ifBlank { null },
        busy = vm.busy,
        onPickPhoto = { uri -> vm.updatePhoto(uri) },
        onChangeName = { vm.name = it },
        curNameEdit = if (vm.name.isEmpty()) me?.displayName ?: "-" else vm.name,
        onSaveName = { vm.saveName() },
        onLogout = { vm.logout(); onLoggedOut() }
    )
}

@Composable
private fun ProfileContent(
    name: String,
    photo: String?,
    busy: Boolean,
    curNameEdit: String,
    onPickPhoto: (Uri) -> Unit,
    onChangeName: (String) -> Unit,
    onSaveName: () -> Unit,
    onLogout: () -> Unit
) {
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let(onPickPhoto) }
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Profil") }) }) { inner ->
        Column(Modifier.padding(inner).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (!photo.isNullOrBlank())
                AsyncImage(model = photo, contentDescription = null, modifier = Modifier.size(96.dp).clip(CircleShape))
            else
                Box(Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Text(name.take(1).uppercase())
                }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { pick.launch("image/*") }, enabled = !busy) { Text("Ubah Foto") }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = curNameEdit, onValueChange = onChangeName, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = onSaveName, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Simpan") }
            Spacer(Modifier.height(24.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Logout") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    MaterialTheme {
        ProfileContent(
            name = "Koko", photo = null, busy = false, curNameEdit = "Koko",
            onPickPhoto = {}, onChangeName = {}, onSaveName = {}, onLogout = {}
        )
    }
}


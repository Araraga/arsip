@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arsip.R // <-- SESUAIKAN DENGAN NAMA PAKET APLIKASI ANDA

@Composable
fun AuthScreen(
    onAuthed: () -> Unit,
    onPickMap: () -> Unit = {},
    vm: AuthViewModel = hiltViewModel()
) {
    AuthContent(
        isRegister = vm.isRegister,
        email = vm.email,
        pass = vm.pass,
        name = vm.name,
        phoneNumber = vm.phoneNumber,
        addressText = vm.addressText,
        busy = vm.busy,
        error = vm.error,
        onToggle = { vm.isRegister = !vm.isRegister },
        onEmail = { vm.email = it },
        onPass = { vm.pass = it },
        onName = { vm.name = it },
        onPhoneNumber = { vm.phoneNumber = it },
        onAddressText = { vm.addressText = it },
        onPickMap = onPickMap,
        onSubmit = { vm.submit(onAuthed) },
        onGuest = { vm.guest(onAuthed) }
    )
}

@Composable
private fun AuthHeader(isRegister: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStartPercent = 50, bottomEndPercent = 50)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stack (tumpuk) lingkaran putih dan logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp) // Ukuran lingkaran putih, sedikit lebih besar dari logo
                    .background(Color.White, shape = CircleShape)
                    .padding(15.dp) // Padding opsional agar logo tidak terlalu mepet
            ) {
                // Menggunakan Image composable untuk memuat file logo.png
                Image(
                    painter = painterResource(id = R.drawable.logo), // <-- Menggunakan nama file Anda
                    contentDescription = "Logo Aplikasi",
                    modifier = Modifier.fillMaxSize() // Logo akan mengisi Box lingkaran ini
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isRegister) "REGISTER" else "LOGIN",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun AuthContent(
    isRegister: Boolean,
    email: String,
    pass: String,
    name: String,
    phoneNumber: String,
    addressText: String,
    busy: Boolean,
    error: String?,
    onToggle: () -> Unit,
    onEmail: (String) -> Unit,
    onPass: (String) -> Unit,
    onName: (String) -> Unit,
    onPhoneNumber: (String) -> Unit,
    onAddressText: (String) -> Unit,
    onSubmit: () -> Unit,
    onGuest: () -> Unit,
    onPickMap: () -> Unit = {}
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AuthHeader(isRegister = isRegister)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = if (isRegister) "Silakan isi data diri Anda" else "Masuk untuk melanjutkan",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            // Error message display
            error?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            if (isRegister) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onName,
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !busy
                )
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = onEmail,
                label = { Text("Alamat Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !busy
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = pass,
                onValueChange = onPass,
                label = { Text("Kata Sandi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !busy,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image: ImageVector
                    val description: String
                    if (passwordVisible) {
                        image = Icons.Filled.Visibility
                        description = "Sembunyikan kata sandi"
                    } else {
                        image = Icons.Filled.VisibilityOff
                        description = "Tampilkan kata sandi"
                    }

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                }
            )
            Spacer(Modifier.height(16.dp))

            if (isRegister) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumber,
                    label = { Text("Nomor WhatsApp (08xxxxxxxxx)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    enabled = !busy,
                    placeholder = { Text("08123456789") }
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = addressText,
                    onValueChange = onAddressText,
                    label = { Text("Alamat Lengkap") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    enabled = !busy,
                    placeholder = { Text("Jl. Contoh No. 123, Kota, Provinsi") }
                )
                Spacer(Modifier.height(16.dp))

                // Tombol untuk memilih lokasi di peta
                OutlinedButton(
                    onClick = onPickMap,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy
                ) {
                    Text("📍 Pilih Lokasi di Peta")
                }
                Spacer(Modifier.height(24.dp))
            }

            Button(
                onClick = onSubmit,
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isRegister) "DAFTAR SEKARANG" else "MASUK")
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onToggle, enabled = !busy) {
                    Text(if (isRegister) "Sudah punya akun? Masuk" else "Belum punya akun? Daftar")
                }
            }

            if (!isRegister) {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onGuest, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                    Text("Masuk Sebagai Tamu", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// --- Preview Composable (Tidak perlu diubah) ---

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun AuthPreviewLogin() {
    MaterialTheme {
        AuthContent(
            isRegister = false,
            email = "test@example.com",
            pass = "password",
            name = "",
            phoneNumber = "",
            addressText = "",
            busy = false,
            error = null,
            onToggle = {},
            onEmail = {},
            onPass = {},
            onName = {},
            onPhoneNumber = {},
            onAddressText = {},
            onSubmit = {},
            onGuest = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun AuthPreviewRegister() {
    MaterialTheme {
        AuthContent(
            isRegister = true,
            email = "baru@example.com",
            pass = "secure123",
            name = "Pengguna Baru",
            phoneNumber = "08123456789",
            addressText = "Jl. Contoh Alamat No. 123",
            busy = false,
            error = null,
            onToggle = {},
            onEmail = {},
            onPass = {},
            onName = {},
            onPhoneNumber = {},
            onAddressText = {},
            onSubmit = {},
            onGuest = {}
        )
    }
}
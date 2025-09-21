@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.arsip.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AuthScreen(onAuthed: () -> Unit, vm: AuthViewModel = hiltViewModel()) {
    AuthContent(
        isRegister = vm.isRegister,
        email = vm.email,
        pass  = vm.pass,
        name  = vm.name,
        busy  = vm.busy,
        onToggle = { vm.isRegister = !vm.isRegister },
        onEmail  = { vm.email = it },
        onPass   = { vm.pass = it },
        onName   = { vm.name = it },
        onSubmit = { vm.submit(onAuthed) },
        onGuest  = { vm.guest(onAuthed) }
    )
}

@Composable
private fun AuthContent(
    isRegister: Boolean,
    email: String,
    pass: String,
    name: String,
    busy: Boolean,
    onToggle: () -> Unit,
    onEmail: (String) -> Unit,
    onPass: (String) -> Unit,
    onName: (String) -> Unit,
    onSubmit: () -> Unit,
    onGuest: () -> Unit
) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text(if (isRegister) "Register" else "Login") }) }) { inner ->
        Column(Modifier.padding(inner).padding(16.dp)) {
            if (isRegister) {
                OutlinedTextField(value = name, onValueChange = onName, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }
            OutlinedTextField(value = email, onValueChange = onEmail, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = pass, onValueChange = onPass, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = onSubmit, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(if (isRegister) "Register" else "Login") }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onGuest, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Masuk Tanpa Akun") }
            TextButton(onClick = onToggle) { Text(if (isRegister) "Sudah punya akun? Login" else "Belum punya akun? Register") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthPreviewLogin() {
    MaterialTheme {
        AuthContent(isRegister = false, email = "", pass = "", name = "", busy = false,
            onToggle = {}, onEmail = {}, onPass = {}, onName = {}, onSubmit = {}, onGuest = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthPreviewRegister() {
    MaterialTheme {
        AuthContent(isRegister = true, email = "", pass = "", name = "Koko", busy = false,
            onToggle = {}, onEmail = {}, onPass = {}, onName = {}, onSubmit = {}, onGuest = {})
    }
}

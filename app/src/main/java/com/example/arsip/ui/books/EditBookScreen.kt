package com.example.arsip.ui.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.arsip.data.BookCategories
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookScreen(
    navController: NavController,
    onPickMap: () -> Unit, // Callback untuk membuka peta
    vm: EditBookViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val book = vm.book

    LaunchedEffect(vm.message) {
        vm.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.message = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Buku") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { scope.launch { vm.saveBook { navController.popBackStack() } } },
                icon = { Icon(Icons.Outlined.Check, contentDescription = null) },
                text = { Text("Simpan Perubahan") }
            )
        }
    ) { innerPadding ->
        if (book == null) {
            // Tampilan loading saat data buku sedang diambil
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Form edit di dalam LazyColumn
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = vm.title,
                        onValueChange = { vm.title = it },
                        label = { Text("Judul Buku") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = vm.author,
                        onValueChange = { vm.author = it },
                        label = { Text("Penulis") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = vm.desc,
                        onValueChange = { vm.desc = it },
                        label = { Text("Deskripsi (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5
                    )
                }
                item {
                    // Dropdown untuk edit kategori buku
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = if (vm.selectedCategory.isEmpty()) "Pilih Kategori" else vm.selectedCategory,
                            onValueChange = {},
                            label = { Text("Kategori Buku") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Pilih Kategori"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            // Daftar kategori buku diambil dari BookCategories
                            BookCategories.ALL_CATEGORIES.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        vm.selectedCategory = category
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = vm.addressText,
                        onValueChange = { vm.addressText = it },
                        label = { Text("Alamat") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onPickMap,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pilih Ulang Lokasi di Peta")
                    }
                }
                // Spacer agar tidak tertutup FAB
                item {
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}
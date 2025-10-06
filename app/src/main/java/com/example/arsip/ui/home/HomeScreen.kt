package com.example.arsip.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onClickBook: (String) -> Unit,
    onAddBook: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var showLocationFilter by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            vm.onLocationPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            vm.onLocationPermissionGranted()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBook,
                containerColor = Color(0xFF3700B3),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Buku")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFBB86FC),
                                    Color(0xFF6200EE)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Text(
                            text = "Halo, ${state.userName} 👋",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Temukan buku di sekitarmu atau bagikan koleksimu",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { vm.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Cari judul, penulis, atau tag…") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5)
                    ),
                    singleLine = true
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedFilter == "terdekat",
                            onClick = { vm.onFilterSelect("terdekat") },
                            label = { Text("Terdekat") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = state.selectedFilter == "terbaru",
                            onClick = { vm.onFilterSelect("terbaru") },
                            label = { Text("Terbaru") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = state.selectedFilter == "populer",
                            onClick = { vm.onFilterSelect("populer") },
                            label = { Text("Populer") }
                        )
                    }
                }
            }

            if (state.highlightedBooks.isNotEmpty()) {
                item {
                    Text(
                        text = "✨ Pilihan Terbaik",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(state.highlightedBooks) { book ->
                                Card(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(200.dp)
                                        .clickable { onClickBook(book.id) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box {
                                        AsyncImage(
                                            model = book.imageUrls.firstOrNull(),
                                            contentDescription = book.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = book.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "📍 Buku di Sekitar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (!state.isLoading && state.nearbyBooks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada buku di sekitar",
                            color = Color.Gray
                        )
                    }
                }
            }

            if (!state.isLoading && state.nearbyBooks.isNotEmpty()) {
                items(
                    items = state.nearbyBooks,
                    key = { book -> book.id }
                ) { book ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onClickBook(book.id) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            AsyncImage(
                                model = book.imageUrls.firstOrNull(),
                                contentDescription = book.title,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = book.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )
                                if (book.author.isNotBlank()) {
                                    Text(
                                        text = book.author,
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = state.calculateDistance(book),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            IconButton(
                                onClick = { onClickBook(book.id) },
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Detail",
                                    tint = Color(0xFF6200EE)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

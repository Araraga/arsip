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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .offset(y = (-60).dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF6200EE),
                                        Color(0xFF9D4EDD),
                                        Color(0xFFBB86FC)
                                    ),
                                    startY = 0f,
                                    endY = 800f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 24.dp, bottom = 80.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Halo, ${state.userName} 👋",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Temukan buku di sekitarmu atau bagikan koleksimu",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.95f),
                                    lineHeight = 20.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .offset(y = (-80).dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = Color(0xFF6200EE).copy(alpha = 0.25f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF6200EE),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = state.searchQuery,
                            onValueChange = { vm.onSearchQueryChange(it) },
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 16.sp,
                                color = Color(0xFF1C1B1F)
                            ),
                            decorationBox = { innerTextField ->
                                if (state.searchQuery.isEmpty()) {
                                    Text(
                                        "Cari judul, penulis, atau tag...",
                                        fontSize = 16.sp,
                                        color = Color(0xFF79747E)
                                    )
                                }
                                innerTextField()
                            },
                            singleLine = true
                        )
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    modifier = Modifier.offset(y = (-60).dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedFilter == "terdekat",
                            onClick = { vm.onFilterSelect("terdekat") },
                            label = {
                                Text(
                                    "📍 Terdekat",
                                    fontWeight = if (state.selectedFilter == "terdekat") FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6200EE),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF3E8FF)
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = state.selectedFilter == "terbaru",
                            onClick = { vm.onFilterSelect("terbaru") },
                            label = {
                                Text(
                                    "🕒 Terbaru",
                                    fontWeight = if (state.selectedFilter == "terbaru") FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6200EE),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF3E8FF)
                            )
                        )
                    }
                }
            }

            if (state.highlightedBooks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .offset(y = (-50).dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Pilihan Terbaik",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F)
                            )
                            Text(
                                text = "✨ Rekomendasi untuk kamu",
                                fontSize = 13.sp,
                                color = Color(0xFF79747E),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        modifier = Modifier.offset(y = (-40).dp)
                    ) {
                        items(state.highlightedBooks) { book ->
                            Card(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(240.dp)
                                    .clickable { onClickBook(book.id) },
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = book.imageUrls.firstOrNull(),
                                        contentDescription = book.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Gradient Overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.7f)
                                                    ),
                                                    startY = 200f
                                                )
                                            )
                                    )

                                    Text(
                                        text = book.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 2,
                                        lineHeight = 18.sp,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .offset(y = if (state.highlightedBooks.isNotEmpty()) (-10).dp else (-30).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Buku di Sekitar",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1F)
                        )
                        Text(
                            text = "📍 Tersedia untuk dipinjam",
                            fontSize = 13.sp,
                            color = Color(0xFF79747E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .offset(y = (-10).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF6200EE),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            if (!state.isLoading && state.nearbyBooks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .offset(y = (-10).dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF3E8FF)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📚",
                                fontSize = 56.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Belum ada buku tersedia",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Coba ubah filter atau bagikan buku Anda untuk memulai berbagi dengan komunitas!",
                                fontSize = 15.sp,
                                color = Color(0xFF49454F),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
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
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .offset(y = (-10).dp)
                            .clickable { onClickBook(book.id) },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                modifier = Modifier.size(90.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                AsyncImage(
                                    model = book.imageUrls.firstOrNull(),
                                    contentDescription = book.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = book.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1B1F),
                                    maxLines = 2,
                                    lineHeight = 22.sp
                                )

                                if (book.author.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = book.author,
                                        fontSize = 14.sp,
                                        color = Color(0xFF49454F),
                                        maxLines = 1,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF6200EE),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = state.calculateDistance(book),
                                        fontSize = 13.sp,
                                        color = Color(0xFF6200EE),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "Detail",
                                tint = Color(0xFF6200EE),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        FloatingActionButton(
            onClick = onAddBook,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .navigationBarsPadding(),
            containerColor = Color(0xFF6200EE),
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Tambah Buku",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip.ui.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun DiscoverScreen(
    onClickBook: (String) -> Unit,
    vm: DiscoverViewModel = hiltViewModel()
) {
    val items by vm.filtered.collectAsState()
    val categories by vm.categories.collectAsState()
    val sort by vm.sort.collectAsState()
    val query by vm.query.collectAsState()
    val selectedCategory by vm.selectedCategory.collectAsState()

    val ctx = LocalContext.current
    LaunchedEffect(Unit) { Configuration.getInstance().userAgentValue = ctx.packageName }

    var showMap by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFBB86FC),
                            Color(0xFF6200EE)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Explore,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Temukan Buku Baru",
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    color = Color.White
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        // Search at the top
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari judul/kategori") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Explore, contentDescription = null) }
            )
        }
        // Controls below: Terdekat (sort), Kategori, Peta
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SortMenu(current = sort, onChange = { vm.sort.value = it })
            Spacer(Modifier.width(8.dp))
            CategoryMenu(
                selected = selectedCategory,
                onChange = { vm.selectedCategory.value = it },
                categories = categories
            )
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = {
                showMap = !showMap
                if (showMap) vm.sort.value = DiscoverSort.NEAREST
            }) { Text(if (showMap) "Daftar" else "Peta") }
        }
        if (showMap) {
            DiscoverMap(
                books = items,
                userLat = vm.userLat,
                userLng = vm.userLng,
                onClickBook = onClickBook
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items) { book ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { onClickBook(book.id) },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            AsyncImage(
                                model = book.imageUrls.firstOrNull(),
                                contentDescription = book.title,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = book.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF6200EE)
                                )
                                if (book.author.isNotBlank()) {
                                    Text(
                                        text = book.author,
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF6200EE)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortMenu(current: DiscoverSort, onChange: (DiscoverSort) -> Unit) {
    val expanded = remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded.value = true }) {
            Text(
                text = when (current) {
                    DiscoverSort.NEWEST -> "Terbaru"
                    DiscoverSort.NEAREST -> "Terdekat"
                }
            )
        }
        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            DropdownMenuItem(text = { Text("Terbaru") }, onClick = { onChange(DiscoverSort.NEWEST); expanded.value = false })
            DropdownMenuItem(text = { Text("Terdekat") }, onClick = { onChange(DiscoverSort.NEAREST); expanded.value = false })
        }
    }
}

@Composable
private fun DiscoverMap(
    books: List<com.example.arsip.data.Book>,
    userLat: Double?,
    userLng: Double?,
    onClickBook: (String) -> Unit
) {
    val ctx = LocalContext.current
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            MapView(ctx).apply {
                TileSourceFactory.MAPNIK.also { setTileSource(it) }
                controller.setZoom(13.0)
                val center = if (userLat != null && userLng != null) GeoPoint(userLat, userLng) else GeoPoint(-6.2, 106.816666)
                controller.setCenter(center)

                // Simpan markers dalam tag agar bisa di-update
                tag = mutableListOf<Marker>()
            }
        },
        update = { map ->
            // bersihkan markers lama
            (map.tag as? MutableList<Marker>)?.forEach { m -> map.overlays.remove(m) }
            val stored = mutableListOf<Marker>()
            books.forEach { b ->
                val la = b.lat; val ln = b.lng
                if (la != null && ln != null) {
                    val m = Marker(map).apply {
                        position = GeoPoint(la, ln)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = b.title
                        setOnMarkerClickListener { _, _ -> onClickBook(b.id); true }
                    }
                    map.overlays.add(m)
                    stored.add(m)
                }
            }
            map.tag = stored
            map.invalidate()
        }
    )
}

@Composable
private fun CategoryMenu(selected: String, onChange: (String) -> Unit, categories: List<String>) {
    val expanded = remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded.value = true }) {
            Text(text = if (selected.isBlank()) "Kategori" else selected)
        }
        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            DropdownMenuItem(text = { Text("Semua") }, onClick = { onChange(""); expanded.value = false })
            categories.forEach { c ->
                DropdownMenuItem(text = { Text(c) }, onClick = { onChange(c); expanded.value = false })
            }
        }
    }
}

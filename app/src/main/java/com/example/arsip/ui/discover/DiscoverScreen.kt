@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip.ui.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
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
            LazyColumn(Modifier.fillMaxSize()) {
                items(items) { b ->
                    ListItem(
                        headlineContent = { Text(b.title) },
                        supportingContent = { Text(b.category.ifBlank { "Tanpa kategori" }) },
                        leadingContent = {
                            val first = b.imageUrls.firstOrNull()
                            if (first != null) AsyncImage(model = first, contentDescription = null, modifier = Modifier.size(56.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClickBook(b.id) }
                    )
                    Divider()
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
                org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK.also { setTileSource(it) }
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



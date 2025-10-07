@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip.ui.map

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.net.URLEncoder
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun MapPickerScreen(nav: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { Configuration.getInstance().userAgentValue = ctx.packageName }

    // --- State utama ---
    var point by remember { mutableStateOf(GeoPoint(-6.200000, 106.816666)) } // Jakarta default
    var address by remember { mutableStateOf<String?>(null) }

    // --- State search kota & suggestion ---
    var cityQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<CitySuggestion>>(emptyList()) }
    var loadingSuggest by remember { mutableStateOf(false) }

    // Debounce fetch suggestion saat user mengetik
    LaunchedEffect(cityQuery) {
        val q = cityQuery.trim()
        if (q.length < 2) {
            suggestions = emptyList()
            return@LaunchedEffect
        }
        loadingSuggest = true
        delay(320)
        suggestions = runCatching { fetchCitySuggestions(ctx, q) }.getOrElse { emptyList() }
        loadingSuggest = false
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Pilih Lokasi") }) },
        bottomBar = {
            Surface(tonalElevation = 2.dp, shadowElevation = 8.dp, modifier = Modifier.imePadding()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        address ?: "Tap peta untuk memilih",
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Button(onClick = {
                        nav.previousBackStackEntry?.savedStateHandle?.set("picked_lat", point.latitude)
                        nav.previousBackStackEntry?.savedStateHandle?.set("picked_lng", point.longitude)
                        nav.previousBackStackEntry?.savedStateHandle?.set("picked_address", address ?: "")
                        nav.navigateUp()
                    }) { Text("Pilih") }
                }
            }
        }
    ) { inner ->
        // Layering: Map di belakang, UI floating di depan (Box: urutan terakhir = paling depan)
        Box(
            Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // --- Peta ---
            AndroidMap(
                modifier = Modifier.fillMaxSize(),
                context = ctx,
                center = point,
                onPick = { gp ->
                    point = gp
                    scope.launch {
                        address = reverseGeocodeNominatim(ctx, gp.latitude, gp.longitude)
                    }
                }
            )

            // --- Search floating bar + suggestions (di depan peta) ---
            Column(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Kartu “mengambang” di atas peta
                Surface(
                    shape = MaterialTheme.shapes.large,
                    shadowElevation = 12.dp,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = cityQuery,
                            onValueChange = { cityQuery = it },
                            label = { Text("Kota (mis. Bandung)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        )
                        Button(
                            onClick = {
                                val q = cityQuery.trim()
                                if (q.isNotEmpty()) {
                                    scope.launch {
                                        loadingSuggest = true
                                        geocodeCity(ctx, q)?.let { gp ->
                                            point = gp
                                            address = reverseGeocodeNominatim(ctx, gp.latitude, gp.longitude)
                                        }
                                        loadingSuggest = false
                                    }
                                }
                            }
                        ) { Text("Cari") }
                    }
                }

                // Dropdown suggestions (jika ada)
                if (loadingSuggest || suggestions.isNotEmpty()) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        shadowElevation = 10.dp,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        if (loadingSuggest) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Mencari kota…")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 260.dp)
                            ) {
                                items(suggestions) { s ->
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                // pilih suggestion -> pindah peta & isi alamat
                                                val gp = GeoPoint(s.lat, s.lon)
                                                point = gp
                                                cityQuery = s.primary // isi input dengan pilihan
                                                scope.launch {
                                                    address = reverseGeocodeNominatim(ctx, s.lat, s.lon)
                                                }
                                                suggestions = emptyList() // tutup dropdown
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Text(s.primary, style = MaterialTheme.typography.titleSmall)
                                        if (s.secondary.isNotEmpty()) {
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                s.secondary,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ===================== MAP ===================== */

@Composable
private fun AndroidMap(
    modifier: Modifier,
    context: Context,
    center: GeoPoint,
    onPick: (GeoPoint) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(16.0)
                controller.setCenter(center)

                // Marker awal
                val marker = Marker(this).apply {
                    position = center
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                overlays.add(marker)
                tag = marker

                // Tap di peta -> pindah marker
                val events = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        (tag as Marker).position = p
                        onPick(p)
                        invalidate()
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint) = false
                }
                overlays.add(MapEventsOverlay(events))
            }
        },
        update = { map ->
            val m = map.tag as Marker
            if (m.position != center) {
                m.position = center
                map.controller.setCenter(center)
                map.invalidate()
            }
        }
    )
}

/* ===================== GEOCODING (Nominatim) ===================== */

private data class CitySuggestion(
    val primary: String,   // nama kota singkat
    val secondary: String, // baris alamat tambahan
    val lat: Double,
    val lon: Double
)

private suspend fun fetchCitySuggestions(ctx: Context, q: String): List<CitySuggestion> =
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val qq = URLEncoder.encode(q, Charsets.UTF_8.name())
        val url = "https://nominatim.openstreetmap.org/search?format=jsonv2&addressdetails=1&" +
                "limit=8&accept-language=id&q=$qq"
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "${ctx.packageName} (contact: you@example.com)")
            .build()
        runCatching {
            client.newCall(req).execute().use { res ->
                if (!res.isSuccessful) return@use emptyList<CitySuggestion>()
                val arr = JSONArray(res.body?.string() ?: "[]")
                buildList {
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        val addr = o.optJSONObject("address")
                        val cityLike = addr?.optString("city")
                            ?: addr?.optString("town")
                            ?: addr?.optString("village")
                            ?: ""
                        val state = addr?.optString("state") ?: ""
                        val country = addr?.optString("country") ?: ""
                        val primary = when {
                            cityLike.isNotBlank() -> cityLike
                            else -> o.optString("display_name").split(",").firstOrNull()?.trim().orEmpty()
                        }
                        val secondary = listOfNotNull(
                            addr?.optString("county")?.takeIf { it.isNotBlank() },
                            state.takeIf { it.isNotBlank() },
                            country.takeIf { it.isNotBlank() }
                        ).joinToString(" · ")

                        add(
                            CitySuggestion(
                                primary = primary,
                                secondary = secondary,
                                lat = o.getString("lat").toDouble(),
                                lon = o.getString("lon").toDouble()
                            )
                        )
                    }
                }
            }
        }.getOrElse { emptyList() }
    }

private suspend fun reverseGeocodeNominatim(ctx: Context, lat: Double, lon: Double): String? =
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=$lat&lon=$lon&accept-language=id"
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "${ctx.packageName} (contact: you@example.com)")
            .build()
        runCatching {
            client.newCall(req).execute().use { res ->
                if (!res.isSuccessful) return@use null
                val body = res.body?.string() ?: return@use null
                JSONObject(body).optString("display_name")
            }
        }.getOrNull()
    }

private suspend fun geocodeCity(ctx: Context, city: String): GeoPoint? =
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val q = URLEncoder.encode(city, Charsets.UTF_8.name())
        val url = "https://nominatim.openstreetmap.org/search?format=jsonv2&limit=1&accept-language=id&q=$q"
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "${ctx.packageName} (contact: you@example.com)")
            .build()
        runCatching {
            client.newCall(req).execute().use { res ->
                if (!res.isSuccessful) return@use null
                val body = res.body?.string() ?: return@use null
                val arr = JSONArray(body)
                if (arr.length() == 0) return@use null
                val obj = arr.getJSONObject(0)
                GeoPoint(obj.getString("lat").toDouble(), obj.getString("lon").toDouble())
            }
        }.getOrNull()
    }

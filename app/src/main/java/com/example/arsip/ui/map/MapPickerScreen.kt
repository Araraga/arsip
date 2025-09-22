@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip.ui.map

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
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
import androidx.compose.foundation.layout.imePadding
import java.net.URLEncoder

@Composable
fun MapPickerScreen(nav: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // User-Agent untuk OSMDroid
    LaunchedEffect(Unit) { Configuration.getInstance().userAgentValue = ctx.packageName }

    // State
    var point by remember { mutableStateOf(GeoPoint(-6.200000, 106.816666)) } // default Jakarta
    var address by remember { mutableStateOf<String?>(null) }
    var cityQuery by remember { mutableStateOf("") } // <-- tanpa rememberSaveable

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Pilih Lokasi") }) },
        bottomBar = {
            Surface(tonalElevation = 2.dp, shadowElevation = 8.dp, modifier = Modifier.imePadding()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
        Column(Modifier.padding(inner).fillMaxSize()) {
            // Pencarian kota
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = cityQuery,
                    onValueChange = { cityQuery = it },
                    label = { Text("Kota (mis. Bandung)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                )
                Button(onClick = {
                    val q = cityQuery.trim()
                    if (q.isNotEmpty()) {
                        scope.launch {
                            geocodeCity(ctx, q)?.let { gp ->
                                point = gp
                                address = reverseGeocodeNominatim(ctx, gp.latitude, gp.longitude)
                            }
                        }
                    }
                }) { Text("Cari") }
            }
            // (opsional) bantu teks kecil di bawah input
            Text(
                "Ketik nama kota lalu tekan Cari",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
            )

            // Peta
            AndroidMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                context = ctx,
                center = point,
                onPick = { gp ->
                    point = gp
                    scope.launch {
                        address = reverseGeocodeNominatim(ctx, gp.latitude, gp.longitude)
                    }
                }
            )
        }
    }
}

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

                val marker = Marker(this).apply {
                    position = center
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                overlays.add(marker)
                tag = marker

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

// ---- Geocoding util ----

private suspend fun reverseGeocodeNominatim(ctx: Context, lat: Double, lon: Double): String? =
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=$lat&lon=$lon"
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
        val url = "https://nominatim.openstreetmap.org/search?format=jsonv2&q=$q&limit=1"
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
                val lat = obj.getString("lat").toDouble()
                val lon = obj.getString("lon").toDouble()
                GeoPoint(lat, lon)
            }
        }.getOrNull()
    }

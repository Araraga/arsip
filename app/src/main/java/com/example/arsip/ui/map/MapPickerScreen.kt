@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip.ui.map

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun MapPickerScreen(nav: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Set user-agent OSMDroid (WAJIB agar tidak diblokir)
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = ctx.packageName
    }

    var point by remember { mutableStateOf(GeoPoint(-6.200000, 106.816666)) } // Jakarta default
    var address by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Pilih Lokasi") }) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(address ?: "Tap peta untuk memilih")
                    Button(onClick = {
                        nav.previousBackStackEntry?.savedStateHandle?.set("picked_lat", point.latitude)
                        nav.previousBackStackEntry?.savedStateHandle?.set("picked_lng", point.longitude)
                        nav.previousBackStackEntry?.savedStateHandle?.set("picked_address", address ?: "")
                        nav.navigateUp()
                    }) { Text("Simpan") }
                }
            }
        }
    ) { inner ->
        AndroidMap(
            modifier = Modifier.fillMaxSize().padding(inner),
            context = ctx,
            initial = point,
            onPick = { gp ->
                point = gp
                // ⬇️ JANGAN pakai LaunchedEffect di sini. Ini di luar konteks composable.
                scope.launch {
                    address = reverseGeocodeNominatim(ctx, gp.latitude, gp.longitude)
                }
            }
        )
    }
}

@Composable
private fun AndroidMap(
    modifier: Modifier,
    context: Context,
    initial: GeoPoint,
    onPick: (GeoPoint) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(16.0)
                controller.setCenter(initial)

                val marker = Marker(this).apply {
                    position = initial
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                overlays.add(marker)

                val events = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        marker.position = p
                        onPick(p)      // panggil callback ke Compose state
                        invalidate()
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint) = false
                }
                overlays.add(MapEventsOverlay(events))
            }
        }
    )
}

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

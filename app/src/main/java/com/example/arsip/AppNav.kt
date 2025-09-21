@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.arsip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arsip.ui.auth.AuthScreen
import com.example.arsip.ui.books.AddBookScreen
import com.example.arsip.ui.books.AddBookViewModel
import com.example.arsip.ui.books.MyBooksScreen
import com.example.arsip.ui.map.MapPickerScreen
import com.example.arsip.ui.profile.ProfileScreen

@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "auth") {

        composable("auth") {
            AuthScreen(onAuthed = {
                nav.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                    launchSingleTop = true
                }
            })
        }

        composable("home") {
            var tab by rememberSaveable { mutableStateOf(0) }
            Scaffold(
                topBar = { CenterAlignedTopAppBar(title = { Text(if (tab == 0) "Buku Saya" else "Profil") }) },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = tab == 0,
                            onClick = { tab = 0 },
                            label = { Text("Buku Saya") },
                            icon = { Icon(Icons.Outlined.LibraryBooks, null) }
                        )
                        NavigationBarItem(
                            selected = tab == 1,
                            onClick = { tab = 1 },
                            label = { Text("Profil") },
                            icon = { Icon(Icons.Outlined.Person, null) }
                        )
                    }
                },
                floatingActionButton = {
                    if (tab == 0) {
                        FloatingActionButton(onClick = { nav.navigate("add") { launchSingleTop = true } }) {
                            Text("+")
                        }
                    }
                }
            ) { pv ->
                Box(Modifier.padding(pv)) {
                    if (tab == 0) {
                        MyBooksScreen(onAdd = { nav.navigate("add") { launchSingleTop = true } })
                    } else {
                        ProfileScreen(onLoggedOut = {
                            nav.navigate("auth") { popUpTo(0) }
                        })
                    }
                }
            }
        }

        // Tambah Buku
        composable("add") { entry ->
            val vm: AddBookViewModel = hiltViewModel()

            // Ambil hasil dari MapPicker SEKALI via get()
            LaunchedEffect(Unit) {
                val lat = entry.savedStateHandle.get<Double>("picked_lat")
                val lng = entry.savedStateHandle.get<Double>("picked_lng")
                val addr = entry.savedStateHandle.get<String>("picked_address")

                if (lat != null && lng != null) vm.setManualLatLng(lat, lng)
                if (!addr.isNullOrBlank()) vm.setManualAddress(addr)

                // bersihkan supaya tidak terpakai ulang
                entry.savedStateHandle.remove<Double>("picked_lat")
                entry.savedStateHandle.remove<Double>("picked_lng")
                entry.savedStateHandle.remove<String>("picked_address")
            }

            AddBookScreen(
                onDone = { nav.popBackStack() },
                onPickMap = { nav.navigate("pickLocation") },
                vm = vm
            )
        }

        // Picker peta (OSM)
        composable("pickLocation") {
            MapPickerScreen(nav = nav)
        }
    }
}

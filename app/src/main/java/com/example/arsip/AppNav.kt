@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.arsip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arsip.ui.auth.AuthScreen
import com.example.arsip.ui.books.*
import com.example.arsip.ui.map.MapPickerScreen
import com.example.arsip.ui.profile.ProfileScreen
import com.example.arsip.ui.profile.ProfileViewModel

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

        composable("home") { entry ->
            var tab by rememberSaveable { mutableStateOf(0) }
            val profileVm: ProfileViewModel = hiltViewModel()

            // LaunchedEffect untuk menerima data dari MapPicker untuk halaman Profil
            LaunchedEffect(key1 = entry.savedStateHandle) {
                if (tab == 1) { // Hanya proses jika sedang di tab Profil
                    val lat = entry.savedStateHandle.get<Double>("picked_lat")
                    val lng = entry.savedStateHandle.get<Double>("picked_lng")

                    if (lat != null && lng != null) {
                        profileVm.onLatLngSelected(lat, lng)

                        // Bersihkan state setelah dipakai
                        entry.savedStateHandle.remove<Double>("picked_lat")
                        entry.savedStateHandle.remove<Double>("picked_lng")
                    }
                }
            }

            Scaffold(
                // topBar dihapus agar tidak ada judul ganda
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
                        MyBooksScreen(onClickBook = { bookId ->
                            nav.navigate("detail/$bookId")
                        })
                    } else {
                        ProfileScreen(
                            onLoggedOut = {
                                nav.navigate("auth") { popUpTo(0) }
                            },
                            onPickMap = {
                                nav.navigate("pickLocation")
                            },
                            vm = profileVm
                        )
                    }
                }
            }
        }

        composable("add") { entry ->
            val vm: AddBookViewModel = hiltViewModel()

            LaunchedEffect(entry.savedStateHandle) {
                val lat = entry.savedStateHandle.get<Double>("picked_lat")
                val lng = entry.savedStateHandle.get<Double>("picked_lng")
                val addr = entry.savedStateHandle.get<String>("picked_address")

                if (lat != null && lng != null) vm.setManualLatLng(lat, lng)
                if (!addr.isNullOrBlank()) vm.setManualAddress(addr)

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

        composable("detail/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookDetailScreen(navController = nav, bookId = bookId)
        }

        composable("edit/{bookId}") { entry ->
            val vm: EditBookViewModel = hiltViewModel()

            LaunchedEffect(entry.savedStateHandle) {
                val newLat = entry.savedStateHandle.get<Double>("picked_lat")
                val newLng = entry.savedStateHandle.get<Double>("picked_lng")
                val newAddr = entry.savedStateHandle.get<String>("picked_address")

                if (newLat != null && newLng != null && newAddr != null) {
                    vm.onAddressUpdate(newAddr, newLat, newLng)

                    entry.savedStateHandle.remove<Double>("picked_lat")
                    entry.savedStateHandle.remove<Double>("picked_lng")
                    entry.savedStateHandle.remove<String>("picked_address")
                }
            }

            EditBookScreen(
                navController = nav,
                onPickMap = { nav.navigate("pickLocation") },
                vm = vm
            )
        }

        composable("pickLocation") {
            MapPickerScreen(nav = nav)
        }
    }
}
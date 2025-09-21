package com.example.arsip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arsip.ui.auth.AuthScreen
import com.example.arsip.ui.books.AddBookScreen
import com.example.arsip.ui.books.MyBooksScreen
import com.example.arsip.ui.profile.ProfileScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf


@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "auth") {
        composable("auth") {
            AuthScreen(onAuthed = {
                nav.navigate("home") { popUpTo("auth") { inclusive = true } }
            })
        }
        composable("home") {
            var tab by remember { mutableIntStateOf(0) }
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = tab == 0, onClick = { tab = 0 },
                            label = { Text("Buku Saya") }, icon = {}
                        )
                        NavigationBarItem(
                            selected = tab == 1, onClick = { tab = 1 },
                            label = { Text("Profil") }, icon = {}
                        )
                    }
                },
                floatingActionButton = {
                    if (tab == 0) FloatingActionButton(onClick = { nav.navigate("add") }) { Text("+") }
                }
            ) { pv ->
                Box(Modifier.padding(pv)) {
                    if (tab == 0) MyBooksScreen(onAdd = { nav.navigate("add") })
                    else ProfileScreen(onLoggedOut = {
                        nav.navigate("auth") { popUpTo("home") { inclusive = true } }
                    })
                }
            }
        }
        composable("add") { AddBookScreen(onDone = { nav.popBackStack() }) }
    }
}

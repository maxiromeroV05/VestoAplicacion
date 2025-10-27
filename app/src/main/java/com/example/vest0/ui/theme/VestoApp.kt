package com.example.vest0.ui.theme

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.vest0.datastore.UserPreferences
import com.example.vest0.model.Producto
import com.example.vest0.model.Usuario
import com.example.vest0.repository.UsuarioRepositorySQLite

@Composable
fun VestoApp() {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val emailGuardado by prefs.getEmail.collectAsState(initial = null)

    val navController = rememberNavController()
    val usuarioRegistrado = remember { mutableStateOf<Usuario?>(null) }
    val usuarioLogueado = remember { mutableStateOf(false) }
    val productosMap = remember { mutableStateMapOf<String, Producto>() }

    LaunchedEffect(emailGuardado) {
        if (emailGuardado != null) {
            val repo = UsuarioRepositorySQLite(context)
            val usuario = repo.obtenerPorEmail(emailGuardado!!)
            if (usuario != null) {
                usuarioRegistrado.value = usuario
                usuarioLogueado.value = true
                navController.navigate("perfil") {
                    popUpTo("menu") { inclusive = false }
                }
            }
        }
    }

    Scaffold(
        topBar = { VestoTopBar() },
        bottomBar = {
            BottomNavigationBar { route -> navController.navigate(route) }
        }
    ) { innerPadding ->
        AppNavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            usuarioRegistrado = usuarioRegistrado,
            usuarioLogueado = usuarioLogueado,
            productosMap = productosMap
        )
    }
}
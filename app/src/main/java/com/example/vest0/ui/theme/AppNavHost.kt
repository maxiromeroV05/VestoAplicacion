package com.example.vest0.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vest0.model.Producto
import com.example.vest0.model.Usuario
import com.example.vest0.viewmodel.CatalogoScreen
import com.example.vest0.viewmodel.LoginScreen
import com.example.vest0.viewmodel.PerfilScreen
import com.example.vest0.viewmodel.ProductoDetalleScreen
import com.example.vest0.viewmodel.RegistroScreen
import com.example.vest0.viewmodel.UbicacionScreen
import kotlin.collections.get
import kotlin.collections.set

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    usuarioRegistrado: MutableState<Usuario?>,
    usuarioLogueado: MutableState<Boolean>,
    productosMap: SnapshotStateMap<String, Producto>
) {
    NavHost(
        navController = navController,
        startDestination = "menu",
        modifier = modifier
    ) {
        composable("menu") { CentralContent() }

        composable("ropa") {
            CatalogoScreen { producto ->
                val id = producto.nombre
                productosMap[id] = producto
                navController.navigate("detalle/$id")
            }
        }

        composable("detalle/{productoId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("productoId")
            val producto = productosMap[id]
            producto?.let {
                ProductoDetalleScreen(producto = it) {
                    navController.popBackStack()
                }
            }
        }

        composable("ubicacion") { UbicacionScreen() }

        composable("perfil") {
            PerfilScreen(
                usuario = usuarioRegistrado.value,
                logeado = usuarioLogueado.value,
                onIrLogin = { navController.navigate("login") },
                onIrRegistro = { navController.navigate("registro") },
                onCerrarSesion = {
                    usuarioLogueado.value = false
                    usuarioRegistrado.value = null
                    navController.navigate("menu") { popUpTo("menu") { inclusive = true } }
                },
                onEliminarUsuario = {
                    usuarioLogueado.value = false
                    usuarioRegistrado.value = null
                    navController.navigate("menu") { popUpTo("menu") { inclusive = true } }
                }
            )
        }

        composable("registro") {
            RegistroScreen(
                onRegistroExitoso = { usuario ->
                    usuarioRegistrado.value = usuario
                    usuarioLogueado.value = true
                    navController.navigate("perfil") {
                        popUpTo("menu") { inclusive = false }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                usuarioRegistrado = usuarioRegistrado.value,
                onLoginExitoso = { usuario ->
                    usuarioRegistrado.value = usuario
                    usuarioLogueado.value = true
                    navController.navigate("perfil") {
                        popUpTo("menu") { inclusive = false }
                    }
                },
                onIrRegistro = { navController.navigate("registro") }
            )
        }
    }
}
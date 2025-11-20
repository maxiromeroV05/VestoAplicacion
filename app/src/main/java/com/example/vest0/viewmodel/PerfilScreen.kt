package com.example.vest0.viewmodel

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vest0.datastore.UserPreferences
import com.example.vest0.model.Usuario
import com.example.vest0.repository.UsuarioRepositorySQLite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(
    usuario: Usuario?,
    logeado: Boolean,
    onIrLogin: () -> Unit,
    onIrRegistro: () -> Unit,
    onCerrarSesion: () -> Unit,
    onEliminarUsuario: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { UsuarioRepositorySQLite(context) }
    val prefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onIrLogin) {
                Text("INICIAR SESIÓN", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            TextButton(onClick = onIrRegistro) {
                Text("REGÍSTRATE", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("PERFIL DE USUARIO", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(32.dp))

            if (logeado && usuario != null) {
                RegistroCampo(label = "EMAIL", value = usuario.email, onValueChange = {})
                RegistroCampo(label = "NOMBRE", value = usuario.nombre, onValueChange = {})
                RegistroCampo(label = "APELLIDOS", value = usuario.apellido, onValueChange = {})

                Spacer(Modifier.height(24.dp))

                // 🔴 Botón de eliminar cuenta con lógica actualizada
                Button(
                    onClick = {
                        val auth = FirebaseAuth.getInstance()
                        val currentUser = auth.currentUser

                        if (currentUser != null) {
                            // Re-autenticación con email y contraseña del modelo
                            val credential = EmailAuthProvider.getCredential(
                                currentUser.email!!,
                                usuario.contraseña // contraseña guardada en tu modelo
                            )

                            currentUser.reauthenticate(credential).addOnSuccessListener {
                                currentUser.delete()
                                    .addOnSuccessListener {
                                        // 🔐 Eliminado de FirebaseAuth
                                        FirebaseFirestore.getInstance()
                                            .collection("usuarios")
                                            .document(currentUser.uid)
                                            .delete()
                                            .addOnSuccessListener {
                                                // ☁️ Eliminado de Firestore
                                                val eliminado = repo.eliminar(usuario.email)
                                                if (eliminado) {
                                                    scope.launch {
                                                        prefs.clearEmail()
                                                        onEliminarUsuario()
                                                    }
                                                    Toast.makeText(context, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show()
                                                    auth.signOut()
                                                }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "Error al eliminar en Firestore", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error al eliminar en FirebaseAuth", Toast.LENGTH_SHORT).show()
                                    }
                            }.addOnFailureListener {
                                Toast.makeText(context, "Re-autenticación requerida", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)) // rojo elegante
                ) {
                    Text("ELIMINAR CUENTA", color = Color.White)
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        scope.launch {
                            prefs.clearEmail()
                            onCerrarSesion()
                        }
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black) // negro total
                ) {
                    Text("CERRAR SESIÓN", color = Color.White)
                }
            } else {
                Text("No has iniciado sesión.", fontSize = 16.sp)
            }
        }
    }
}
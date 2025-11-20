package com.example.vest0.viewmodel

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
fun LoginScreen(
    onLoginExitoso: (Usuario) -> Unit,
    onIrRegistro: () -> Unit,
    usuarioRegistrado: Usuario?
) {
    val context = LocalContext.current
    val repo = remember { UsuarioRepositorySQLite(context) }
    val prefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var mostrarContrasena by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text("INICIA SESIÓN", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(32.dp))
        // Asumiendo que RegistroCampo es un Composable que has creado. Si no, reemplázalo por un OutlinedTextField
        // RegistroCampo(label = "EMAIL", value = email, onValueChange = { email = it })
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("EMAIL") },
            modifier = Modifier.fillMaxWidth()
        )


        Text("CONTRASEÑA", fontSize = 12.sp, fontWeight = FontWeight.Light, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = contraseña,
            onValueChange = { contraseña = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                    Icon(
                        imageVector = if (mostrarContrasena) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (mostrarContrasena) "Ocultar" else "Mostrar"
                    )
                }
            }
        )

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                if (email.isNotBlank() && contraseña.isNotBlank()) {
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email, contraseña)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener
                                FirebaseFirestore.getInstance()
                                    .collection("usuarios")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener { doc ->
                                        val data = doc.data
                                        if (data != null) {
                                            val usuario = Usuario(
                                                email = data["email"] as? String ?: "",
                                                contraseña = contraseña, // solo para SQLite
                                                nombre = data["nombre"] as? String ?: "",
                                                apellido = data["apellido"] as? String ?: "",
                                                perfilUser = data["perfilUser"] as? String ?: ""
                                            )
                                            val exito = repo.insertar(usuario)
                                            if (exito) {
                                                scope.launch {
                                                    prefs.saveEmail(usuario.email)
                                                    onLoginExitoso(usuario)
                                                }
                                            } else {
                                                // Si el login es exitoso pero falla la inserción (porque ya existe),
                                                // igualmente procedemos con el login.
                                                scope.launch {
                                                    prefs.saveEmail(usuario.email)
                                                    onLoginExitoso(usuario)
                                                }
                                            }
                                        } else {
                                            error = true
                                        }
                                    }
                                    .addOnFailureListener {
                                        error = true
                                    }
                            } else {
                                error = true
                            }
                        }
                } else {
                    error = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("INICIAR SESIÓN", color = Color.White)
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onIrRegistro,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("REGÍSTRATE")
        }
    }
}

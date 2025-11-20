package com.example.vest0.repository

import com.example.vest0.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UsuarioRepositorio {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // 👉 Registrar usuario en FirebaseAuth y guardar perfil en Firestore
    fun registrarUsuario(
        usuario: Usuario,
        onResult: (Boolean) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(usuario.email, usuario.contraseña)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val perfil = mapOf(
                        "email" to usuario.email,
                        "nombre" to usuario.nombre,
                        "apellido" to usuario.apellido,
                        "perfilUser" to usuario.perfilUser
                    )
                    firestore.collection("usuarios").document(uid).set(perfil)
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                } else {
                    onResult(false)
                }
            }
    }

    // 👉 Login con email y contraseña
    fun loginUsuario(email: String, contraseña: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, contraseña)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // 👉 Obtener perfil desde Firestore
    fun obtenerPerfil(onResult: (Usuario?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(null)
        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val data = doc.data
                if (data != null) {
                    val usuario = Usuario(
                        email = data["email"] as? String ?: "",
                        contraseña = "", // nunca se guarda en Firestore
                        nombre = data["nombre"] as? String ?: "",
                        apellido = data["apellido"] as? String ?: "",
                        perfilUser = data["perfilUser"] as? String ?: ""
                    )
                    onResult(usuario)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { onResult(null) }
    }

    // 👉 Logout
    fun logout() {
        auth.signOut()
    }

    // 👉 UID del usuario actual
    fun usuarioActual(): String? {
        return auth.currentUser?.uid
    }
}
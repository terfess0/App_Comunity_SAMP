package com.terfess.comunidadmp.clases

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class Utilities {
    val db = FirebaseFirestore.getInstance()
    fun actualizarFotoUsuario(root: View, emailUsuario: String, nuevaFoto: String) {

        val usuarioRef = db.collection("users")

        // Realizar una consulta para obtener el documento del usuario por su nombre
        usuarioRef.whereEqualTo("userEmail", emailUsuario)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Encontrar el primer documento que coincida con el email de usuario
                    val document = querySnapshot.documents[0]
                    val userId = document.id

                    // Actualizar el campo userPhoto del documento del usuario
                    usuarioRef.document(userId)
                        .update("userPhoto", nuevaFoto)
                        .addOnSuccessListener {
                            // Campo userPhoto actualizado exitosamente
                            Snackbar.make(
                                root,
                                "Foto de perfil actualizada.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            // Error al actualizar el campo
                            println("Error al actualizar el campo 'userPhoto' Error: $e")
                            Snackbar.make(
                                root,
                                "Algo salió mal, prueba más tarde",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // No se encontró ningún usuario con el nombre dado
                    println("No se encontró ningún usuario con el email '$emailUsuario'")
                }
            }
            .addOnFailureListener { e ->
                // Error al realizar la consulta
                println("Error al buscar el usuario con email '$emailUsuario': $e")
            }
    }

    fun getUserName(context: Context, userEmail: String) {
        val usuarioRef = db.collection("users")

        usuarioRef
            .whereNotEqualTo("userEmail", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userName = documents.documents[0].getString("userName") ?: ""
                    Utilities().saveSharedpref(context, "userName", userName)
                } else {
                    println("Usuario no encontrado o no tiene nombre.")
                }
            }
            .addOnFailureListener { exception ->
                // Manejar errores
                println("Error al obtener usuario de email: $exception")
            }
    }

    fun setUserName(emailUsuario: String, newName: String) {

        val usuarioRef = db.collection("users")

        // Realizar una consulta para obtener el documento del usuario por su nombre
        usuarioRef.whereEqualTo("userEmail", emailUsuario)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Encontrar el primer documento que coincida con el email de usuario
                    val document = querySnapshot.documents[0]
                    val userId = document.id

                    // Actualizar el campo userPhoto del documento del usuario
                    usuarioRef.document(userId)
                        .update("userName", newName)
                        .addOnSuccessListener {
                            // Campo userPhoto actualizado exitosamente
                            println("Campo 'userName' actualizado para el usuario con correo '$emailUsuario'")
                        }
                        .addOnFailureListener { e ->
                            // Error al actualizar el campo
                            println("Error al actualizar el campo 'userName' para el usuario '$emailUsuario': $e")
                        }
                } else {
                    // No se encontró ningún usuario con el nombre dado
                    println("No se encontró ningún usuario con el email '$emailUsuario'")
                }
            }
            .addOnFailureListener { e ->
                // Error al realizar la consulta
                println("Error al buscar el usuario con email '$emailUsuario': $e")
            }
    }


    // Función para guardar la fecha como string en SharedPreferences
    fun saveSharedpref(context: Context, claveDato: String, valor: String) {
        val sharedPreferences =
            context.getSharedPreferences("MiSharedPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(claveDato, valor)
        editor.apply()
        println("Se guardo: $claveDato")
    }

    // Función para obtener la fecha guardada en SharedPreferences
    fun getRegistSheredPref(claveDato: String, context: Context): String? {
        val sharedPreferences =
            context.getSharedPreferences("MiSharedPreferences", Context.MODE_PRIVATE)
        println("Se recupero de: $claveDato")
        return sharedPreferences.getString(claveDato, null)
    }

    //guardar un post como favorito, esto va de la mano con FavoritesFragment y su viewmodel
    fun addFavorites(userEmail: String, refNewDocFavorite: String) {
        val usersCollection = db.collection("users")
        val query = usersCollection.whereEqualTo("userEmail", userEmail)

        query.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userDocRef = usersCollection.document(document.id)

                    userDocRef.get()
                        .addOnSuccessListener { userDocument ->
                            if (userDocument.exists()) {
                                val favoritosActuales =
                                    userDocument.get("userFavorites") as? List<String>
                                        ?: emptyList()
                                // Verificar si el nuevo favorito ya está en la lista actual
                                if (favoritosActuales.contains(refNewDocFavorite)) {
                                    // El nuevo favorito ya existe en la lista actual
                                    println(
                                        "El post ya está guardado"
                                    )
                                } else {
                                    // Agrega el nuevo favorito a la lista de favoritos actuales
                                    val nuevaListaFavoritos = favoritosActuales.toMutableList()
                                    nuevaListaFavoritos.add(refNewDocFavorite)

                                    // Actualiza el documento del usuario con la nueva lista de favoritos
                                    userDocRef.update("userFavorites", nuevaListaFavoritos)
                                        .addOnSuccessListener {
                                            println("Post Guardado")
                                            println("Nuevo post favorito agregado correctamente")
                                        }
                                        .addOnFailureListener { e ->
                                            println(
                                                "No se pudo guardar el post, intenta más tarde"
                                            )
                                            println("Error al agregar nuevo favorito: $e")
                                        }
                                }
                            } else {
                                // Si el documento de usuario no existe, crea uno nuevo
                                val nuevaListaFavoritos = listOf(refNewDocFavorite)
                                userDocRef.set(
                                    mapOf(
                                        "userEmail" to userEmail,
                                        "userFavorites" to nuevaListaFavoritos
                                    )
                                )
                                    .addOnSuccessListener {
                                        println("Post Guardado")
                                        println("Campo de favoritos creado con nuevo post favorito")
                                    }
                                    .addOnFailureListener { e ->
                                        println("No se pudo guardar el post, intenta más tarde")
                                        println("Error al crear el documento de usuario con nuevo favorito post: $e")
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            println("Error al obtener documento de usuario: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                println("Error al buscar en query a firebase para añadir favorito: $e")
            }
    }


}
package com.terfess.comunidadmp.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.terfess.comunidadmp.datamodel.Post

class FavoritesViewModel : ViewModel() {

    //variables livedata
    private val _favorites = MutableLiveData<List<Post>>()
    val favorites: LiveData<List<Post>> = _favorites

    // Obtén una referencia al documento de usuario actual
    private val db = FirebaseFirestore.getInstance()

    fun getListFavorites(userEmail: String) {
        val usersCollection = db.collection("users")
        val query = usersCollection.whereEqualTo("userEmail", userEmail)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val favoritos = document.get("userFavorites") as? List<String> ?: emptyList()
                    println("Favoritos lista primaria de firebase es :: $favoritos")

                    if (favoritos.isEmpty()) {
                        _favorites.value = emptyList()
                    }

                    val listaPublicaciones = mutableListOf<Post>()

                    var count =
                        0 // Contador para llevar el seguimiento de cuántas publicaciones se han recuperado

                    for (favorito in favoritos) {
                        db.collection("posts")
                            .document(favorito)
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    val fechaPost = documentSnapshot.getString("fechaPost") ?: ""
                                    val autorPost = documentSnapshot.getString("autorPost") ?: ""
                                    val tituloPost = documentSnapshot.getString("tituloPost") ?: ""
                                    val cuerpoPost = documentSnapshot.getString("cuerpoPost") ?: ""
                                    val imagenPost = documentSnapshot.getString("imagenPost") ?: ""
                                    val emailAutorPost = document.getString("emailAutorPost") ?: ""
                                    val idPost = documentSnapshot.id
                                    println("Recuperado id de post: $idPost")
                                    val post = Post(
                                        fechaPost,
                                        autorPost,
                                        tituloPost,
                                        cuerpoPost,
                                        imagenPost,
                                        idPost,
                                        emailAutorPost
                                    )
                                    listaPublicaciones.add(post)
                                }

                                // Incrementar el contador y emitir la lista de publicaciones si todas se han recuperado
                                count++
                                if (count == favoritos.size) {
                                    _favorites.value = listaPublicaciones
                                    println("Para de buscar favorites y manda :: $listaPublicaciones")
                                }
                            }
                            .addOnFailureListener { exception ->
                                println("Error obteniendo posts: $exception")

                                // Incrementar el contador en caso de error para asegurar que se emita la lista de publicaciones vacía
                                count++
                                if (count == favoritos.size) {
                                    _favorites.value = emptyList()
                                }
                            }
                    }
                } else {
                    _favorites.value = emptyList()
                }
            }
            .addOnFailureListener { e ->
                println("No se pudo obtener lista de favoritos :: $e")
                _favorites.value = emptyList()
            }
    }
}
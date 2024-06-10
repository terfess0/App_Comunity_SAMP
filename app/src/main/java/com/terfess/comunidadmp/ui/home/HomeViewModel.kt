package com.terfess.comunidadmp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.terfess.comunidadmp.datamodel.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _myList = MutableLiveData<List<Post>>()
    val myList: LiveData<List<Post>> = _myList

    fun loadPosts() {
        CoroutineScope(Dispatchers.Default).launch {
            db.collection("posts")
                .orderBy(
                    "fechaPost",
                    Query.Direction.ASCENDING
                ) // Ordenar por fecha de publicación descendente
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty){
                        _myList.value = emptyList()
                    }
                    val listaPublicaciones = mutableListOf<Post>()
                    for (document in documents) {
                        val fechaPost = document.getString("fechaPost") ?: ""
                        val autorPost = document.getString("autorPost") ?: ""
                        val tituloPost = document.getString("tituloPost") ?: ""
                        val cuerpoPost = document.getString("cuerpoPost") ?: ""
                        val imagenPost = document.getString("imagenPost") ?: ""
                        val emailAutorPost = document.getString("emailAutorPost") ?: ""
                        val idPost = document.id
                        println("Recuperado id de post: $idPost")
                        val post = Post(fechaPost, autorPost, tituloPost, cuerpoPost, imagenPost, idPost, emailAutorPost)
                        listaPublicaciones.add(post)
                    }
                    _myList.value = listaPublicaciones
                }
                .addOnFailureListener { exception ->
                    println("Error obteniendo posts: $exception")

                    // Emitir un estado de error si falla la obtención de datos
                    _myList.value = emptyList()
                }
        }
    }
}

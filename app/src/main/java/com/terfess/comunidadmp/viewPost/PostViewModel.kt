package com.terfess.comunidadmp.viewPost

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.terfess.comunidadmp.datamodel.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    private val _infoPost = MutableLiveData<Post>()
    val infoPost: LiveData<Post> = _infoPost
    private val db = Firebase.firestore

    fun loadInfoPost(idPost: String) {
        CoroutineScope(Dispatchers.Default).launch {
            db.collection("posts")
                .document(idPost)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // El documento existe

                        val post = Post(
                            document.getString("fechaPost").toString(),
                            document.getString("autorPost").toString(),
                            document.getString("tituloPost").toString(),
                            document.getString("cuerpoPost").toString(),
                            document.getString("imagenPost").toString(),
                            idPost,
                            document.getString("emailAutorPost").toString()
                        )
                        _infoPost.value = post
                        println("PVM Datos del post: $post")
                    } else {
                        // El documento no existe
                        println("Publicación no encontrada.")

                    }
                }
                .addOnFailureListener { exception ->
                    println("Error obteniendo la info del post: $idPost Error: $exception")
                }
        }
    }
}

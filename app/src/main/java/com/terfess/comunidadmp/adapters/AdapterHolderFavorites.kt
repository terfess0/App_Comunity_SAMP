package com.terfess.comunidadmp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.terfess.comunidadmp.R
import com.terfess.comunidadmp.databinding.FormatoNewsRecyclerBinding
import com.terfess.comunidadmp.datamodel.Post
import com.terfess.comunidadmp.ui.favorites.FavoritesFragmentDirections

class AdapterHolderFavorites(private var data: List<Post>) : RecyclerView.Adapter<AdapterHolderFavorites.Holder>() {
    private val db = FirebaseFirestore.getInstance()
    private val user = Firebase.auth.currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.formato_news_recycler, parent, false)
        return Holder(itemView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val currentItem = data[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = data.size

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding = FormatoNewsRecyclerBinding.bind(itemView)
        private val nombreAutor = binding.nickAuthor
        private val titleTextView = binding.tituloP
        private val cuerpoPost = binding.cuerpoP
        private val imagenP = binding.imgP
        private val perfilAutorImg = binding.imgProfileAuthor
        private val datePost = binding.datePost
        private val containImg = binding.containImgP

        fun bind(post: Post) {
            // Mostrar publicación
            nombreAutor.text = post.autorPost
            titleTextView.text = post.tituloPost
            cuerpoPost.text = post.cuerpoPost
            datePost.text = post.fechaPost.substring(0, 10)

            // Visibilidad de la imagen del post
            if (post.imagenPost.isNotBlank()) {
                containImg.visibility = View.VISIBLE
                // Cargar imagen del post
                Glide.with(itemView)
                    .load(post.imagenPost)
                    .placeholder(R.drawable.predet_image)
                    .into(imagenP)
            } else {
                containImg.visibility = View.GONE
            }

            // Cargar foto de perfil del autor
            db.collection("users")
                .whereEqualTo("userEmail", post.emailAutorPost)
                .get()
                .addOnSuccessListener { querySnapshot ->

                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot
                            .documents[0]
                            .getString("userPhoto")
                        Glide.with(itemView)
                            .load(document)
                            .placeholder(R.drawable.predet_image)
                            .into(perfilAutorImg)
                    }
                }


            //usuario selecciona clickea publicacion
            itemView.setOnClickListener {
                binding.root.findNavController().navigate(
                    FavoritesFragmentDirections.actionFavoritesFragmentToPostFragment(postDocumentId = post.idPost)
                )
                println("Se navegó de guardados en recicler hacia PostFragment con args")
            }
        }
    }

    fun setData(newItems: List<Post>) {
        data = newItems
        notifyDataSetChanged()
    }
}

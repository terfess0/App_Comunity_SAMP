package com.terfess.comunidadmp.viewPost

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.terfess.comunidadmp.R
import com.terfess.comunidadmp.databinding.FragmentPostBinding

class PostFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels()
    private lateinit var binding: FragmentPostBinding
    private val arguments: PostFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)

        val idPost = arguments.postDocumentId

        viewModel.loadInfoPost(idPost)

        viewModel.infoPost.observe(viewLifecycleOwner) { post ->
            // Actualizar la UI con los datos del post
            binding.viewPostTitle.text = post.tituloPost
            binding.viewPostContent.text = post.cuerpoPost

            Glide.with(binding.root)
                .load(post.imagenPost)
                .placeholder(R.drawable.predet_image) // Imagen de marcador de posición
                .error(R.drawable.predet_image) // Imagen de error
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Almacenamiento en caché tanto en memoria como en disco
                .skipMemoryCache(false) // No omitir la caché de memoria
                .into(binding.viewPostImage)

            binding.viewPostImage.setOnClickListener {
                // Crear una instancia del BottomSheetDialog con el tema personalizado
                val bottomSheetDialog = ImageBottomSheetDialog(post.imagenPost)
                bottomSheetDialog.show(parentFragmentManager, "ImageBottomSheetDialog")
            }



            binding.viewPostDate.text = post.fechaPost.substring(0, 10)
            binding.viewPostAutor.text = "@${post.autorPost}"

            println("PF Datos del post: $post")
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_postFragment_to_navigation_home)
            println("Se navegó de postFragment a HomeFragment")
        }

        return binding.root
    }
}

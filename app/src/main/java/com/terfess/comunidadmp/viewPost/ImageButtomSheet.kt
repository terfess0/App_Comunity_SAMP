package com.terfess.comunidadmp.viewPost

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.terfess.comunidadmp.R

class ImageBottomSheetDialog(private val imageUrl: String) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_img_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = view.findViewById<ImageView>(R.id.maxi_image)


        // Cargar la imagen utilizando Glide
        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.predet_image) // Imagen de marcador de posición
            .error(R.drawable.predet_image) // Imagen de error
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Almacenamiento en caché tanto en memoria como en disco
            .skipMemoryCache(false) // No omitir la caché de memoria
            .into(imageView)

    }

}

package com.terfess.comunidadmp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.terfess.comunidadmp.R
import com.terfess.comunidadmp.adapters.AdapterHolderHome
import com.terfess.comunidadmp.clases.Utilities
import com.terfess.comunidadmp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adaptador: AdapterHolderHome
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // Llamada a la función para obtener posts de Firestore
        homeViewModel.loadPosts()

        val user = Firebase.auth.currentUser

        if (user == null) {
            Snackbar.make(root, "Debe iniciar sesión", Snackbar.LENGTH_LONG).show()
            findNavController().navigate(R.id.navigation_login)
            findNavController().popBackStack(R.id.navigation_login, false)
        }

        progressBar = binding.progressBar
        val recicler = binding.contenido


        homeViewModel.myList.observe(viewLifecycleOwner, Observer { items ->
            // Actualiza tu UI con la lista de items

            adaptador.setData(items)

            progressBar.visibility = View.GONE

            // Mostrar o ocultar el texto de "No hay publicaciones" según si la lista está vacía
            if (items == null) {
                binding.noPostsText.visibility = View.VISIBLE
            } else if (items.isEmpty()) {
                binding.noPostsText.visibility = View.VISIBLE
            } else {
                binding.noPostsText.visibility = View.GONE
            }
        })

        adaptador = AdapterHolderHome(emptyList())
        recicler.adapter = adaptador
        recicler.layoutManager = LinearLayoutManager(root.context)

        binding.newPublisBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_writePost)
        }

        // Escucha el evento de deslizar para recargar
        recicler.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) homeViewModel.loadPosts()

            false
        }

        Utilities().getUserName(root.context, user?.email.toString())

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
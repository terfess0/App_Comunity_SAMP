package com.terfess.comunidadmp.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.terfess.comunidadmp.adapters.AdapterHolderFavorites
import com.terfess.comunidadmp.databinding.FragmentFavoritesBinding

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private lateinit var adaptador: AdapterHolderFavorites
    private lateinit var progressBar: ProgressBar
    private lateinit var favoritesViewModel: FavoritesViewModel

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        favoritesViewModel = ViewModelProvider(this).get(FavoritesViewModel::class.java)

        // Inicializar RecyclerView y ProgressBar
        progressBar = binding.progressBar
        val reciclerGuardados = binding.contenido
        adaptador = AdapterHolderFavorites(emptyList())
        reciclerGuardados.adapter = adaptador
        reciclerGuardados.layoutManager = LinearLayoutManager(root.context)

        // Obtener favoritos del usuario actual si está autenticado
        val user = Firebase.auth.currentUser
        if (user != null) {
            println("**Get Favorites Posts")
            favoritesViewModel.getListFavorites(user.email.toString())
        }

        // Observar cambios en la lista de favoritos
        favoritesViewModel.favorites.observe(viewLifecycleOwner, Observer { favoritos ->
            // Actualizar UI con la lista de favoritos
            adaptador.setData(favoritos)
            println("Lista de favoritos a mostrar: $favoritos")
            progressBar.visibility = View.GONE

            // Mostrar o ocultar el texto de "No hay publicaciones" según si la lista está vacía
            if (favoritos == null) {
                binding.noPostsText.visibility = View.VISIBLE
            } else if (favoritos.isEmpty()) {
                binding.noPostsText.visibility = View.VISIBLE
            } else {
                binding.noPostsText.visibility = View.GONE
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

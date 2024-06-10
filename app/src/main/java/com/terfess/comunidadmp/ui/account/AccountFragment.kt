package com.terfess.comunidadmp.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.terfess.comunidadmp.R
import com.terfess.comunidadmp.clases.Utilities
import com.terfess.comunidadmp.databinding.ActivityMainBinding
import com.terfess.comunidadmp.databinding.FragmentAccountBinding
import java.io.File

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val db = FirebaseFirestore.getInstance()
    private val user = Firebase.auth.currentUser
    private val nombreArchivoFotoPerfil = "profile_perfil.jpg"
    private var navController: NavController? = null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val dashboardViewModel =
            ViewModelProvider(this)[AccountViewModel::class.java]

        _binding = FragmentAccountBinding.inflate(inflater, container, false)

        val root: View = binding.root

        val localFile = File(root.context?.filesDir, nombreArchivoFotoPerfil)
        println("Archivo de imagen presente: $localFile")

        dashboardViewModel.getDateRegist(root.context)

        navController = findNavController()

        val email = user?.email
        binding.UserCorreo.text = email ?: "Desconocido"

        val fechaRegist = Utilities().getRegistSheredPref("fechaRegistro", root.context)
        binding.FechaCreacion.text = fechaRegist  ?: "Desconocido"

        val nameUser = Utilities().getRegistSheredPref("userName", root.context)
        binding.NombreUsuario.text = nameUser  ?: "Desconocido"


        var refImagen = ""
        //devolucion de llamada cuando el usuario elige imagen local se subira a storage
        val getContent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri == null) {
                    // La ventana fue cerrada sin seleccionar una imagen
                    Snackbar.make(root, "No se seleccionó ninguna imagen", Snackbar.LENGTH_SHORT)
                        .show()
                    // Habilitar la navegación y ocultar el indicador de carga
                    enableNavigation(true)
                    showLoadingIndicator(false)
                } else {
                    enableNavigation(false)
                    showLoadingIndicator(true)
                    // Handle the returned Uri
                    val imageViewIcon = binding.imageViewFotoPerfil

                    uri?.let { img ->
                        imageViewIcon.setImageURI(img) // Muestra la imagen seleccionada en tu ImageView

                        // Upload the selected image to Firebase Storage
                        val refStorage = FirebaseStorage.getInstance().reference
                        val riversRef =
                            refStorage.child("images/${System.currentTimeMillis()}_${File(img.path!!).name}")

                        // Upload the selected image to Firebase Storage
                        val uploadTask = riversRef.putFile(img)

                        uploadTask.addOnFailureListener { exception ->
                            // Handle unsuccessful uploads
                            println("Upload failed: $exception")
                            // Habilitar la navegación y ocultar el indicador de carga
                            enableNavigation(true)
                            showLoadingIndicator(false)
                        }.addOnSuccessListener { _ ->
                            // Obtener la URL de descarga de la imagen
                            riversRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                val imageUrl = downloadUri.toString()
                                refImagen = imageUrl

                                // Actualizar la referencia de la imagen en Firestore
                                Utilities().actualizarFotoUsuario(
                                    root,
                                    user?.email.toString(),
                                    refImagen
                                )


//                            // Borrar la imagen anterior localmente (si existe)
//                            val localFile3 = File(root.context?.filesDir, nombreArchivoFotoPerfil)
//                            if (localFile3.exists()) {
//                                localFile3.delete()
//                                println("Imagen anterior borrada exitosamente.")
//                            }

                                // Habilitar la navegación y ocultar el indicador de carga
                                enableNavigation(true)
                                showLoadingIndicator(false)
                            }
                        }
                    }

                }
            }


        binding.buttonCerrarSesion.setOnClickListener {
            // Usuario presiona botón cerrar sesión
            Firebase.auth.signOut()
            Snackbar.make(root, "Saliste de tu cuenta.", Snackbar.LENGTH_SHORT).show()

            // Navegar al fragmento de inicio de sesión
            findNavController().navigate(R.id.action_navigation_account_to_navigation_login)
            root.findNavController().popBackStack(R.id.navigation_login, false)
        }

        binding.imageViewFotoPerfil.setOnClickListener {
            //el usuario esta logeado?
            if (user != null) {
                //seleccionar imagen local
                getContent.launch("image/*")
            } else {
                Snackbar.make(root, "Debe iniciar sesión", Snackbar.LENGTH_SHORT).show()

                // Navegar al fragmento de inicio de sesión
                findNavController().navigate(R.id.action_navigation_account_to_navigation_login)
                root.findNavController().popBackStack(R.id.navigation_login, false)
            }
        }

        getImageProfile(root)

        return root
    }

    private fun showLoadingIndicator(show: Boolean) {
        binding.progressBar2.visibility = if (show) View.VISIBLE else View.GONE
    }


    private fun enableNavigation(enabled: Boolean) {
        val bin = ActivityMainBinding.bind(requireActivity().findViewById(R.id.container))
        if (enabled) {
            // Habilitar la navegación
            bin.navView.visibility = View.VISIBLE
            navController?.let { controller ->
                val navGraph = controller.navInflater.inflate(R.navigation.mobile_navigation)
                controller.graph = navGraph
            }
        } else {
            // Bloquear la navegación
            bin.navView.visibility = View.GONE
        }
    }


    fun getImageProfile(root: View) {
        db.collection("users")
            .whereEqualTo("userEmail", user?.email.toString())
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val imagenPerfil =
                        querySnapshot.documents[0].getString("userPhoto")

                    Glide.with(root.context)
                        .load(imagenPerfil)
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Almacenamiento en caché tanto en memoria como en disco
                        .skipMemoryCache(false) // No omitir la caché de memoria
                        .into(root.findViewById(R.id.imageViewFotoPerfil))


//                    val localFile2 = File(root.context?.filesDir, nombreArchivoFotoPerfil)
//                    // Borrar la imagen anterior localmente (si existe)
//                    if (localFile2.exists()) {
//                        localFile2.delete()
//                        println("Imagen anterior borrada exitosamente.")
//                    }

//                    downloadImageProfile(root, imagenPerfil.toString())
                }
            }
    }

//    private fun downloadImageProfile(root: View, urlImage: String) {
//        println("Descargando archivo $nombreArchivoFotoPerfil")
//
//        try {
//            // La imagen no está descargada, descárgala desde Firebase Storage
//            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(urlImage)
//
//            val localFile = File(root.context?.filesDir, nombreArchivoFotoPerfil)
//
//            storageRef.getFile(localFile)
//                .addOnSuccessListener {
//                    // La imagen ya está descargada localmente, cárgala desde la memoria interna
//                    Glide.with(root.context)
//                        .load(localFile)
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
//                        .into(root.findViewById(R.id.imageViewFotoPerfil))
//
//                    println("Archivo profile_photo.jpg fue guardado en almacenamiento local.")
//                }
//                .addOnFailureListener { exception ->
//                    println("Error al guardar archivo profile_photo.jpg : $exception")
//                }
//        } catch (e: IllegalArgumentException) {
//            println("Error: La URL del Storage de Firebase no es válida: $urlImage")
//            e.printStackTrace()
//        }
//    }
//
//
//
//    private fun checkImageProfileLocal(root: View) {
//        // Comprueba si la imagen ya está descargada localmente
//        val localFile = File(root.context?.filesDir, nombreArchivoFotoPerfil)
//        if (localFile.exists()) {
//            // La imagen ya está descargada localmente, cárgala desde la memoria interna
//            Glide.with(root.context)
//                .load(localFile)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .into(root.findViewById(R.id.imageViewFotoPerfil))
//            println("Fue recuperada la foto de perfil: $nombreArchivoFotoPerfil.")
//        } else {
//            println("No hay imagen guardada, se procede a buscar en firebase.")
//            getImageProfile(root)
//
//        }
//    }

}

//// Realiza una consulta para verificar si el nombre de usuario ya está en uso
//        usuariosRef.whereEqualTo("userName", nombre)
//            .get()
//            .addOnSuccessListener { querySnapshot ->
//                if (!querySnapshot.isEmpty) {
//                    // El nombre de usuario ya está en uso
//                    Toast.makeText(
//                        contexto,
//                        "El usuario '$nombre' ya está en uso.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
//                    // El nombre de usuario no está en uso, procede a crear la cuenta
//
//                }
//            }
//            .addOnFailureListener { e ->
//                // Error al verificar el nombre de usuario
//                Toast.makeText(
//                    contexto,
//                    "No se pudo verificar el nombre de usuario: $e",
//                    Toast.LENGTH_SHORT
//                ).show()
//                println("Falla verificar nombre de usuario caso : $e")
//            }

//        val textView: TextView = binding.textDashboard
//        dashboardViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }


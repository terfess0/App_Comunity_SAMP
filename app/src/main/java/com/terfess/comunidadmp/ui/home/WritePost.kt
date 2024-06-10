package com.terfess.comunidadmp.ui.home

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.terfess.comunidadmp.R
import com.terfess.comunidadmp.clases.Utilities
import com.terfess.comunidadmp.databinding.FragmentWritePostBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


class WritePost : Fragment() {
    private lateinit var binding: FragmentWritePostBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentWritePostBinding.inflate(inflater, container, false)
        val root = binding.root


        val buttonSelectImage = binding.btnSelectImgPost
        var refImagen = ""
        val btnPublicar = binding.btnSendPublish
        val progresIc = binding.progressPost

        //devolucion de llamada cuando el usuario elige imagen local se subira a storage
        val getContent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                // Handle the returned Uri
                val imageViewIcon = binding.imagenPost

                //mostrar progress ic en lugar de boton
                btnPublicar.visibility = View.GONE
                progresIc.visibility = View.VISIBLE

                val selectedImage: Uri? = uri
                selectedImage?.let { img ->
                    imageViewIcon.visibility = View.VISIBLE
                    imageViewIcon.setImageURI(img) // Muestra la imagen seleccionada en tu ImageView

                    // Upload the selected image to Firebase Storage
                    val refStorage = FirebaseStorage.getInstance().reference
                    val riversRef =
                        refStorage.child("images/${System.currentTimeMillis()}_${File(img.path!!).name}")

                    CoroutineScope(Dispatchers.Main).launch {
                        val uploadTask = riversRef.putFile(img)

                        uploadTask.addOnFailureListener { exception ->
                            // Handle unsuccessful uploads
                            println("Upload failed: $exception")
                        }.addOnSuccessListener { _ ->
                            // Obtener la URL de descarga de la imagen
                            riversRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                val imageUrl = downloadUri.toString()
                                refImagen = imageUrl

                                //habilitar boton
                                btnPublicar.visibility = View.VISIBLE
                                progresIc.visibility = View.GONE
                            }
                        }
                    }
                }
            }

        buttonSelectImage.setOnClickListener {
            //seleccionar imagen local
            getContent.launch("image/*")
        }

        val edtTitulo = binding.editTextTitle
        val editTextBody = binding.editTextBody

        editTextBody.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                // Ocultar el teclado suavemente
                hideKeyboard(editTextBody)
            }
            false
        }

        val user = Firebase.auth.currentUser
        val db = FirebaseFirestore.getInstance()

        val timestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = Date(timestamp)
        val fechaPost = dateFormat.format(date)

        var refdoc = ""

        //publicar
        btnPublicar.setOnClickListener {
            if (user != null) {
                val titulo = edtTitulo.text.toString()
                val cuerpo = editTextBody.text.toString()
                val emailAutor = user.email
                var autor = Utilities().getRegistSheredPref("userName", root.context)

                if (titulo.isBlank() || cuerpo.isBlank()) {
                    Toast.makeText(
                        root.context,
                        "Complete titulo y texto del post",
                        Toast.LENGTH_LONG
                    ).show()
                }else{


                    //mostrar progress ic en lugar de boton
                    btnPublicar.visibility = View.GONE
                    progresIc.visibility = View.VISIBLE

                    if (autor.isNullOrBlank()) autor = "unknown"

                    // Crear un nuevo mapa con los datos a guardar
                    val datosPost = hashMapOf(
                        "tituloPost" to titulo,
                        "cuerpoPost" to cuerpo,
                        "autorPost" to autor,
                        "fechaPost" to fechaPost,
                        "imagenPost" to refImagen,
                        "emailAutorPost" to emailAutor
                    )

                    // Agregar el nuevo documento a la colección "posts"
                    db.collection("posts")
                        .add(datosPost)
                        .addOnSuccessListener { documentReference ->
                            println("Documento agregado con ID: ${documentReference.id}")
                            refdoc = documentReference.id
                            // Limpiar los EditText después de agregar los datos
                            edtTitulo.setText("")
                            editTextBody.setText("")
                            btnPublicar.visibility = View.VISIBLE
                            progresIc.visibility = View.GONE

                            Snackbar.make(root, "Post Enviado", Snackbar.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.navigation_home)
                            findNavController().popBackStack(R.id.navigation_home, false)
                        }
                        .addOnFailureListener { e ->
                            btnPublicar.visibility = View.VISIBLE
                            progresIc.visibility = View.GONE

                            Toast.makeText(
                                root.context,
                                "Algo salio mal, prueba más tarde",
                                Toast.LENGTH_SHORT
                            ).show()
                            println("Algo salio mal al subir post: $e")
                            findNavController().navigate(R.id.navigation_home)
                            findNavController().popBackStack(R.id.navigation_home, false)
                        }
                }

            } else {
                Toast.makeText(root.context, "Debe Iniciar Sesión", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.navigation_login)
                findNavController().popBackStack(R.id.navigation_home, false)
            }
        }

        return root

    }

    private fun hideKeyboard(editTextBody: EditText) {
        val imm =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editTextBody.windowToken, 0)
    }


}